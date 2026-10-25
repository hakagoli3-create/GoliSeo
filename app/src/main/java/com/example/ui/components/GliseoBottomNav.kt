package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavDestination
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoTertiary

data class NavItemSpec(
    val destination: AppNavDestination,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun GliseoBottomNav(
    currentDestination: AppNavDestination,
    pendingReviewsCount: Int,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemSpec(AppNavDestination.DASHBOARD, Icons.Default.Dashboard),
        NavItemSpec(AppNavDestination.CONTENT_STUDIO, Icons.Default.EditNote),
        NavItemSpec(AppNavDestination.QUEUE_AUTOPILOT, Icons.Default.AutoAwesome, pendingReviewsCount),
        NavItemSpec(AppNavDestination.LOGS_SNAPSHOTS, Icons.Default.History),
        NavItemSpec(AppNavDestination.SETTINGS_SECURITY, Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentDestination == item.destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.destination) },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = GliseoTertiary) {
                                Text(item.badgeCount.toString(), fontSize = 10.sp)
                            }
                        }) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.destination.persianLabel,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.destination.persianLabel,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.destination.persianLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GliseoPrimary,
                    selectedTextColor = GliseoPrimary,
                    indicatorColor = GliseoPrimary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
