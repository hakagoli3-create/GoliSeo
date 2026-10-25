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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
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
import com.example.data.model.BrandKnowledgeBase
import com.example.ui.theme.GliseoError
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSuccess
import com.example.ui.theme.GliseoTertiary

@Composable
fun BrandKnowledgeBaseDialog(
    knowledgeBase: BrandKnowledgeBase,
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
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = GliseoTertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "حافظه و پایگاه دانش «${knowledgeBase.storeName}»",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "یادگیری مستمر از تمام ویرایش‌ها و تاییدهای شما (${knowledgeBase.totalLearnedEditsCount} مورد یادگیری)",
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
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tone of voice card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "لحن گلی کالا:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = knowledgeBase.brandTone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Allowed vs Forbidden Words
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GliseoSuccess.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "واژه‌های مجاز و شایسته:", color = GliseoSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                knowledgeBase.allowedTerms.take(4).forEach { term ->
                                    Text(text = "✓ $term", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = GliseoError.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = "واژه‌های ممنوع و مسدود:", color = GliseoError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                knowledgeBase.forbiddenTerms.take(4).forEach { term ->
                                    Text(text = "✗ $term", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Brand Official Facts (لافارر، سینره، هیدرودرم)
                item {
                    Text(text = "اطلاعات موثق و تاییدشده برندها (منبع‌محور بدون ادعای دروغین):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                items(knowledgeBase.officialBrandFacts) { fact ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = fact.brandName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GliseoPrimary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "خاستگاه: ${fact.origin}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "ادعاهای تاییدشده: ${fact.verifiedClaims.joinToString("، ")}", fontSize = 10.sp)
                            Text(text = "هشدارهای ایمنی: ${fact.safetyCautions.joinToString("، ")}", fontSize = 9.sp, color = GliseoError)
                        }
                    }
                }

                // Approved Exemplars
                item {
                    Text(text = "نمونه‌متن‌های تاییدشده و یادگیری هوش مصنوعی:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                items(knowledgeBase.approvedSamples) { sample ->
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
                                Text(text = "دسته: ${sample.category}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = "الگوی آموخته‌شده", fontSize = 9.sp, color = GliseoTertiary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "متن تصویب‌شده: «${sample.approvedText}»", fontSize = 10.sp, color = GliseoSuccess)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "دلیل تایید: ${sample.keyLearning}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}
