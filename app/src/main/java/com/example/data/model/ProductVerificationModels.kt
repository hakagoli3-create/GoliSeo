package com.example.data.model

/**
 * وضعیت صحت‌سنجی یک ادعا یا ویژگی کالا
 */
enum class VerificationClaimStatus(val persianLabel: String) {
    VERIFIED("تأییدشده"),
    CONTRADICTORY("متناقض"),
    UNVERIFIED("نامشخص / غیرقابل‌اثبات")
}

/**
 * رکورد مشخصات و ادعاهای استخراج‌شده از تصویر، عنوان و کاتالوگ
 */
data class VerifiedClaim(
    val field: String,                      // نام بخش (مثلاً: برند سازنده، کشور مبدا، آرکتایپ، دستور مصرف)
    val value: String,                      // مقدار احراز شده یا ادعا شده
    val status: VerificationClaimStatus,     // وضعیت
    val sourceName: String,                 // نام منبع معتبر (مثلاً: کاتالوگ رسمی Quimi Romar اسپانیا، وب‌گراندینگ گوگل)
    val sourceUrl: String? = null,          // لینک منبع در صورت وجود
    val evidenceSnippet: String = "",       // قطعه متن مستند
    val conflictingClaim: String? = null    // ادعای مغایر در صورت وجود تناقض
)

/**
 * نتیجه نهایی احراز هویت و تصمیم سامانه مدارشکن (Circuit Breaker)
 */
enum class ProductVerificationOutcome(val persianLabel: String) {
    PASSED_VERIFIED("هویت و مشخصات کالا احراز و تأیید قطعی شد"),
    HALTED_FATAL_CONTRADICTION("توقف تولید: تناقض شدید و غیرقابل‌اغماض در ماهیت کالا"),
    HALTED_UNVERIFIED_IDENTITY("توقف تولید: عدم امکان احراز قطعی برند یا هویت کالا"),
    HALTED_INSUFFICIENT_DATA("توقف تولید: داده‌های ورودی محصول ناقص یا فاقد عنوان و تصویر معتبر است")
}

/**
 * گزارش جامع فرایند پیش از نگارش و احراز هویت محصول
 */
data class ProductVerificationReport(
    val outcome: ProductVerificationOutcome,
    val isGenerationHalted: Boolean,
    val haltReasonPersian: String? = null,
    val verifiedClaims: List<VerifiedClaim> = emptyList(),
    val contradictoryClaims: List<VerifiedClaim> = emptyList(),
    val unverifiedClaims: List<VerifiedClaim> = emptyList(),
    val authoritativeSources: List<String> = emptyList(),
    val visionAnalysis: ProductVisionAnalysis,
    val webGroundingUsed: Boolean = false,
    val technicalDependenciesNote: String = ""
)
