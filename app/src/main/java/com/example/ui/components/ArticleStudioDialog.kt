package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Publish
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArticleTopicSuggestion
import com.example.data.model.GeneratedArticle
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ArticleStudioDialog(
    topics: List<ArticleTopicSuggestion>,
    previewArticle: GeneratedArticle?,
    onSelectTopicToGenerate: (ArticleTopicSuggestion) -> Unit,
    onGenerateCustomTopic: (String, String, String?) -> Unit = { _, _, _ -> },
    onPublishArticle: (GeneratedArticle) -> Unit,
    onScheduleArticle: (GeneratedArticle, Int, String?) -> Unit = { _, _, _ -> },
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = پیشنهادات هوشمند, 1 = موضوع دلخواه
    var customTitle by remember { mutableStateOf("") }
    var customKeyword by remember { mutableStateOf("") }
    var customInstruction by remember { mutableStateOf("") }

    var scheduleDays by remember { mutableStateOf(3) }
    var scheduleOccasion by remember { mutableStateOf("جشنواره و بازاریابی محتوایی") }
    var isSchedulingMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Article, contentDescription = null, tint = GliseoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ماژول ۳: استودیو مقالات و نگارش E-E-A-T",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            if (previewArticle != null) {
                // Article Preview View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GliseoSuccess.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "مقاله جدید آماده انتشار است (بر پایه E-E-A-T):", color = GliseoSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "H1: ${previewArticle.h1Heading}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "کلمه کلیدی: ${previewArticle.focusKeyword}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Text(text = "بخش‌ها و ساختار سرفصل‌ها (H2 و H3):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    items(previewArticle.sections) { sec ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "H2: ${sec.h2Heading}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                if (sec.h3Headings.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    sec.h3Headings.forEach { h3 ->
                                        Text(text = "• H3: $h3", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = sec.content, fontSize = 10.sp)
                            }
                        }
                    }

                    // Comparison or Specs Table
                    if (previewArticle.specificationsOrComparisonTable.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "جدول مقایسه کیفی و استانداردهای بهداشتی:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    previewArticle.specificationsOrComparisonTable.forEach { (k, v) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "• $k:", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = v, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "پرسش و پاسخ‌های انتهای مقاله (FAQ):", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                previewArticle.faqSection.forEach { (q, a) ->
                                    Text(text = "سؤال: $q", fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "پاسخ: $a", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Internal Links Footer
                    if (previewArticle.internalLinks.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GliseoPrimary.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "🔗 لینک‌های داخلی و پیشنهاد خرید/مطالعه:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    previewArticle.internalLinks.forEach { link ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "✦ ${link.anchorText}", fontSize = 9.sp, color = GliseoPrimary)
                                            Text(text = link.contentType, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }


                    // Scheduling Option inside Preview
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSchedulingMode) GliseoPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "زمان‌بندی انتشار شمسی در آینده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { isSchedulingMode = !isSchedulingMode },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSchedulingMode) GliseoPrimary else MaterialTheme.colorScheme.outline
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(if (isSchedulingMode) "فعال (زمان‌بندی)" else "غیرفعال (انتشار فوری)", fontSize = 10.sp)
                                    }
                                }

                                if (isSchedulingMode) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "انتشار خودکار پس از چند روز: $scheduleDays روز آینده", fontSize = 10.sp, color = GliseoPrimary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(1, 2, 3, 5, 7, 14).forEach { d ->
                                            OutlinedButton(
                                                onClick = { scheduleDays = d },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = if (scheduleDays == d) ButtonDefaults.outlinedButtonColors(containerColor = GliseoPrimary.copy(alpha = 0.2f)) else ButtonDefaults.outlinedButtonColors()
                                            ) {
                                                Text("$d روز", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Topic Selection View with Tabs
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("پیشنهادات هوشمند سئو", fontSize = 11.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("نگارش با موضوع دلخواه", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedTab == 0) {
                        Text(
                            text = "پیشنهاد موضوعات هوشمند بر اساس خلاءهای محتوایی سایت و جلوگیری از هم‌پوشانی کلیدواژه‌ها (Anti-Cannibalization):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(topics) { topic ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (topic.isAlreadyCovered) GliseoError.copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (!topic.isAlreadyCovered) onSelectTopicToGenerate(topic)
                                        }
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
                                                    .background(GliseoPrimary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = "قصد جستجو: ${topic.searchIntent}", fontSize = 9.sp, color = GliseoPrimary, fontWeight = FontWeight.Bold)
                                            }

                                            if (topic.isAlreadyCovered) {
                                                Text(text = "⚠️ کلیدواژه هم‌پوشان (موجود است)", color = GliseoError, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = topic.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "کلیدواژه: ${topic.focusKeyword}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = topic.relevanceReason, fontSize = 9.sp, color = GliseoTertiary)
                                    }
                                }
                            }
                        }
                    } else {
                        // Custom Topic Tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "موضوع دلخواه خود را وارد کنید تا هوش مصنوعی مقاله کامل با ساختار H1-H3 و FAQ برای شما بنویسد:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = customTitle,
                                onValueChange = { customTitle = it },
                                label = { Text("عنوان یا موضوع مقاله...") },
                                placeholder = { Text("مثال: راهنمای انتخاب بهترین ضد آفتاب پوست چرب") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customKeyword,
                                onValueChange = { customKeyword = it },
                                label = { Text("کلمه کلیدی کانونی (Focus Keyword)") },
                                placeholder = { Text("مثال: بهترین ضد آفتاب پوست چرب") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customInstruction,
                                onValueChange = { customInstruction = it },
                                label = { Text("دستورات تکمیلی (لحن، تاکیدها، نکات خاص)") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )

                            Button(
                                onClick = {
                                    val t = customTitle.ifBlank { "راهنمای جامع مراقبت و سلامت پوست" }
                                    val k = customKeyword.ifBlank { t.split(" ").take(3).joinToString(" ") }
                                    onGenerateCustomTopic(t, k, customInstruction)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تولید کامل مقاله با هوش مصنوعی جمینای", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (previewArticle != null) {
                Button(
                    onClick = {
                        if (isSchedulingMode) {
                            onScheduleArticle(previewArticle, scheduleDays, scheduleOccasion)
                        } else {
                            onPublishArticle(previewArticle)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSchedulingMode) Icons.Default.CalendarMonth else Icons.Default.Publish,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSchedulingMode) "زمان‌بندی مقاله در وردپرس" else "انتشار آنی مقاله در وردپرس")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
