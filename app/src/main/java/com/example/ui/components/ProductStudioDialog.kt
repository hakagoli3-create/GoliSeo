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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductOptimizationPackage
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun ProductStudioDialog(
    pkg: ProductOptimizationPackage,
    onApplySingle: () -> Unit,
    onBatchAllProducts: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = GliseoSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ماژول ۲: استودیو ووکامرس و سئو Rank Math",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hard lock reminder
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GliseoHardLock.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قفل سخت فعال: قیمت، اسلاگ و دسته‌بندی مسدود و دست‌نخورده باقی می‌مانند.",
                                color = GliseoHardLock,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Circuit Breaker Halt Banner if identity is unverified or contradictory
                if (pkg.isGenerationHalted) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GliseoHardLock.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "⛔ فرآیند تولید سئو متوقف شد (مدارشکن احراز هویت)",
                                        color = GliseoHardLock,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pkg.haltReasonPersian ?: "اطلاعات حیاتی کالا قابل احراز نیست یا تناقض ساختاری وجود دارد.",
                                    fontSize = 11.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Three-Bucket Verification Report (تأییدشده، متناقض، نامشخص)
                pkg.verificationReport?.let { report ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🔍 گزارش تطبیق با کاتالوگ‌های رسمی و اعتبارسنجی:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GliseoPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // 1. Verified Claims
                                if (report.verifiedClaims.isNotEmpty()) {
                                    Text(text = "🟢 اطلاعات تأییدشده:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoSuccess)
                                    report.verifiedClaims.forEach { claim ->
                                        Text(text = "• ${claim.field}: ${claim.value} [مرجع: ${claim.sourceName}]", fontSize = 9.sp, lineHeight = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // 2. Contradictory Claims
                                if (report.contradictoryClaims.isNotEmpty()) {
                                    Text(text = "🔴 اطلاعات متناقض و مسدودشده:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoHardLock)
                                    report.contradictoryClaims.forEach { claim ->
                                        Text(text = "• ${claim.field}: ${claim.conflictingClaim ?: claim.value}", fontSize = 9.sp, color = GliseoHardLock, lineHeight = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // 3. Unverified Claims
                                if (report.unverifiedClaims.isNotEmpty()) {
                                    Text(text = "🟡 اطلاعات نامشخص / نیازمند استعلام:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GliseoTertiary)
                                    report.unverifiedClaims.forEach { claim ->
                                        Text(text = "• ${claim.field}: ${claim.value} (${claim.evidenceSnippet})", fontSize = 9.sp, color = GliseoTertiary, lineHeight = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // 4. Authoritative Sources
                                if (report.authoritativeSources.isNotEmpty()) {
                                    Text(text = "🌐 مراجع رسمی تطبیق داده شده:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    report.authoritativeSources.forEach { src ->
                                        Text(text = "• $src", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // Technical Dependencies Note
                                if (report.technicalDependenciesNote.isNotBlank()) {
                                    Text(
                                        text = "⚙️ ${report.technicalDependenciesNote}",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Step 1: Pre-Writing Product Analysis & Brand Identity Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (pkg.isOfficialAgencyBrand) GliseoSuccess.copy(alpha = 0.08f) else GliseoPrimary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (pkg.isOfficialAgencyBrand) GliseoSuccess else GliseoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "۱. تحلیل پیش از نگارش و تشخیص هویت کالا:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pkg.isOfficialAgencyBrand) GliseoSuccess else GliseoPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "• برند سازنده اصلی: ${pkg.detectedBrand.ifBlank { "تشخیص داده شده" }} (گلی کالا نماینده و واردکننده است، نه سازنده)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "• کشور مبدا و استاندارد: ${pkg.originCountry.ifBlank { "اسپانیا / اتحادیه اروپا" }}", fontSize = 10.sp)
                            Text(text = "• نوع محصول: ${pkg.productType.ifBlank { "کالای مراقبتی بهداشتی" }}", fontSize = 10.sp)
                            if (pkg.isOfficialAgencyBrand) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GliseoSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "✦ گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GliseoSuccess
                                    )
                                }
                            }
                        }
                    }
                }

                // SEO Title & Focus Keyword
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "عنوان سئو پیشنهادی (~۶۰ کاراکتر):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = pkg.seoTitle, fontSize = 11.sp, color = GliseoPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "کلمه کلیدی کانونی: ${pkg.focusKeyword}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Rank Math Meta Description
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "توضیحات متا Rank Math (~۱۶۰ کاراکتر):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = pkg.rankMathMetaDesc, fontSize = 11.sp)
                        }
                    }
                }

                // Short Description & Features
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "توضیح کوتاه و جذاب:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = pkg.shortDescription, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "ویژگی‌های تاییدشده:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            pkg.features.forEach { feat ->
                                Text(text = "• $feat", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Specifications Table
                if (pkg.specificationsTable.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "جدول مشخصات و مقایسه فنی کالا:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                pkg.specificationsTable.forEach { (specKey, specVal) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "• $specKey:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = specVal, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }

                // Internal Links Section
                if (pkg.internalLinks.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "لینک‌سازی داخلی هوشمند و محصولات مرتبط:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                pkg.internalLinks.forEach { link ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🔗 ${link.anchorText}", fontSize = 10.sp, color = GliseoPrimary, fontWeight = FontWeight.Medium)
                                        Text(text = "${link.relevanceScore}٪", fontSize = 9.sp, color = GliseoSuccess, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }


                // FAQs
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "پرسش و پاسخ‌های متداول خریداران (FAQ):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            pkg.faqs.forEach { (q, a) ->
                                Text(text = "سؤال: $q", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "پاسخ: $a", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Truthfulness & Safety Notice
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GliseoTertiary.copy(alpha = 0.1f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(text = "راستی‌آزمایی منبع: ${pkg.verifiedSource}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "هشدار بهداشتی: ${pkg.safetyWarning}", fontSize = 9.sp, color = GliseoTertiary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApplySingle,
                enabled = !pkg.isGenerationHalted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pkg.isGenerationHalted) MaterialTheme.colorScheme.surfaceVariant else GliseoPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (pkg.isGenerationHalted) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (pkg.isGenerationHalted) "تولید متوقف است (عدم احراز هویت)" else "اعمال روی این محصول")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onBatchAllProducts,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Queue, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("بهینه‌سازی دسته‌ای تمام محصولات")
            }
        }
    )
}
