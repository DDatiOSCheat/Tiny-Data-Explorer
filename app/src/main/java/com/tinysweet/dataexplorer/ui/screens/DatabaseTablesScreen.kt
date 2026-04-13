package com.example.rootforgedataexplorer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rootforgedataexplorer.utils.RootUtils

/**
 * DatabaseTablesScreen - Hiển thị và quản lý các bảng trong SQLite database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTablesScreen(
    modifier: Modifier = Modifier,
    databasePath: String,
    databaseName: String
) {
    var tables by remember { mutableStateOf<List<TableInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTable by remember { mutableStateOf<TableInfo?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load tables when entering screen
    LaunchedEffect(databasePath) {
        isLoading = true
        errorMessage = null
        try {
            tables = loadTables(databasePath)
        } catch (e: Exception) {
            errorMessage = "Không thể tải bảng: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    if (selectedTable != null) {
        // Show table data
        TableDataScreen(
            databasePath = databasePath,
            tableName = selectedTable!!.name,
            onBack = { selectedTable = null }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(databaseName) },
                    navigationIcon = {
                        IconButton(onClick = { /* Navigate back */ }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            // Execute custom query
                        }) {
                            Icon(Icons.Default.Sql, contentDescription = "SQL Query")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = modifier.fillMaxSize().padding(padding)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tables, key = { it.name }) { table ->
                            TableCard(
                                table = table,
                                onClick = { selectedTable = table },
                                databasePath = databasePath
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCard(
    table: TableInfo,
    onClick: () -> Unit,
    databasePath: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${table.rowCount} hàng, ${table.columns.size} cột",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * TableDataScreen - Hiển thị dữ liệu bảng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(
    databasePath: String,
    tableName: String,
    onBack: () -> Unit
) {
    var columns by remember { mutableStateOf<List<String>>(emptyList()) }
    var rows by remember { mutableStateOf<List<List<Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    var showQueryDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(tableName, databasePath) {
        isLoading = true
        try {
            // Get table info using sqlite3 command
            val schemaQuery = "sqlite3 '$databasePath' '.schema $tableName'"
            val schema = RootUtils.readFile(databasePath)
            
            columns = getColumnsFromSchema(schema)
            
            // Get all data
            val selectQuery = "sqlite3 '$databasePath' 'SELECT * FROM $tableName'"
            val data = RootUtils.readFile(databasePath)
            rows = parseSqliteData(data, columns.size)
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tableName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showQueryDialog = true }) {
                        Icon(Icons.Default.Sql, contentDescription = "SQL Query")
                    }
                    IconButton(onClick = {
                        // Export to CSV
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Column headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    columns.forEach { column ->
                        Text(
                            text = column,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Data rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(rows.size) { rowIndex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            rows[rowIndex].forEach { value ->
                                Text(
                                    text = value?.toString() ?: "NULL",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
    
    // Query Dialog
    if (showQueryDialog) {
        AlertDialog(
            onDismissRequest = { showQueryDialog = false },
            title = { Text("SQL Query") },
            text = {
                Column {
                    Text(
                        text = "Nhập câu truy vấn SQL:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = { Text("SELECT * FROM ...") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Execute query
                    showQueryDialog = false
                }) {
                    Text("Thực thi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQueryDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

/**
 * Th��ng tin bảng SQLite
 */
data class TableInfo(
    val name: String,
    val columns: List<String>,
    val rowCount: Int
)

/**
 * Load tables từ SQLite database
 */
fun loadTables(databasePath: String): List<TableInfo> {
    val tables = mutableListOf<TableInfo>()
    
    try {
        // Get list of tables
        val listTablesCmd = "sqlite3 '$databasePath' \".tables\""
        val tablesList = RootUtils.readFile(databasePath)
        
        if (tablesList.isNotBlank()) {
            tablesList.trim().split(Regex("\\s+")).forEach { tableName ->
                if (tableName.isNotBlank()) {
                    // Get row count
                    val countCmd = "sqlite3 '$databasePath' \"SELECT COUNT(*) FROM $tableName\""
                    val countStr = RootUtils.readFile(databasePath)
                    val rowCount = countStr.trim().toIntOrNull() ?: 0
                    
                    // Get column info
                    val schemaCmd = "sqlite3 '$databasePath' \".schema $tableName\""
                    val schema = RootUtils.readFile(databasePath)
                    val columns = getColumnsFromSchema(schema)
                    
                    tables.add(TableInfo(tableName, columns, rowCount))
                }
            }
        }
    } catch (e: Exception) {
        // Handle error
    }
    
    return tables
}

/**
 * Get columns from CREATE TABLE schema
 */
fun getColumnsFromSchema(schema: String): List<String> {
    val columns = mutableListOf<String>()
    val createMatch = Regex("""CREATE\s+TABLE\s+\w+\s*\(([^)]+)\)""", RegexOption.IGNORE_CASE)
    val match = createMatch.find(schema)
    
    if (match != null) {
        val columnsDef = match.groupValues[1]
        columnsDef.split(",").forEach { col ->
            val colName = col.trim().split(Regex("\\s+"))[0]
            if (colName.isNotBlank() && !colName.startsWith("PRIMARY") && 
                !colName.startsWith("FOREIGN") && !colName.startsWith("UNIQUE") &&
                !colName.startsWith("CHECK") && !colName.startsWith("CONSTRAINT")) {
                columns.add(colName)
            }
        }
    }
    
    return columns
}

/**
 * Parse SQLite data output
 */
fun parseSqliteData(data: String, columnCount: Int): List<List<Any>> {
    val rows = mutableListOf<List<Any>>()
    val lines = data.lines()
    
    lines.forEach { line ->
        if (line.isNotBlank()) {
            val values = line.split("|").map { it.trim() }
            if (values.size == columnCount) {
                rows.add(values.map { 
                    when {
                        it == "" || it == "NULL" -> null
                        it.toIntOrNull() != null -> it.toInt()
                        it.toDoubleOrNull() != null -> it.toDouble()
                        else -> it
                    }
                })
            }
        }
    }
    
    return rows
}