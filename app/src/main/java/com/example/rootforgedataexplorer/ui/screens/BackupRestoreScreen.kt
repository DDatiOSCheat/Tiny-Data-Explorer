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
import com.example.rootforgedataexplorer.utils.BackupInfo
import com.example.rootforgedataexplorer.utils.RootUtils
import com.example.rootforgedataexplorer.ui.utils.Icons
import kotlinx.coroutines.launch
import java.io.File

/**
 * BackupRestoreScreen - Sao lưu và khôi phục dữ liệu ứng dụng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    modifier: Modifier = Modifier,
    appPackageName: String,
    onBack: () -> Unit
) {
    var backups by remember { mutableStateOf<List<BackupInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<BackupInfo?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Load existing backups
    LaunchedEffect(appPackageName) {
        isLoading = true
        errorMessage = null
        try {
            backups = loadBackups(context, appPackageName)
        } catch (e: Exception) {
            errorMessage = "Không thể tải bản sao lưu: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBackupDialog = true },
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                text = { Text("Sao lưu") }
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
                if (backups.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có bản sao lưu nào",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Nhấn nút + để tạo bản sao lưu mới",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backups, key = { it.fileName }) { backup ->
                            BackupItem(
                                backup = backup,
                                onRestore = {
                                    selectedBackup = backup
                                    showRestoreDialog = true
                                },
                                onDelete = {
                                    // Delete backup
                                    scope.launch {
                                        deleteBackup(context, backup)
                                        backups = loadBackups(context, appPackageName)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            appPackageName = appPackageName,
            onBackup = { backupName ->
                scope.launch {
                    isLoading = true
                    try {
                        createBackup(context, appPackageName, backupName)
                        backups = loadBackups(context, appPackageName)
                        showBackupDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Sao lưu thất bại: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            onDismiss = { showBackupDialog = false }
        )
    }
    
    // Restore Dialog
    if (showRestoreDialog && selectedBackup != null) {
        RestoreDialog(
            backup = selectedBackup!!,
            appPackageName = appPackageName,
            onRestore = {
                scope.launch {
                    isLoading = true
                    try {
                        restoreBackup(context, appPackageName, selectedBackup!!)
                        showRestoreDialog = false
                    } catch (e: Exception) {
                        errorMessage = "Khôi phục thất bại: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            onDismiss = { showRestoreDialog = false }
        )
    }
}

@Composable
fun BackupItem(
    backup: BackupInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = backup.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ngày tạo: ${backup.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Kích thước: ${formatFileSize(backup.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Khôi phục")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                }
            }
        }
    }
}

@Composable
fun BackupDialog(
    appPackageName: String,
    onBackup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var backupName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo bản sao lưu") },
        text = {
            Column {
                Text(
                    text = "Đặt tên cho bản sao lưu:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("VD: backup_2024_01_01") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (backupName.isNotBlank()) {
                        onBackup(backupName)
                    }
                }
            ) {
                Text("Sao lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun RestoreDialog(
    backup: BackupInfo,
    appPackageName: String,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xác nhận khôi phục") },
        text = {
            Column {
                Text(
                    text = "Bạn có chắc chắn muốn khôi phục dữ liệu từ bản sao lưu '${backup.name}'?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cảnh báo: Dữ liệu hiện tại sẽ bị ghi đè!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onRestore,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Khôi phục")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

/**
 * Tạo bản sao lưu dữ liệu ứng dụng
 */
suspend fun createBackup(
    context: Context,
    appPackageName: String,
    backupName: String
): Boolean {
    val dataPath = "/data/data/$appPackageName"
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")
    if (!backupDir.exists()) {
        backupDir.mkdirs()
    }
    
    val backupFile = File(backupDir, "$backupName.zip")
    
    // Sử dụng root để nén thư mục data
    val success = RootUtils.zipDirectory(dataPath, backupFile.absolutePath)
    
    if (success) {
        // Lưu thông tin backup
        val infoFile = File(backupDir, "$backupName.info")
        infoFile.writeText(
            "name=$backupName\n" +
            "date=${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n" +
            "package=$appPackageName\n" +
            "size=${backupFile.length()}"
        )
    }
    
    return success
}

/**
 * Khôi phục dữ liệu từ bản sao lưu
 */
suspend fun restoreBackup(
    context: Context,
    appPackageName: String,
    backup: BackupInfo
): Boolean {
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")
    val backupFile = File(backupDir, backup.fileName)
    val dataPath = "/data/data/$appPackageName"
    
    // Xóa dữ liệu cũ
    RootUtils.deleteFile(dataPath)
    RootUtils.createDirectory(dataPath)
    
    // Giải nén backup
    return RootUtils.unzipFile(backupFile.absolutePath, dataPath)
}

/**
 * Tải danh sách bản sao lưu
 */
fun loadBackups(context: Context, appPackageName: String): List<BackupInfo> {
    val backups = mutableListOf<BackupInfo>()
    val backupDir = File(context.getExternalFilesDir(null), "backups/$appPackageName")
    
    if (backupDir.exists()) {
        backupDir.listFiles { file -> file.extension == "zip" }?.forEach { zipFile ->
            val infoFile = File(backupDir, "${zipFile.nameWithoutExtension}.info")
            if (infoFile.exists()) {
                val info = infoFile.readLines().associate { line ->
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) parts[0] to parts[1] else "" to ""
                }
                
                backups.add(
                    BackupInfo(
                        name = info["name"] ?: zipFile.nameWithoutExtension,
                        fileName = zipFile.name,
                        date = info["date"] ?: "Unknown",
                        size = info["size"]?.toLongOrNull() ?: zipFile.length(),
                        package_name = appPackageName
                    )
                )
            }
        }
    }
    
    return backups.sortedByDescending { it.date }
}

/**
 * Xóa bản sao lưu
 */
suspend fun deleteBackup(context: Context, backup: BackupInfo): Boolean {
    val backupDir = File(context.getExternalFilesDir(null), "backups/${backup.package_name}")
    val zipFile = File(backupDir, backup.fileName)
    val infoFile = File(backupDir, "${backup.fileName.substringBeforeLast(".")}.info")
    
    return zipFile.delete() && infoFile.delete()
}

/**
 * Thông tin bản sao lưu
 */
data class BackupInfo(
    val name: String,
    val fileName: String,
    val date: String,
    val size: Long,
    val package_name: String
)