package com.tinysweet.dataexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.utils.RootUtils
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPreferencesDetailScreen(
    modifier: Modifier = Modifier,
    prefsFilePath: String,
    prefsFileName: String,
    onBack: () -> Unit
) {
    var prefsEntries by remember { mutableStateOf<List<PrefEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(prefsFilePath) {
        isLoading = true
        errorMessage = null
        try {
            val content = RootUtils.readFile(prefsFilePath)
            prefsEntries = parseSharedPreferencesXml(content)
        } catch (e: Exception) {
            errorMessage = "Không thể đọc file: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(prefsFileName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm entry")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(prefsEntries, key = { it.key }) { entry ->
                            PrefEntryItem(
                                entry = entry,
                                onValueChange = { newValue ->
                                    val updatedEntry = entry.copy(
                                        value = parseValueByType(newValue, entry.type)
                                    )
                                    val updatedEntries = prefsEntries.map { currentEntry ->
                                        if (currentEntry.key == entry.key) updatedEntry else currentEntry
                                    }
                                    prefsEntries = updatedEntries
                                    scope.launch {
                                        saveSharedPreferences(prefsFilePath, updatedEntries)
                                    }
                                },
                                onDelete = {
                                    val updatedEntries = prefsEntries.filter { currentEntry ->
                                        currentEntry.key != entry.key
                                    }
                                    prefsEntries = updatedEntries
                                    scope.launch {
                                        saveSharedPreferences(prefsFilePath, updatedEntries)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPrefEntryDialog(
            onAdd = { newEntry ->
                val updatedEntries = prefsEntries + newEntry
                prefsEntries = updatedEntries
                scope.launch {
                    saveSharedPreferences(prefsFilePath, updatedEntries)
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun PrefEntryItem(
    entry: PrefEntry,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.key,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = formatValueForDisplay(entry.value, entry.type),
                onValueChange = onValueChange,
                label = { Text(entry.type) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        }
    }
}

@Composable
fun AddPrefEntryDialog(
    onAdd: (PrefEntry) -> Unit,
    onDismiss: () -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("string") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it.lowercase() },
                    label = { Text("Type: string/int/float/boolean/long/string-set") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (key.isNotBlank()) {
                        onAdd(
                            PrefEntry(
                                key = key,
                                value = parseValueByType(value, type),
                                type = type
                            )
                        )
                    }
                }
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

fun parseSharedPreferencesXml(xmlContent: String): List<PrefEntry> {
    val entries = mutableListOf<PrefEntry>()

    try {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "string" -> {
                        val key = parser.getAttributeValue(null, "name")
                        val text = parser.nextText()
                        if (!key.isNullOrBlank()) {
                            entries.add(PrefEntry(key = key, value = text, type = "string"))
                        }
                    }

                    "int", "float", "boolean", "long" -> {
                        val entryType = parser.name
                        val key = parser.getAttributeValue(null, "name")
                        val attrValue = parser.getAttributeValue(null, "value") ?: ""
                        if (!key.isNullOrBlank()) {
                            entries.add(
                                PrefEntry(
                                    key = key,
                                    value = parseValueByType(attrValue, entryType),
                                    type = entryType
                                )
                            )
                        }
                    }

                    "set" -> {
                        val key = parser.getAttributeValue(null, "name")
                        if (!key.isNullOrBlank()) {
                            val values = mutableSetOf<String>()
                            var nestedEventType = parser.next()
                            while (!(nestedEventType == XmlPullParser.END_TAG && parser.name == "set")) {
                                if (nestedEventType == XmlPullParser.START_TAG && parser.name == "string") {
                                    values.add(parser.nextText())
                                }
                                nestedEventType = parser.next()
                            }
                            entries.add(PrefEntry(key = key, value = values, type = "string-set"))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
    } catch (_: Exception) {
        // Ignore parsing errors and return partial result
    }

    return entries
}

suspend fun saveSharedPreferences(filePath: String, entries: List<PrefEntry>): Boolean {
    val xmlContent = buildSharedPreferencesXml(entries)
    return RootUtils.writeFile(filePath, xmlContent)
}

fun buildSharedPreferencesXml(entries: List<PrefEntry>): String {
    val sb = StringBuilder()
    sb.append("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n")
    sb.append("<map>\n")

    entries.forEach { entry ->
        when (entry.type) {
            "string" -> sb.append(
                "    <string name=\"${escapeXml(entry.key)}\">${escapeXml(entry.value.toString())}</string>\n"
            )
            "int" -> sb.append(
                "    <int name=\"${escapeXml(entry.key)}\" value=\"${entry.value}\" />\n"
            )
            "float" -> sb.append(
                "    <float name=\"${escapeXml(entry.key)}\" value=\"${entry.value}\" />\n"
            )
            "boolean" -> sb.append(
                "    <boolean name=\"${escapeXml(entry.key)}\" value=\"${entry.value}\" />\n"
            )
            "long" -> sb.append(
                "    <long name=\"${escapeXml(entry.key)}\" value=\"${entry.value}\" />\n"
            )
            "string-set" -> {
                sb.append("    <set name=\"${escapeXml(entry.key)}\">\n")
                val setValues = entry.value as? Set<*> ?: emptySet<Any>()
                setValues.forEach { item ->
                    sb.append("        <string>${escapeXml(item.toString())}</string>\n")
                }
                sb.append("    </set>\n")
            }
        }
    }

    sb.append("</map>")
    return sb.toString()
}

fun escapeXml(value: String): String {
    return value
        .replace("&", "&")
        .replace("<", "<")
        .replace(">", ">")
        .replace("\"", """)
        .replace("'", "'")
}

fun parseValueByType(value: String, type: String): Any {
    return when (type) {
        "int" -> value.toIntOrNull() ?: 0
        "float" -> value.toFloatOrNull() ?: 0f
        "boolean" -> value.equals("true", ignoreCase = true)
        "long" -> value.toLongOrNull() ?: 0L
        "string-set" -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        else -> value
    }
}

fun formatValueForDisplay(value: Any, type: String): String {
    return when (type) {
        "string-set" -> (value as? Set<*>)?.joinToString(", ") { it.toString() } ?: ""
        else -> value.toString()
    }
}

data class PrefEntry(
    val key: String,
    val value: Any,
    val type: String
)
