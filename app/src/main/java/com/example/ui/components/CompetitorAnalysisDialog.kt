package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompetitorAnalysisReport
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun CompetitorAnalysisDialog(
    report: CompetitorAnalysisReport,
    onApplyWinningStrategy: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: رقبا در گوگل, 1: خلاءهای محتوایی, 2: سرفصل‌ها و LSI, 3: لینک‌های داخلی

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Insights, contentDescription = null, tint = GliseoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تحلیل رقبا در گوگل (Google SERP Intelligence)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "کلیدواژه: ${report.targetKeyword} • قصد کاربر: ${report.searchIntent}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = GliseoPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("۳ رقیب برتر", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("خلاءهای محتوا", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("LSI و H2/H3", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("لینک داخلی", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Top Competitors Tab
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "میانگین حجم محتوای رقبا:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "${report.averageCompetitorWordCount} کلمه", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "امتیاز رقابت‌پذیری:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "${report.overallCompetitivenessScore} از ۱۰۰", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GliseoSuccess)
                                        }
                                    }
                                }
                            }

                            items(report.topCompetitors) { comp ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (comp.rank == 1) GliseoTertiary else GliseoPrimary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "${comp.rank}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = comp.siteName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = "(${comp.domain})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(text = "~${comp.wordCountEstimate} کلمه", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = comp.pageTitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = GliseoPrimary)

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "نقاط قوت رقیب:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoSuccess)
                                        comp.keyStrengths.forEach { str ->
                                            Text(text = "  ✔ $str", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "نقاط ضعف و آسیب‌پذیری رقیب (فرصت ما):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoError)
                                        comp.vulnerabilities.forEach { vul ->
                                            Text(text = "  ⚠️ $vul", fontSize = 10.sp, color = GliseoError)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Content Gaps Tab
                            item {
                                Text(
                                    text = "خلاءهای محتوایی رقبای رتبه ۱ تا ۳ که با پوشش آنها رتبه اول گوگل را کسب می‌کنید:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(report.contentGaps) { gap ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = gap.gapTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GliseoPrimary)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(GliseoTertiary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = "اهمیت: ${gap.importance}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GliseoTertiary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = gap.description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text(text = "راهکار گلیسئو: ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoSuccess)
                                            Text(text = gap.recommendation, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // LSI & Heading Structure Tab
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "کلمات کلیدی معنایی LSI استخراج‌شده از نتایج گوگل:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        report.suggestedLsiKeywords.forEach { lsi ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(text = lsi, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "سلسله‌مراتب سرفصل‌های برنده (H2 / H3):", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        report.recommendedH2Headings.forEachIndexed { idx, h2 ->
                                            Text(text = "${idx + 1}. H2: $h2", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = GliseoPrimary)
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // Internal Links Tab
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.08f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "لینک‌سازی داخلی هوشمند جهت کاهش بانس‌ریت و چرخش کاربر:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "این لینک‌ها به صورت استاندارد در انتهای محتوا قرار می‌گیرند تا سئوی داخلی سایت را به شدت تقویت نمایند.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            items(report.suggestedInternalLinks) { link ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = link.anchorText, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GliseoPrimary)
                                            Text(text = "${link.relevanceScore}٪ تطابق", fontSize = 10.sp, color = GliseoSuccess, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "آدرس مقصد: ${link.targetUrl}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "نوع هدف: ${link.contentType}", fontSize = 9.sp, color = GliseoTertiary)
                                    }
                                }
                            }
                        }
                    }

                    // Winning Strategy Summary Box
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GliseoSuccess.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = GliseoSuccess, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "راهکار برنده گلیسئو برای کسب رتبه ۱ گوگل:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GliseoSuccess)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = report.winningStrategy, fontSize = 10.sp, lineHeight = 18.sp, color = Color(0xFF14532D))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApplyWinningStrategy,
                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("اعمال استراتژی پیروزی در محتوا", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("بستن", fontSize = 11.sp)
            }
        }
    )
}
