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

@Composable
fun ArticleStudioDialog(
    topics: List<ArticleTopicSuggestion>,
    previewArticle: GeneratedArticle?,
    onSelectTopicToGenerate: (ArticleTopicSuggestion) -> Unit,
    onPublishArticle: (GeneratedArticle) -> Unit,
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
                }
            } else {
                // Topic Suggestions View
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "پیشنهاد موضوعات هوشمند بر اساس خلاءهای محتوایی سایت و جلوگیری از هم‌پوشانی کلیدواژه‌ها (Anti-Cannibalization):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
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
                }
            }
        },
        confirmButton = {
            if (previewArticle != null) {
                Button(
                    onClick = { onPublishArticle(previewArticle) },
                    colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("انتشار مقاله در وردپرس")
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
