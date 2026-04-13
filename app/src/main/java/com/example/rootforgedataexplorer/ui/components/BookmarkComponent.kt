package com.example.rootforgedataexplorer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * BookmarkComponent - Hiển thị các thư mục được bookmark
 */
@Composable
fun BookmarkComponent(
    bookmarks: List<BookmarkItem>,
    onBookmarkClick: (BookmarkItem) -> Unit,
    onAddBookmark: () -> Unit
) {
    if (bookmarks.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đánh dấu thư mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddBookmark) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm bookmark")
                }
            }
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarks.take(5)) { bookmark ->
                    BookmarkItem(
                        bookmark = bookmark,
                        onClick = { onBookmarkClick(bookmark) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkItem(
    bookmark: BookmarkItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.layout.Text(
                text = bookmark.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            androidx.compose.foundation.layout.Text(
                text = bookmark.path,
                style = MaterialTheme.typography.caption,
                maxLines = 1
            )
        }
    }
}

/**
 * Bookmark item data class
 */
data class BookmarkItem(
    val id: String,
    val name: String,
    val path: String,
    val timestamp: Long
)