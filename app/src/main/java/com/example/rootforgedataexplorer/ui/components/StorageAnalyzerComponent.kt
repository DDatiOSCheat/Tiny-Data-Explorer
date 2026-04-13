package com.example.rootforgedataexplorer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rootforgedataexplorer.utils.RootUtils

/**
 * StorageAnalyzerComponent - Phân tích dung lượng lưu trữ
 */
@Composable
fun StorageAnalyzerComponent(
    appPackageName: String,
    modifier: Modifier = Modifier
) {
    var totalSize by remember { mutableStateOf("0 B") }
    var databasesSize by remember { mutableStateOf("0 B") }
    var sharedPrefsSize by remember { mutableStateOf("0 B") }
    var cacheSize by remember { mutableStateOf("0 B") }
    var filesSize by remember { mutableStateOf("0 B") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(appPackageName) {
        isLoading = true
        try {
            val basePath = "/data/data/$appPackageName"
            totalSize = RootUtils.getDirectorySize(basePath)
            databasesSize = RootUtils.getDirectorySize("$basePath/databases")
            sharedPrefsSize = RootUtils.getDirectorySize("$basePath/shared_prefs")
            cacheSize = RootUtils.getDirectorySize("$basePath/cache")
            filesSize = RootUtils.getDirectorySize("$basePath/files")
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Phân tích dung lượng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                StorageItem(
                    label = "Tổng dung lượng",
                    size = totalSize,
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                StorageItem(
                    label = "Cơ sở dữ liệu",
                    size = databasesSize,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                StorageItem(
                    label = "SharedPreferences",
                    size = sharedPrefsSize,
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                StorageItem(
                    label = "Cache",
                    size = cacheSize,
                    color = MaterialTheme.colorScheme.error
                )
                
                StorageItem(
                    label = "Files",
                    size = filesSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StorageItem(
    label: String,
    size: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(12.dp)
                ) {
                    drawCircle(color)
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = size,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}