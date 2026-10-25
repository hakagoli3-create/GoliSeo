package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class IranianOccasion(
    val title: String,
    val jalaliDate: String, // e.g. "۱ فروردین"
    val campaignTheme: String,
    val recommendedKeywords: List<String>
)

data class ScheduledPostPlan(
    val targetId: Long,
    val targetTitle: String,
    val jalaliScheduledDate: String,
    val isoGmtTimestamp: String,
    val wpStatus: String = "future",
    val occasion: String? = null
)

object JalaliCalendarHelper {

    val IRANIAN_OCCASIONS = listOf(
        IranianOccasion(
            title = "جشن باستانی نوروز و بهار نو",
            jalaliDate = "۱ تا ۱۳ فروردین",
            campaignTheme = "عیدانه و نوسازی روتین پوست، هدایای بهداشتی نوروزی",
            recommendedKeywords = listOf("خرید کادوی عید", "روتین پوستی بهار", "پکیج هدیه بهداشتی")
        ),
        IranianOccasion(
            title = "جشنواره تابستانه و محافظت آفتاب",
            jalaliDate = "۱۵ تیر تا ۱۵ مرداد",
            campaignTheme = "ضدآفتاب، آبرسانی عمیق و محافظت در برابر گرمازدگی پوست",
            recommendedKeywords = listOf("بهترین کرم ضد آفتاب", "ژل آبرسان تابستانه", "ضد لک آفتاب")
        ),
        IranianOccasion(
            title = "بلک فرایدی و حراج بزرگ پاییزه",
            jalaliDate = "هفته اول آذر",
            campaignTheme = "تخفیف‌های ویژه محصولات اورجینال، بسته‌های اقتصادی شوینده و مراقبتی",
            recommendedKeywords = listOf("حراج بلک فرایدی گلی کالا", "خرید اقلام بهداشتی با تخفیف")
        ),
        IranianOccasion(
            title = "شب یلدا و جشنواره زمستانه",
            jalaliDate = "۳۰ آذر",
            campaignTheme = "جشنواره طولانی‌ترین شب سال، تغذیه پوست در هوای سرد و خشک",
            recommendedKeywords = listOf("تخفیف یلدایی", "کرم مرطوب کننده دست و صورت زمستانه")
        ),
        IranianOccasion(
            title = "گرامیداشت روز زن و مادر",
            jalaliDate = "بهمن ماه",
            campaignTheme = "پکیج‌های هدیه لوکس مراقبت از پوست، سرم‌های جوانساز و عطر",
            recommendedKeywords = listOf("کادوی روز مادر", "پکیج مراقبت از پوست زنانه")
        )
    )

    private val PERSIAN_MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    /**
     * Converts Gregorian date to formatted Jalali string e.g. "۱۴۰۵/۰۲/۱۵"
     */
    fun formatToJalali(date: Date = Date()): String {
        val cal = Calendar.getInstance()
        cal.time = date
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        val jalali = gregorianToJalali(gYear, gMonth, gDay)
        val monthName = PERSIAN_MONTHS[(jalali[1] - 1).coerceIn(0, 11)]

        return PersianNormalizer.normalize("${jalali[2]} $monthName ${jalali[0]}")
    }

    /**
     * Creates standard WordPress future schedule ISO string.
     * When status="future" and date_gmt is in the future, WordPress WP-Cron automatically
     * publishes the post when the time arrives, even if the app or user device is completely off!
     */
    fun createWpFutureDate(daysInFuture: Int, hour: Int = 10, minute: Int = 0): Pair<String, String> {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.add(Calendar.DAY_OF_YEAR, daysInFuture)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val isoDate = sdf.format(cal.time)

        val jalaliFormatted = formatToJalali(cal.time) + " ساعت $hour:$minute"
        return Pair(isoDate, jalaliFormatted)
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val g_d_m = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var jy: Int
        var jm: Int
        var jd: Int
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + g_d_m[gm - 1]
        jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            days -= 186
            jm = 7 + days / 30
            jd = 1 + days % 30
        }
        return intArrayOf(jy, jm, jd)
    }
}
