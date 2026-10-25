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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IranianOccasion
import com.example.data.model.JalaliCalendarHelper
import com.example.ui.theme.GliseoPrimary
import com.example.ui.theme.GliseoSecondary
import com.example.ui.theme.GliseoTertiary

@Composable
fun JalaliSchedulerDialog(
    targetTitle: String,
    onSchedule: (Int, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDaysAhead by remember { mutableIntStateOf(3) }
    var selectedOccasion by remember { mutableStateOf<IranianOccasion?>(null) }

    val datePreview = JalaliCalendarHelper.createWpFutureDate(selectedDaysAhead)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = GliseoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "زمان‌بندی تقویم شمسی (WP Future)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "با ذخیره به عنوان «status=future»، وردپرس حتی هنگام خاموش بودن اپلیکیشن در موعد مقرر مطلب را به صورت خودکار منتشر خواهد کرد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = GliseoPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "زمان انتشار برنامه‌ریزی‌شده:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = datePreview.second,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GliseoPrimary
                        )
                        Text(
                            text = "فرمت GMT وردپرس: ${datePreview.first}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "فاصله زمانی انتشار:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DaysChip(label = "فردا", days = 1, isSelected = selectedDaysAhead == 1) { selectedDaysAhead = 1 }
                    DaysChip(label = "۳ روز بعد", days = 3, isSelected = selectedDaysAhead == 3) { selectedDaysAhead = 3 }
                    DaysChip(label = "هفته آینده", days = 7, isSelected = selectedDaysAhead == 7) { selectedDaysAhead = 7 }
                    DaysChip(label = "ماه آینده", days = 30, isSelected = selectedDaysAhead == 30) { selectedDaysAhead = 30 }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "تطبیق با تقویم مناسبت‌ها و فصل‌های خرید ایرانی:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(JalaliCalendarHelper.IRANIAN_OCCASIONS) { occasion ->
                        val isPicked = selectedOccasion == occasion
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPicked) GliseoTertiary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOccasion = if (isPicked) null else occasion
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = occasion.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = occasion.jalaliDate, fontSize = 10.sp, color = GliseoTertiary, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = occasion.campaignTheme, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSchedule(selectedDaysAhead, selectedOccasion?.title) },
                colors = ButtonDefaults.buttonColors(containerColor = GliseoPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ثبت زمان‌بندی انتشار")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun DaysChip(label: String, days: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) GliseoPrimary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
