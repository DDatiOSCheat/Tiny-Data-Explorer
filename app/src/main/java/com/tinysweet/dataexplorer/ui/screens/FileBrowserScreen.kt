package com.tinysweet.dataexplorer.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.utils.FileInfo
import com.tinysweet.dataexplorer.utils.RootUtils
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
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
                IconButton(
                    onClick = {
                        val parentPath = currentPath.substringBeforeLast("/")
                        if (parentPath.isNotEmpty()) {
                            currentPath = parentPath
                        }
                    }
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Lên thư mục")
                }
                IconButton(
                    onClick = {
                        scope.launch {
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
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                }
            }
        }

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
            }

            else -> {
                val sortedFiles = files.sortedWith(
                    compareByDescending<FileInfo> { it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sortedFiles, key = { it.name }) { file ->
                        FileItem(
                            file = file,
                            onClick = {
                                if (file.isDirectory) {
                                    currentPath = "$currentPath/${file.name}"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItem(
    file: FileInfo,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = if (file.isDirectory) "Thư mục" else formatBrowserFileSize(file.size),
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

fun getFileIcon(file: FileInfo): ImageVector = when {
    file.isDirectory -> Icons.Default.Folder
    file.name.endsWith(".apk", ignoreCase = true) -> Icons.Default.Android
    file.name.endsWith(".db", ignoreCase = true) -> Icons.Default.Storage
    file.name.endsWith(".xml", ignoreCase = true) -> Icons.Default.Code
    file.name.endsWith(".json", ignoreCase = true) -> Icons.Default.DataObject
    file.name.endsWith(".txt", ignoreCase = true) -> Icons.Default.Description
    file.name.endsWith(".jpg", ignoreCase = true) ||
        file.name.endsWith(".jpeg", ignoreCase = true) ||
        file.name.endsWith(".png", ignoreCase = true) -> Icons.Default.Image
    file.name.endsWith(".mp3", ignoreCase = true) ||
        file.name.endsWith(".wav", ignoreCase = true) -> Icons.Default.AudioFile
    file.name.endsWith(".mp4", ignoreCase = true) ||
        file.name.endsWith(".avi", ignoreCase = true) -> Icons.Default.VideoFile
    else -> Icons.Default.InsertDriveFile
}

@Composable
fun getFileColor(file: FileInfo): Color = when {
    file.isDirectory -> Color(0xFFFFC107)
    file.name.endsWith(".apk", ignoreCase = true) -> Color(0xFF4CAF50)
    file.name.endsWith(".db", ignoreCase = true) -> Color(0xFF2196F3)
    file.name.endsWith(".xml", ignoreCase = true) -> Color(0xFFFF9800)
    file.name.endsWith(".json", ignoreCase = true) -> Color(0xFF9C27B0)
    file.name.endsWith(".txt", ignoreCase = true) -> Color(0xFF607D8B)
    else -> MaterialTheme.colorScheme.onSurface
}

fun formatBrowserFileSize(size: Long): String = when {
    size < 1024L -> "$size B"
    size < 1024L * 1024L -> "${size / 1024L} KB"
    size < 1024L * 1024L * 1024L -> "${size / (1024L * 1024L)} MB"
    else -> "${size / (1024L * 1024L * 1024L)} GB"
}
