package com.example.rootforgedataexplorer.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rootforgedataexplorer.utils.RootUtils
import com.example.rootforgedataexplorer.utils.SharedPreferencesInfo
import com.example.rootforgedataexplorer.ui.utils.Icons
import kotlinx.coroutines.launch

/**
 * SharedPreferencesEditorScreen - Chỉnh sửa file SharedPreferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedPreferencesEditorScreen(
    modifier: Modifier = Modifier,
    appPackageName: String
) {
    var prefsFiles by remember { mutableStateOf<List<SharedPreferencesInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Load SharedPreferences files
    LaunchedEffect(appPackageName) {
        isLoading = true
        errorMessage = null
        try {
            prefsFiles = loadSharedPreferencesFiles(appPackageName)
        } catch (e: Exception) {
            errorMessage = "Không thể tải file SharedPreferences: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SharedPreferences Editor") },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Lỗi không xác định",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                if (prefsFiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Không tìm thấy file SharedPreferences nào",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Trong thư mục /data/data/$appPackageName/shared_prefs/",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(prefsFiles, key = { it.name }) { prefs ->
                            SharedPreferencesItem(
                                prefs = prefs,
                                onClick = {
                                    // Navigate to edit this prefs file
                                },
                                appPackageName = appPackageName
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedPreferencesItem(
    prefs: SharedPreferencesInfo,
    onClick: () -> Unit,
    appPackageName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prefs.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prefs.filePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "Sửa đổi: ${prefs.lastModified}",
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
 * Load SharedPreferences files từ thư mục app data
 */
fun loadSharedPreferencesFiles(packageName: String): List<SharedPreferencesInfo> {
    val prefsPath = "/data/data/$packageName/shared_prefs"
    val files = RootUtils.listDirectory(prefsPath)
    
    return files.filter { it.isDirectory == false && it.name.endsWith(".xml") }
        .map { file ->
            SharedPreferencesInfo(
                name = file.name.substringBeforeLast(".xml"),
                filePath = "$prefsPath/${file.name}",
                lastModified = "" // TODO: Get actual last modified time
            )
        }
}