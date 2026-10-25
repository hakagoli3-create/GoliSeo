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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.TaskType
import com.example.data.remote.SeoAiResult
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoHardLock
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun ContentStudioScreen(
    items: List<SiteItem>,
    selectedItem: SiteItem?,
    aiOptimizing: Boolean,
    aiResult: SeoAiResult?,
    dryRunPreviewItem: SiteItem?,
    onSelectItem: (SiteItem?) -> Unit,
    onGenerateAiSeo: () -> Unit,
    onRunDryRun: (Map<String, Any?>) -> Unit,
    onDismissDryRun: () -> Unit,
    onCommitUpdate: (Map<String, Any?>, String) -> Unit,
    onTestHardLock: () -> Unit,
    onEnqueueTask: (Long, TaskType) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedItem != null) {
        ItemEditorView(
            item = selectedItem,
            aiOptimizing = aiOptimizing,
            aiResult = aiResult,
            dryRunPreview = dryRunPreviewItem,
            onClose = { onSelectItem(null) },
            onGenerateAiSeo = onGenerateAiSeo,
            onRunDryRun = onRunDryRun,
            onDismissDryRun = onDismissDryRun,
            onCommit = onCommitUpdate,
            onTestHardLock = onTestHardLock,
            onEnqueue = { taskType -> onEnqueueTask(selectedItem.id, taskType) },
            modifier = modifier
        )
    } else {
        ItemListView(
            items = items,
            onSelectItem = onSelectItem,
            modifier = modifier
        )
    }
}

@Composable
fun ItemListView(
    items: List<SiteItem>,
    onSelectItem: (SiteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = items.filter { item ->
        val matchesFilter = when (selectedFilter) {
            "PRODUCTS" -> item.type == SiteItemType.PRODUCT
            "POSTS" -> item.type == SiteItemType.POST
            "PAGES" -> item.type == SiteItemType.PAGE
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() || item.title.contains(searchQuery, ignoreCase = true) || item.focusKeyword.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "استودیو محتوا و سئو گلیسئو",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "مدیریت نوشته‌ها، برگه‌ها و محصولات با هوش مصنوعی و محافظت قفل سخت",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو در عناوین و کلمات کلیدی...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("همه موارد (${items.size})") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "PRODUCTS",
                    onClick = { selectedFilter = "PRODUCTS" },
                    label = { Text("محصولات ووکامرس") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "POSTS",
                    onClick = { selectedFilter = "POSTS" },
                    label = { Text("نوشته‌های وبلاگ") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "PAGES",
                    onClick = { selectedFilter = "PAGES" },
                    label = { Text("برگه‌های سایت") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { item ->
                SiteItemCard(item = item, onClick = { onSelectItem(item) })
            }
        }
    }
}

@Composable
fun SiteItemCard(item: SiteItem, onClick: () -> Unit) {
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
                // Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (item.type == SiteItemType.PRODUCT) GliseoSecondary.copy(alpha = 0.15f)
                            else GliseoPrimary.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.type.persianLabel,
                        color = if (item.type == SiteItemType.PRODUCT) GliseoSecondary else GliseoPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // SEO Score Badge
                val scoreColor = if (item.seoScore >= 75) GliseoSuccess else if (item.seoScore >= 50) GliseoTertiary else GliseoError
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "امتیاز سئو: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${item.seoScore}٪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = scoreColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.excerpt.ifBlank { item.content.take(80) + "..." },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer with Locked Fields Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isProduct) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.regularPrice, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(text = "کلیدواژه: ${item.focusKeyword.ifEmpty { "تعیین نشده" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    text = "ویرایش و بهینه‌سازی ❯",
                    color = GliseoPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ItemEditorView(
    item: SiteItem,
    aiOptimizing: Boolean,
    aiResult: SeoAiResult?,
    dryRunPreview: SiteItem?,
    onClose: () -> Unit,
    onGenerateAiSeo: () -> Unit,
    onRunDryRun: (Map<String, Any?>) -> Unit,
    onDismissDryRun: () -> Unit,
    onCommit: (Map<String, Any?>, String) -> Unit,
    onTestHardLock: () -> Unit,
    onEnqueue: (TaskType) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(item) { mutableStateOf(item.title) }
    var metaTitle by remember(item) { mutableStateOf(item.metaTitle) }
    var metaDescription by remember(item) { mutableStateOf(item.metaDescription) }
    var focusKeyword by remember(item) { mutableStateOf(item.focusKeyword) }
    var schemaJson by remember(item) { mutableStateOf(item.schemaJson) }
    var imageAlt by remember(item) { mutableStateOf(item.imageAlt) }

    // When AI result arrives, allow populating fields
    fun applyAiFields(res: SeoAiResult) {
        metaTitle = res.suggestedTitle
        metaDescription = res.suggestedMetaDescription
        focusKeyword = res.suggestedFocusKeyword
        schemaJson = res.suggestedSchemaJson
        imageAlt = res.suggestedImageAlt
    }

    fun buildCurrentChanges(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "metaTitle" to metaTitle,
            "metaDescription" to metaDescription,
            "focusKeyword" to focusKeyword,
            "schemaJson" to schemaJson,
            "imageAlt" to imageAlt
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
                Text(
                    text = "ویرایش و بهینه‌سازی سئو",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GliseoPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = item.type.persianLabel, color = GliseoPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Hard Lock Notice Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GliseoHardLock.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GliseoHardLock, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فیلدهای قفل‌شده امنیتی (غیرقابل‌تغییر)",
                            color = GliseoHardLock,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اسلاگ/URL: ${item.slug}\nدسته‌بندی: ${item.categories.joinToString("، ")}${if (item.isProduct) "\nقیمت عادی: ${item.regularPrice} | قیمت حراج: ${item.salePrice.ifEmpty { "ندارد" }}" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Dry Run Preview Alert (if active)
        if (dryRunPreview != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GliseoTertiary.copy(alpha = 0.15f)),
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
                                Icon(imageVector = Icons.Default.Preview, contentDescription = null, tint = GliseoTertiary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "پیش‌نمایش اجرای آزمایشی (Dry Run)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GliseoTertiary)
                            }
                            IconButton(onClick = onDismissDryRun) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = GliseoTertiary)
                            }
                        }
                        Text(
                            text = "هیچ تغییری در سایت زنده اعمال نشده است. در صورت رضایت روی «ذخیره نهایی» کلیک کنید.",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Gemini AI Optimizer Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = GliseoSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "بهینه‌ساز سئو جمینای (با سرچ گوگل)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = onGenerateAiSeo,
                            enabled = !aiOptimizing,
                            colors = ButtonDefaults.buttonColors(containerColor = GliseoSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (aiOptimizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("تولید خودکار سئو", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (aiResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GliseoSuccess.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "پیشنهاد هوش مصنوعی آماده است!", color = GliseoSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Button(
                                        onClick = { applyAiFields(aiResult) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GliseoSuccess),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("اعمال روی فرم", fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "متا تایتل پیشنهادی: ${aiResult.suggestedTitle}", fontSize = 11.sp)
                                Text(text = "توضیحات متا: ${aiResult.suggestedMetaDescription}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Editable Form Fields
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان نوشته یا محصول") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = focusKeyword,
                onValueChange = { focusKeyword = it },
                label = { Text("کلمه کلیدی کانونی (Focus Keyword)") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column {
                OutlinedTextField(
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    label = { Text("عنوان سئو (Meta Title)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "طول مناسب: زیر ۶۰ کاراکتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${metaTitle.length}/60",
                        fontSize = 10.sp,
                        color = if (metaTitle.length > 60) GliseoError else GliseoSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Column {
                OutlinedTextField(
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    label = { Text("توضیحات متا (Meta Description)") },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "طول استاندارد: بین ۱۳۰ تا ۱۶۰ کاراکتر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${metaDescription.length}/160",
                        fontSize = 10.sp,
                        color = if (metaDescription.length in 120..160) GliseoSuccess else GliseoTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = imageAlt,
                onValueChange = { imageAlt = it },
                label = { Text("متن جایگزین تصویر (Alt Text)") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = schemaJson,
                onValueChange = { schemaJson = it },
                label = { Text("کد اسکیما JSON-LD") },
                shape = RoundedCornerShape(10.dp),
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Action Buttons
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Primary Save Button
                Button(
                    onClick = {
                        onCommit(buildCurrentChanges(), "بهینه‌سازی سئو و فراداده")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ذخیره تغییرات و ایجاد نقطه بازگردانی (Undo)", fontWeight = FontWeight.Bold)
                }

                // Dry Run Button
                OutlinedButton(
                    onClick = { onRunDryRun(buildCurrentChanges()) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Preview, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "اجرای آزمایشی (Dry Run)", fontWeight = FontWeight.SemiBold)
                }

                // Test Hard Lock Button
                Button(
                    onClick = onTestHardLock,
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoHardLock.copy(alpha = 0.15f), contentColor = GliseoHardLock),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "آزمایش نفوذ به قفل سخت (تست مسدودسازی تغییر قیمت و اسلاگ)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
