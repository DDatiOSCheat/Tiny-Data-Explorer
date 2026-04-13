package com.example.rootforgedataexplorer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rootforgedataexplorer.ui.theme.*

/**
 * RootStatusComponent - Hiển thị trạng thái root
 */
@Composable
fun RootStatusComponent(
    isRooted: Boolean,
    rootType: RootType = RootType.Unknown,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color, text) = when {
                isRooted -> {
                    when (rootType) {
                        RootType.Magisk -> Triple(Icons.Default.Shield, MagiskColor, "Magisk")
                        RootType.KernelSU -> Triple(Icons.Default.Shield, KernelSUColor, "KernelSU")
                        RootType.APatch -> Triple(Icons.Default.Shield, APatchColor, "APatch")
                        else -> Triple(Icons.Default.Shield, RootedColor, "Rooted")
                    }
                }
                else -> Triple(Icons.Default.Warning, NotRootedColor, "Not Rooted")
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRooted) "Root Access" else "No Root Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (isRooted && rootType != RootType.Unknown) {
                    Text(
                        text = "Type: $text",
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
            
            if (isRooted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = ErrorColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Loại root
 */
enum class RootType {
    Unknown,
    Magisk,
    KernelSU,
    APatch
}