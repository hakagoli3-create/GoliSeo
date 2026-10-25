package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.DiffItem
import com.example.data.model.LogStatus
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiffViewerDialog(
    log: AuditLog,
    onDismiss: () -> Unit,
    onUndo: (AuditLog) -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جزئیات تغییرات و نسخه",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = log.targetTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Metadata Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "اقدام: ${log.actionName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "کاربر: ${log.authorRole}", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "علت / پرامپت: ${log.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "زمان: $dateStr", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تفاوت نسخه (قبل / بعد):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    if (log.diffs.isEmpty()) {
                        item {
                            Text(
                                text = "تغییر محتوایی ثبت نشده است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(log.diffs) { diff ->
                            DiffCard(diff)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (log.priorSnapshotId != null && !log.isUndone) {
                Button(
                    onClick = { onUndo(log) },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "بازگردانی به این نسخه (Undo)")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "بستن")
            }
        }
    )
}

@Composable
fun DiffCard(diff: DiffItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (diff.isLockedViolation) GliseoHardLock.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = diff.fieldName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (diff.isLockedViolation) GliseoHardLock else MaterialTheme.colorScheme.onSurface
                )
                if (diff.isLockedViolation) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "قفل سخت امنیتی", color = GliseoHardLock, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Before
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(GliseoError.copy(alpha = 0.1f))
                    .padding(6.dp)
            ) {
                Text(
                    text = "قبل: ${diff.beforeValue}",
                    color = GliseoError,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // After
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(GliseoSuccess.copy(alpha = 0.1f))
                    .padding(6.dp)
            ) {
                Text(
                    text = "بعد: ${diff.afterValue}",
                    color = GliseoSuccess,
                    fontSize = 11.sp
                )
            }
        }
    }
}
