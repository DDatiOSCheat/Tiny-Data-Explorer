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
import com.example.rootforgedataexplorer.utils.DatabaseInfo
import com.example.rootforgedataexplorer.utils.RootUtils
import com.example.rootforgedataexplorer.ui.utils.Icons
import kotlinx.coroutines.launch

/**
 * SQLiteExplorerScreen - Khám phá và chỉnh sửa cơ sở dữ liệu SQLite
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SQLiteExplorerScreen(
    modifier: Modifier = Modifier,
    appPackageName: String
) {
    var databases by remember { mutableStateOf<List<DatabaseInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Load databases khi vào màn hình
    LaunchedEffect(appPackageName) {
        isLoading = true
        errorMessage = null
        try {
            databases = loadDatabases(appPackageName)
        } catch (e: Exception) {
            errorMessage = "Không thể tải cơ sở dữ liệu: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SQLite Explorer") },
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
                if (databases.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Không tìm thấy cơ sở dữ liệu nào",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Trong thư mục /data/data/$appPackageName/",
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
                        items(databases, key = { it.name }) { db ->
                            DatabaseItem(
                                database = db,
                                onClick = {
                                    // Navigate to table list for this database
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
fun DatabaseItem(
    database: DatabaseInfo,
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
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = database.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kích thước: ${formatFileSize(database.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sửa đổi: ${database.lastModified}",
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
 * Load databases từ thư mục app data
 */
fun loadDatabases(packageName: String): List<DatabaseInfo> {
    val dbPath = "/data/data/$packageName/databases"
    val files = RootUtils.listDirectory(dbPath)
    
    return files.filter { it.isDirectory == false && it.name.endsWith(".db") }
        .map { file ->
            DatabaseInfo(
                name = file.name,
                path = "$dbPath/${file.name}",
                size = file.size,
                lastModified = "" // TODO: Get actual last modified time
            )
        }
}

/**
 * Thông tin cơ sở dữ liệu
 */
data class DatabaseInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: String
)

/**
 * Format kích thước file
 */
fun formatFileSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${size / 1024} KB"
    size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
    else -> "${size / (1024 * 1024 * 1024)} GB"
}