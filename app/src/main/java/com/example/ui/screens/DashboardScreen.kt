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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditLog
import com.example.data.model.AutopilotConfig
import com.example.data.model.BudgetTracker
import com.example.data.model.LogStatus
import com.example.data.model.SiteConfig
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.TaskStatus
import com.example.ui.AppNavDestination
import com.example.ui.theme.GliseoAutopilotOff
import com.example.ui.theme.GliseoAutopilotOn
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    items: List<SiteItem>,
    siteConfig: SiteConfig,
    autopilotConfig: AutopilotConfig,
    budgetTracker: BudgetTracker,
    auditLogs: List<AuditLog>,
    circuitBreakerActive: Boolean,
    onNavigate: (AppNavDestination) -> Unit,
    onTestConnection: () -> Unit,
    onTestHardLock: () -> Unit,
    onViewLog: (AuditLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val productCount = items.count { it.type == SiteItemType.PRODUCT }
    val postCount = items.count { it.type != SiteItemType.PRODUCT }
    val pendingReviewCount = auditLogs.count { it.status == LogStatus.BLOCKED_SECURITY }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Site Connection Status Banner
        item {
            SiteHealthBanner(
                siteConfig = siteConfig,
                onTestConnection = onTestConnection,
                onConfigure = { onNavigate(AppNavDestination.SETTINGS_SECURITY) }
            )
        }

        // Hard Lock Assurance Banner
        item {
            HardLockAssuranceCard(onTestHardLock = onTestHardLock)
        }

        // Metrics Grid (2x2)
        item {
            Text(
                text = "شاخص‌های کلیدی سایت",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "محصولات ووکامرس",
                    value = "$productCount مورد",
                    subtitle = "با قیمت‌های قفل‌شده",
                    icon = Icons.Default.ShoppingBag,
                    accentColor = GliseoSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppNavDestination.CONTENT_STUDIO) }
                )
                MetricCard(
                    title = "نوشته‌ها و برگه‌ها",
                    value = "$postCount مورد",
                    subtitle = "بهینه‌سازی سئو آزاد",
                    icon = Icons.Default.Inventory2,
                    accentColor = GliseoPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppNavDestination.CONTENT_STUDIO) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "خلبان خودکار",
                    value = if (autopilotConfig.isMasterActive) "روشن" else "خاموش",
                    subtitle = "نمونه‌برداری ۱۰٪ فعال",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = if (autopilotConfig.isMasterActive) GliseoSuccess else GliseoAutopilotOff,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppNavDestination.QUEUE_AUTOPILOT) }
                )
                MetricCard(
                    title = "لاگ و نسخه‌ها",
                    value = "${auditLogs.size} نسخه",
                    subtitle = "قابلیت Undo فوری",
                    icon = Icons.Default.PlaylistAddCheck,
                    accentColor = GliseoTertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppNavDestination.LOGS_SNAPSHOTS) }
                )
            }
        }

        // API Budget & Consumption Card
        item {
            ApiBudgetCard(budgetTracker = budgetTracker)
        }

        // Recent Audit Log Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین لاگ‌ها و نسخه‌های ذخیره‌شده",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "مشاهده همه",
                    color = GliseoPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate(AppNavDestination.LOGS_SNAPSHOTS) }
                )
            }
        }

        if (auditLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "هنوز تغییری ثبت نشده است. با ویرایش هر محتوا یا اجرای صف، نسخه قبل به صورت خودکار پشتیبان‌گیری می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(auditLogs.take(4)) { log ->
                RecentLogItem(log = log, onClick = { onViewLog(log) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SiteHealthBanner(
    siteConfig: SiteConfig,
    onTestConnection: () -> Unit,
    onConfigure: () -> Unit
) {
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GliseoSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سایت متصل: ${siteConfig.siteUrl}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (siteConfig.isDemoMode) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GliseoSecondary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "محیط تست فعال", color = GliseoSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "WordPress REST + WooCommerce v3 آماده پردازش و ممیزی سئو با جمینای.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onTestConnection,
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تست اتصال زنده", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onConfigure,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "تنظیمات کلیدها", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun HardLockAssuranceCard(onTestHardLock: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GliseoHardLock.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "قفل سخت",
                    tint = GliseoHardLock,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سیاست قفل‌های سخت (غیرقابل‌دورزدن در سرور و کد)",
                    color = GliseoHardLock,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "تغییر قیمت (regular_price, sale_price)، اسلاگ/URL و دسته‌بندی‌ها تحت هر شرایطی بلاک می‌شوند. سایر فیلدها (سئو، اسکیما، عنوان و محتوا) آزاد هستند.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "آزمایش نفوذ به قفل سخت ❯",
                    color = GliseoHardLock,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onTestHardLock() }
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = accentColor)
        }
    }
}

@Composable
fun ApiBudgetCard(budgetTracker: BudgetTracker) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = GliseoTertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "سقف بودجه و مصرف روزانه API", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
                Text(
                    text = "$${budgetTracker.currentSpentUsd} از $${budgetTracker.dailyBudgetCapUsd}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { budgetTracker.usagePercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (budgetTracker.usagePercentage > 0.8f) GliseoError else GliseoPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "توکن‌های مصرفی امروز: ${budgetTracker.tokensUsedToday} توکن",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (budgetTracker.isExceeded) "سقف مجاز پر شد" else "وضعیت مصرف: عادی",
                    fontSize = 10.sp,
                    color = if (budgetTracker.isExceeded) GliseoError else GliseoSuccess,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RecentLogItem(log: AuditLog, onClick: () -> Unit) {
    val dateStr = SimpleDateFormat("HH:mm - MM/dd", Locale.getDefault()).format(Date(log.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (log.status) {
                        LogStatus.SUCCESS -> GliseoSuccess
                        LogStatus.BLOCKED_SECURITY -> GliseoHardLock
                        LogStatus.UNDONE -> GliseoTertiary
                        LogStatus.FAILED -> GliseoError
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.targetTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${log.actionName} • ${log.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = log.status.persianLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = dateStr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
