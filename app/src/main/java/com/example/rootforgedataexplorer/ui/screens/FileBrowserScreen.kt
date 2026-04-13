package com.example.rootforgedataexplorer.ui.screens

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
import com.example.rootforgedataexplorer.utils.FileInfo
import com.example.rootforgedataexplorer.utils.RootUtils
import kotlinx.coroutines.launch

/**
 * FileBrowserScreen - Duyệt file bằng root
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    modifier: Modifier = Modifier,
    initialPath: String = "/data/data"
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    var files by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Load files khi path thay đổi
    LaunchedEffect(currentPath) {
        isLoading = true
        errorMessage = null
        try {
            files = RootUtils.listDirectory(currentPath)
        } catch (e: Exception) {
            errorMessage = "Không thể đọc thư mục: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Path bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    // Navigate up
                    val parentPath = currentPath.substringBeforeLast("/")
                    if (parentPath.isNotEmpty()) {
                        currentPath = parentPath
                    }
                }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Lên thư mục")
                }
                IconButton(onClick = {
                    scope.launch {
                        isLoading = true
                        files = RootUtils.listDirectory(currentPath)
                        isLoading = false
                    }
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                }
            }
        }
        
        // File list
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Thư mục lên trước
                val sortedFiles = files.sortedWith(
                    compareByDescending<FileInfo> { it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )
                
                items(sortedFiles, key = { it.name }) { file ->
                    FileItem(
                        file = file,
                        onClick = {
                            if (file.isDirectory) {
                                currentPath = "$currentPath/${file.name}"
                            }
                        },
                        onLongClick = {
                            // Show context menu
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: FileInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon dựa trên loại file
            Icon(
                imageVector = getFileIcon(file),
                contentDescription = null,
                tint = getFileColor(file),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Row {
                    Text(
                        text = if (file.isDirectory) "Thư mục" else formatFileSize(file.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = file.permissions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Lấy icon phù hợp cho file
 */
fun getFileIcon(file: FileInfo) = when {
    file.isDirectory -> Icons.Default.Folder
    file.name.endsWith(".apk", ignoreCase = true) -> Icons.Default.Android
    file.name.endsWith(".db", ignoreCase = true) -> Icons.Default.Storage
    file.name.endsWith(".xml", ignoreCase = true) -> Icons.Default.Code
    file.name.endsWith(".json", ignoreCase = true) -> Icons.Default.DataObject
    file.name.endsWith(".txt", ignoreCase = true) -> Icons.Default.Description
    file.name.endsWith(".jpg", ignoreCase = true) || 
    file.name.endsWith(".png", ignoreCase = true) -> Icons.Default.Image
    file.name.endsWith(".mp3", ignoreCase = true) ||
    file.name.endsWith(".wav", ignoreCase = true) -> Icons.Default.AudioFile
    file.name.endsWith(".mp4", ignoreCase = true) ||
    file.name.endsWith(".avi", ignoreCase = true) -> Icons.Default.VideoFile
    else -> Icons.Default.InsertDriveFile
}

/**
 * Lấy màu cho file
 */
fun getFileColor(file: FileInfo): Color = when {
    file.isDirectory -> Color(0xFFFFC107)
    file.name.endsWith(".apk", ignoreCase = true) -> Color(0xFF4CAF50)
    file.name.endsWith(".db", ignoreCase = true) -> Color(0xFF2196F3)
    file.name.endsWith(".xml", ignoreCase = true) -> Color(0xFFFF9800)
    file.name.endsWith(".json", ignoreCase = true) -> Color(0xFF9C27B0)
    file.name.endsWith(".txt", ignoreCase = true) -> Color(0xFF607D8B)
    else -> MaterialTheme.colorScheme.onSurface
}

/**
 * Format kích thước file
 */
fun formatFileSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${size / 1024} KB"
    size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
    else -> "${size / (1024 * 1024 * 1024)} GB"
}