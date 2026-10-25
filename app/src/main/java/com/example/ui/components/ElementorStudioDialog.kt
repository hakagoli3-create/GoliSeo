package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrandKnowledgeBase
import com.example.data.model.ElementorPageDesign
import com.example.data.model.ElementorPageType
import com.example.data.model.ElementorSectionDesign
import com.example.data.model.ElementorTemplateBuilder
import com.example.data.model.SiteItem
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun ElementorStudioDialog(
    item: SiteItem?,
    brandKnowledge: BrandKnowledgeBase,
    onApplyToItem: (SiteItem, ElementorPageDesign) -> Unit,
    onCreateNewPage: (ElementorPageDesign) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTitle = item?.title ?: "برگه لندینگ جدید"
    var pageTitle by remember { mutableStateOf(initialTitle) }
    var selectedType by remember {
        mutableStateOf(
            when {
                initialTitle.contains("درباره") -> ElementorPageType.ABOUT_US
                initialTitle.contains("تماس") -> ElementorPageType.CONTACT_SUPPORT
                initialTitle.contains("تخفیف") || initialTitle.contains("جشنواره") -> ElementorPageType.PROMO_CAMPAIGN
                item?.isProduct == true -> ElementorPageType.PRODUCT_LANDING
                else -> ElementorPageType.HOME_BRANDING
            }
        )
    }

    var currentDesign by remember {
        mutableStateOf(
            ElementorTemplateBuilder.generateDesignForPage(pageTitle, selectedType, brandKnowledge)
        )
    }

    var selectedPreviewTab by remember { mutableIntStateOf(0) } // 0: Visual Mockup, 1: Elementor JSON, 2: HTML, 3: Rank Math

    fun regenerate() {
        currentDesign = ElementorTemplateBuilder.generateDesignForPage(pageTitle, selectedType, brandKnowledge)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GliseoPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewQuilt,
                            contentDescription = null,
                            tint = GliseoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "استودیو طراحی هوشمند برگه با المنتور (Elementor)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "تولید ساختار استاندارد Elementor Pro، سئو Rank Math و لندینگ مدرن",
                            style = MaterialTheme.typography.bodySmall,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Page Title & Redesign Controls
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pageTitle,
                                onValueChange = {
                                    pageTitle = it
                                },
                                label = { Text("عنوان برگه یا لندینگ پیج") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "سبک و هدف طراحی:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(ElementorPageType.values().toList()) { type ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = {
                                            selectedType = type
                                            regenerate()
                                        },
                                        label = { Text(type.persianTitle, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Button(
                                onClick = { regenerate() },
                                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بازطراحی خودکار برگه با هوش مصنوعی و المنتور", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Tabs for Preview Modes
                item {
                    TabRow(
                        selectedTabIndex = selectedPreviewTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = GliseoPrimary
                    ) {
                        Tab(
                            selected = selectedPreviewTab == 0,
                            onClick = { selectedPreviewTab = 0 },
                            text = { Text("پیش‌نمایش بصری", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedPreviewTab == 1,
                            onClick = { selectedPreviewTab = 1 },
                            text = { Text("المنتور JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedPreviewTab == 2,
                            onClick = { selectedPreviewTab = 2 },
                            text = { Text("قالب HTML", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedPreviewTab == 3,
                            onClick = { selectedPreviewTab = 3 },
                            text = { Text("سئو Rank Math", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                // Tab Content
                when (selectedPreviewTab) {
                    0 -> {
                        // Visual Mockup of sections
                        items(currentDesign.sections) { sec ->
                            ElementorSectionCard(sec)
                        }
                    }
                    1 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "ساختار معتبر _elementor_data (آماده وردپرس):", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${currentDesign.elementorJson.length} کاراکتر", color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentDesign.elementorJson.take(800) + if (currentDesign.elementorJson.length > 800) "\n\n... (ادامه ساختار ویجت‌های المنتور شامل بخش‌ها، ستون‌ها و استایل‌ها)" else "",
                                        color = Color(0xFFE2E8F0),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "کد HTML واکنش‌گرا و سازگار با وردپرس و گوتنبرگ:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentDesign.semanticHtml.take(900) + if (currentDesign.semanticHtml.length > 900) "..." else "",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "پیکربندی افزونه Rank Math برای این برگه:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = "پیش‌نمایش در نتایج جستجوی گوگل:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = currentDesign.recommendedRankMathTitle, fontSize = 12.sp, color = GliseoPrimary, fontWeight = FontWeight.Bold)
                                            Text(text = "https://your-site.com/${pageTitle.replace(" ", "-")}", fontSize = 10.sp, color = GliseoSuccess)
                                            Text(text = currentDesign.recommendedMetaDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (item != null) {
                Button(
                    onClick = {
                        onApplyToItem(item, currentDesign)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اعمال و ارسال مستقیم به المنتور این برگه")
                }
            } else {
                Button(
                    onClick = {
                        onCreateNewPage(currentDesign)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoSuccess),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ایجاد برگه جدید در وردپرس")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun ElementorSectionCard(section: ElementorSectionDesign) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
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
                        .background(GliseoPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = section.sectionTitle,
                        color = GliseoPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "نوع: ${section.sectionType}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            if (section.subHeading.isNotBlank()) {
                Text(
                    text = section.subHeading,
                    color = GliseoSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = section.heading,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = section.bodyText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            if (section.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    section.items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✦ ", color = GliseoSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = item, fontSize = 10.sp)
                        }
                    }
                }
            }

            if (section.buttonText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GliseoPrimary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "دکمه: ${section.buttonText}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
