package com.example.rootforgedataexplorer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rootforgedataexplorer.ui.components.LoadingScreen
import com.example.rootforgedataexplorer.ui.utils.icons

/**
 * ToolsScreen - Công cụ nâng cao
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Công cụ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Refresh */ }) {
                Icon(icons.Refresh, contentDescription = "Làm mới")
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxWidth())
        
        // Tools Grid
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(toolsList) { tool ->
                ToolCard(tool = tool)
            }
        }
    }
}

private val toolsList = listOf(
    ToolItem(
        title = "SQLite Explorer",
        description = "Khám phá và chỉnh sửa cơ sở dữ liệu SQLite",
        icon = icons.Database,
        onClick = { /* Navigate to SQLite Explorer */ }
    ),
    ToolItem(
        title = "SharedPreferences Editor",
        description = "Chỉnh sửa file cấu hình SharedPreferences",
        icon = icons.Settings,
        onClick = { /* Navigate to SharedPreferences Editor */ }
    ),
    ToolItem(
        title = "Backup & Restore",
        description = "Sao lưu và khôi phục dữ liệu ứng dụng",
        icon = icons.Backup,
        onClick = { /* Navigate to Backup/Restore */ }
    ),
    ToolItem(
        title = "Hex Viewer",
        description = "Xem file nhị phân dưới dạng hexadecimal",
        icon = icons.Code,
        onClick = { /* Navigate to Hex Viewer */ }
    ),
    ToolItem(
        title = "Text Editor",
        description = "Chỉnh sửa file văn bản",
        icon = icons.Edit,
        onClick = { /* Navigate to Text Editor */ }
    ),
    ToolItem(
        title = "Recent Apps",
        description = "Lịch sử truy cập ứng dụng gần đây",
        icon = icons.History,
        onClick = { /* Navigate to Recent Apps */ }
    ),
    ToolItem(
        title = "Bookmarks",
        description = "Thư mục được đánh dấu thường dùng",
        icon = icons.Bookmark,
        onClick = { /* Navigate to Bookmarks */ }
    ),
    ToolItem(
        title = "Storage Analyzer",
        description = "Phân tích dung lượng sử dụng",
        icon = icons.Storage,
        onClick = { /* Navigate to Storage Analyzer */ }
    )
)

@Composable
fun ToolCard(tool: ToolItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { tool.onClick() }),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tool.description,
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

data class ToolItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)