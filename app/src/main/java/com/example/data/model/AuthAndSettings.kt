package com.example.data.model

enum class UserRole(val persianLabel: String, val permissions: String) {
    ADMIN("مدیر کل", "دسترسی کامل، تنظیمات اتصال، خلبان خودکار و بودجه"),
    EDITOR("ویرایشگر", "تولید و ویرایش محتوا، اجرای آزمایشی و ثبت صف")
}

data class UserSession(
    val username: String = "admin_gliseo",
    val role: UserRole = UserRole.ADMIN,
    val isAuthenticated: Boolean = true,
    val is2FAActive: Boolean = true,
    val is2FAVerified: Boolean = true
)

data class SiteConfig(
    val siteUrl: String = "https://golikala.com",
    val wpUsername: String = "newadmin",
    val wpAppPassword: String = "pmqi awuG sAZA oPQU HZBr ulz2",
    val wcConsumerKey: String = "",
    val wcConsumerSecret: String = "",
    val isDemoMode: Boolean = false // Real connection to golikala.com active by default
)

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val wpConnected: Boolean,
    val wcConnected: Boolean,
    val latencyMs: Long,
    val wpVersion: String,
    val wcVersion: String,
    val siteName: String,
    val logs: List<String>
)

data class BudgetTracker(
    val dailyBudgetCapUsd: Double = 0.00,
    val currentSpentUsd: Double = 0.00,
    val dailyTokenCap: Int = 1_000_000,
    val tokensUsedToday: Int = 24_500,
    val isUnlimitedFree: Boolean = true,
    val planName: String = "پلن نامحدود و کاملاً رایگان (بدون هزینه)"
) {
    val usagePercentage: Float get() = 0f
    val isExceeded: Boolean get() = false
}

data class BrandProfile(
    val brandName: String = "گلی کالا (golikala.com)",
    val toneOfVoice: String = "تخصصی، صادقانه، بهداشتی و مورد اعتماد خریداران بازار زیبایی و سلامت بدون ادعاهای مبالغه‌آمیز",
    val targetAudience: String = "خریداران و مصرف‌کنندگان محصولات مراقبت از پوست، مو و بهداشت فردی اصل اروپایی",
    val forbiddenKeywords: String = "درج گلی کالا به عنوان سازنده کالا، ارزان‌ترین، ۱۰۰٪ معجزه، بدون رقیب، درمان قطعی",
    val competitiveAdvantage: String = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد - واردکننده مستقیم و تضمین ۱۰۰٪ اصالت فیزیکی کالا با ضمانت مرجوعی ۷ روزه"
)
