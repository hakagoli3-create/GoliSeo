package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.LogStatus
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsSnapshotsScreen(
    logs: List<AuditLog>,
    onSelectLog: (AuditLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = logs.filter { log ->
        when (selectedFilter) {
            "SUCCESS" -> log.status == LogStatus.SUCCESS
            "LOCKED" -> log.status == LogStatus.BLOCKED_SECURITY
            "UNDONE" -> log.status == LogStatus.UNDONE
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "تاریخچه لاگ‌ها و نسخه‌های پشتیبان",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "ردیابی کامل چه چیزی، کی، چرا و بازگردانی فوری (Undo)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("همه موارد (${logs.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "SUCCESS",
                    onClick = { selectedFilter = "SUCCESS" },
                    label = { Text("عملیات موفق") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "LOCKED",
                    onClick = { selectedFilter = "LOCKED" },
                    label = { Text("مسدود شده با قفل سخت") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "UNDONE",
                    onClick = { selectedFilter = "UNDONE" },
                    label = { Text("بازگردانی شده (Undo)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "هیچ لاگی در این دسته‌بندی یافت نشد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { log ->
                    LogCard(log = log, onClick = { onSelectLog(log) })
                }
            }
        }
    }
}

@Composable
fun LogCard(log: AuditLog, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    val statusColor = when (log.status) {
        LogStatus.SUCCESS -> GliseoSuccess
        LogStatus.BLOCKED_SECURITY -> GliseoHardLock
        LogStatus.UNDONE -> GliseoTertiary
        LogStatus.FAILED -> GliseoError
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (log.status == LogStatus.BLOCKED_SECURITY) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = log.status.persianLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = log.targetTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "علت / پرامپت: ${log.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "کاربر: ${log.authorRole} • اقدام: ${log.actionName}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.priorSnapshotId != null && !log.isUndone) {
                        Text(
                            text = "مشاهده تفاوت و Undo ❯",
                            color = GliseoPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "مشاهده جزئیات ❯",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
