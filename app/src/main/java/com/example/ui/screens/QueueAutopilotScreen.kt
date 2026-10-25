package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutopilotConfig
import com.example.data.model.QueuedTask
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.ui.theme.GliseoAutopilotOff
import com.example.ui.theme.GliseoAutopilotOn
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueueAutopilotScreen(
    autopilotConfig: AutopilotConfig,
    tasks: List<QueuedTask>,
    circuitBreakerActive: Boolean,
    onUpdateAutopilot: (AutopilotConfig) -> Unit,
    onEmergencyStop: () -> Unit,
    onResetCircuitBreaker: () -> Unit,
    onApproveReview: (Long) -> Unit,
    onRejectReview: (Long) -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviewTasks = tasks.filter { it.status == TaskStatus.NEEDS_REVIEW }
    val regularTasks = tasks.filter { it.status != TaskStatus.NEEDS_REVIEW }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circuit Breaker Warning if tripped
        if (circuitBreakerActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GliseoError.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = GliseoError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "توقف خودکار اضطراری (نرخ خطا > ۲۰٪)",
                                color = GliseoError,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سیستم به دلیل جلوگیری از اتلاف بودجه و خرابی سایت، اجرای صف را موقتاً متوقف کرده است.",
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onResetCircuitBreaker,
                            colors = ButtonDefaults.buttonColors(containerColor = GliseoError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "رفع توقف و شروع مجدد صف", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Master Autopilot Switch Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (autopilotConfig.isMasterActive) GliseoSuccess else GliseoAutopilotOff,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "کلید اصلی خلبان خودکار",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (autopilotConfig.isMasterActive) "روشن: انتشار خودکار بر اساس ماژول‌ها" else "خاموش: نیازمند تأیید تمام کارها",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = autopilotConfig.isMasterActive,
                            onCheckedChange = { onUpdateAutopilot(autopilotConfig.copy(isMasterActive = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GliseoSuccess
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 10% Sampling Notice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GliseoTertiary.copy(alpha = 0.12f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RateReview, contentDescription = null, tint = GliseoTertiary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "قانون ایمنی: حتی در حالت روشن، ۱۰٪ کارها جهت ارزیابی کیفی تصادفی برای بازبینی انسانی علامت‌گذاری می‌شوند.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "تنظیمات ماژول‌های خودکارسازی:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Module Toggles
                    ModuleToggleRow(
                        title = "تولید متادیتا و عنوان سئو",
                        description = "بهینه‌سازی تایتل و دیسکریپشن بر اساس نتایج گوگل",
                        checked = autopilotConfig.isSeoMetaActive,
                        onCheckedChange = { onUpdateAutopilot(autopilotConfig.copy(isSeoMetaActive = it)) }
                    )
                    ModuleToggleRow(
                        title = "تولید خودکار اسکیما (Schema JSON-LD)",
                        description = "الصاق اسکیمای Article و Product بدون نیاز به کدنویسی",
                        checked = autopilotConfig.isSchemaActive,
                        onCheckedChange = { onUpdateAutopilot(autopilotConfig.copy(isSchemaActive = it)) }
                    )
                    ModuleToggleRow(
                        title = "تولید تگ آلت تصاویر (Image Alt)",
                        description = "نگارش متن توصیفی جایگزین برای عکس‌های محصولات و مقالات",
                        checked = autopilotConfig.isAltTagsActive,
                        onCheckedChange = { onUpdateAutopilot(autopilotConfig.copy(isAltTagsActive = it)) }
                    )
                    ModuleToggleRow(
                        title = "بازنویسی و بهبود متن محتوا",
                        description = "غناسازی پاراگراف‌ها با لحن برند (پیش‌فرض خاموش جهت دقت)",
                        checked = autopilotConfig.isContentPolishActive,
                        onCheckedChange = { onUpdateAutopilot(autopilotConfig.copy(isContentPolishActive = it)) }
                    )
                }
            }
        }

        // Section: 10% Human Review Queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "کارهای نیازمند بازبینی انسانی",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (reviewTasks.isNotEmpty()) GliseoTertiary else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${reviewTasks.size}",
                            color = if (reviewTasks.isNotEmpty()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (reviewTasks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "موردی در انتظار بازبینی نیست. سیستم به صورت خودکار ۱۰٪ کارها را برای شما نمونه‌برداری خواهد کرد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(reviewTasks) { task ->
                HumanReviewTaskCard(
                    task = task,
                    onApprove = { onApproveReview(task.id) },
                    onReject = { onRejectReview(task.id) }
                )
            }
        }

        // Section: General Task Queue
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "صف کارها و محدودیت نرخ (Rate Limit)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onClearCompleted) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "پاکسازی تکمیل‌شده‌ها", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Rate Limit Info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "محدودیت ایمن سرور: ۱۵ درخواست / دقیقه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "تلاش مجدد خودکار: تا ۳ مرتبه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (regularTasks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "صف پردازش خالی است. کارهای جدید با انتخاب ماژول یا ایجاد دستورهای دسته‌ای به اینجا اضافه می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(regularTasks) { task ->
                QueuedTaskCard(task = task)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ModuleToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GliseoPrimary
            )
        )
    }
}

@Composable
fun HumanReviewTaskCard(
    task: QueuedTask,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GliseoTertiary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "نمونه‌برداری ۱۰٪ خلبان خودکار",
                        color = GliseoTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(text = task.taskType.persianLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = task.targetTitle, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task.diffPreview ?: "پیش‌نویس خودکار سئو آماده انتشار",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoSuccess),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تایید و انتشار", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("رد و لغو کار", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun QueuedTaskCard(task: QueuedTask) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(task.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.targetTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = task.taskType.persianLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when (task.status) {
                    TaskStatus.COMPLETED -> GliseoSuccess
                    TaskStatus.RUNNING -> GliseoPrimary
                    TaskStatus.FAILED -> GliseoError
                    TaskStatus.PAUSED -> GliseoTertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(text = task.status.persianLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = dateStr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
