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
    val siteUrl: String = "https://my-store.example.com",
    val wpUsername: String = "gliseo_bot",
    val wpAppPassword: String = "xxxx yyyy zzzz wwww",
    val wcConsumerKey: String = "ck_1234567890abcdef",
    val wcConsumerSecret: String = "cs_0987654321fedcba",
    val isDemoMode: Boolean = true // Seamlessly allows real site + demo mock mode
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
    val brandName: String = "فروشگاه آنلاین گلیسئو",
    val toneOfVoice: String = "حرفه‌ای، معتمد، شیوا و راهنما",
    val targetAudience: String = "خریداران هوشمند و جویای کیفیت برتر در بازار وب فارسی",
    val forbiddenKeywords: String = "ارزان‌ترین، ۱۰۰٪ معجزه، بدون رقیب",
    val competitiveAdvantage: String = "تضمین اصالت کالا، مشاوره رایگان تخصصی، ارسال فوری"
)
