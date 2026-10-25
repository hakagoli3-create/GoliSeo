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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrandProfile
import com.example.data.model.ConnectionTestResult
import com.example.data.model.SiteConfig
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun SettingsSecurityScreen(
    siteConfig: SiteConfig,
    userSession: UserSession,
    brandProfile: BrandProfile,
    testingConnection: Boolean,
    connectionResult: ConnectionTestResult?,
    onSaveSiteConfig: (SiteConfig) -> Unit,
    onRunConnectionTest: () -> Unit,
    onSaveBrandProfile: (BrandProfile) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onVerify2FA: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var siteUrl by remember(siteConfig) { mutableStateOf(siteConfig.siteUrl) }
    var wpUsername by remember(siteConfig) { mutableStateOf(siteConfig.wpUsername) }
    var wpAppPassword by remember(siteConfig) { mutableStateOf(siteConfig.wpAppPassword) }
    var wcKey by remember(siteConfig) { mutableStateOf(siteConfig.wcConsumerKey) }
    var wcSecret by remember(siteConfig) { mutableStateOf(siteConfig.wcConsumerSecret) }
    var isDemoMode by remember(siteConfig) { mutableStateOf(siteConfig.isDemoMode) }

    var brandName by remember(brandProfile) { mutableStateOf(brandProfile.brandName) }
    var toneOfVoice by remember(brandProfile) { mutableStateOf(brandProfile.toneOfVoice) }
    var targetAudience by remember(brandProfile) { mutableStateOf(brandProfile.targetAudience) }
    var forbiddenKeywords by remember(brandProfile) { mutableStateOf(brandProfile.forbiddenKeywords) }
    var competitiveAdvantage by remember(brandProfile) { mutableStateOf(brandProfile.competitiveAdvantage) }

    var otpInput by remember { mutableStateOf("") }
    var otpMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "تنظیمات اتصال و امنیت",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "پیکربندی کلیدهای سرور، آزمون اتصال زنده، احراز هویت ۲FA و حافظه برند",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: WordPress & WooCommerce Connection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = GliseoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اتصال به وردپرس و ووکامرس (REST API)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "حالت تست آزمایشی (Demo Sandbox)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "امکان تست تمام امکانات بدون اتصال زنده به سایت", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { isDemoMode = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = GliseoPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = siteUrl,
                        onValueChange = { siteUrl = it },
                        label = { Text("آدرس سایت (Site URL)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = wpUsername,
                        onValueChange = { wpUsername = it },
                        label = { Text("نام کاربری وردپرس (WP Username)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = wpAppPassword,
                        onValueChange = { wpAppPassword = it },
                        label = { Text("رمز عبور برنامه (Application Password)") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = wcKey,
                        onValueChange = { wcKey = it },
                        label = { Text("کلید مصرف‌کننده ووکامرس (Consumer Key)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = wcSecret,
                        onValueChange = { wcSecret = it },
                        label = { Text("رمز امنیتی ووکامرس (Consumer Secret)") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onRunConnectionTest,
                            enabled = !testingConnection,
                            colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (testingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تست اتصال زنده", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onSaveSiteConfig(
                                    SiteConfig(
                                        siteUrl = siteUrl,
                                        wpUsername = wpUsername,
                                        wpAppPassword = wpAppPassword,
                                        wcConsumerKey = wcKey,
                                        wcConsumerSecret = wcSecret,
                                        isDemoMode = isDemoMode
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ذخیره کلیدها", fontSize = 12.sp)
                        }
                    }

                    // Connection Test Results Box
                    if (connectionResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (connectionResult.isSuccess) GliseoSuccess.copy(alpha = 0.1f) else GliseoError.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (connectionResult.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (connectionResult.isSuccess) GliseoSuccess else GliseoError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (connectionResult.isSuccess) "اتصال با موفقیت تأیید شد" else "خطا در اتصال",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (connectionResult.isSuccess) GliseoSuccess else GliseoError
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "پینگ سرور: ${connectionResult.latencyMs} میلی‌ثانیه | نسخه: ${connectionResult.wpVersion} - ${connectionResult.wcVersion}", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                connectionResult.logs.forEach { logLine ->
                                    Text(text = "• $logLine", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Roles & 2FA Security
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GliseoSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "احراز هویت، ۲FA و سطوح دسترسی",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "نقش فعال در برنامه:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = userSession.role == UserRole.ADMIN,
                            onClick = { onSwitchRole(UserRole.ADMIN) },
                            label = { Text("مدیر کل (دسترسی کامل)") }
                        )
                        FilterChip(
                            selected = userSession.role == UserRole.EDITOR,
                            onClick = { onSwitchRole(UserRole.EDITOR) },
                            label = { Text("ویرایشگر (محتوا و سئو)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "ورود دو مرحله‌ای (۲FA):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6) otpInput = it },
                            placeholder = { Text("کد ۶ رقمی OTP...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val ok = onVerify2FA(otpInput)
                                otpMessage = if (ok) "احراز هویت دو مرحله‌ای با موفقیت تأیید شد." else "کد وارد شده معتبر نیست (کد ۶ رقمی وارد کنید)."
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تأیید کد")
                        }
                    }

                    if (otpMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = otpMessage!!,
                            fontSize = 11.sp,
                            color = if (userSession.is2FAVerified) GliseoSuccess else GliseoError
                        )
                    }
                }
            }
        }

        // Section 3: Brand Memory
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = GliseoTertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حافظه و هویت برند (سفارشی‌سازی جمینای)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("نام رسمی برند") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = toneOfVoice,
                        onValueChange = { toneOfVoice = it },
                        label = { Text("لحن بیان (صمیمی، رسمی، راهنما، تخصصی)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = targetAudience,
                        onValueChange = { targetAudience = it },
                        label = { Text("مخاطبان هدف سایت") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = forbiddenKeywords,
                        onValueChange = { forbiddenKeywords = it },
                        label = { Text("کلمات ممنوعه در محتوا و سئو") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = competitiveAdvantage,
                        onValueChange = { competitiveAdvantage = it },
                        label = { Text("مزیت‌های رقابتی برند") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onSaveBrandProfile(
                                BrandProfile(
                                    brandName = brandName,
                                    toneOfVoice = toneOfVoice,
                                    targetAudience = targetAudience,
                                    forbiddenKeywords = forbiddenKeywords,
                                    competitiveAdvantage = competitiveAdvantage
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GliseoTertiary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره حافظه برند", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
