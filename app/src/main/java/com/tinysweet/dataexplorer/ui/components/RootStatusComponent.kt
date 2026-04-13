package com.tinysweet.dataexplorer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tinysweet.dataexplorer.ui.theme.APatchColor
import com.tinysweet.dataexplorer.ui.theme.ErrorColor
import com.tinysweet.dataexplorer.ui.theme.KernelSUColor
import com.tinysweet.dataexplorer.ui.theme.MagiskColor
import com.tinysweet.dataexplorer.ui.theme.NotRootedColor
import com.tinysweet.dataexplorer.ui.theme.RootedColor
import com.tinysweet.dataexplorer.ui.theme.SuccessColor

/**
 * RootStatusComponent - Hiển thị trạng thái root
 */
@Composable
fun RootStatusComponent(
    isRooted: Boolean,
    rootType: RootType = RootType.Unknown,
    modifier: Modifier = Modifier
) {
    val status = getRootStatusUi(isRooted = isRooted, rootType = rootType)

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
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
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
                        text = "Type: ${status.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = status.color
                    )
                }
            }

            Icon(
                imageVector = if (isRooted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isRooted) SuccessColor else ErrorColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getRootStatusUi(isRooted: Boolean, rootType: RootType): RootStatusUi {
    if (!isRooted) {
        return RootStatusUi(
            icon = Icons.Default.Warning,
            color = NotRootedColor,
            label = "Not Rooted"
        )
    }

    return when (rootType) {
        RootType.Magisk -> RootStatusUi(Icons.Default.Security, MagiskColor, "Magisk")
        RootType.KernelSU -> RootStatusUi(Icons.Default.Security, KernelSUColor, "KernelSU")
        RootType.APatch -> RootStatusUi(Icons.Default.Security, APatchColor, "APatch")
        RootType.Unknown -> RootStatusUi(Icons.Default.Security, RootedColor, "Rooted")
    }
}

private data class RootStatusUi(
    val icon: ImageVector,
    val color: Color,
    val label: String
)

/**
 * Loại root
 */
enum class RootType {
    Unknown,
    Magisk,
    KernelSU,
    APatch
}
