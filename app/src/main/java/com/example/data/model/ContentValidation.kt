package com.example.data.model

data class ValidationCheckResult(
    val isValid: Boolean,
    val score: Int, // 0 - 100
    val checks: List<QualityCheckItem>,
    val requiresReview: Boolean,
    val reviewReasons: List<String>
)

data class QualityCheckItem(
    val name: String,
    val passed: Boolean,
    val detail: String,
    val isCritical: Boolean = false
)

object ContentValidator {

    private val BANNED_MEDICAL_CLAIMS = listOf(
        "درمان قطعی",
        "معجزه پزشکی",
        "درمان صد در صد",
        "درمان بیماری",
        "داروی شفا",
        "بدون بازگشت تضمینی",
        "حذف دائمی لک در چند روز"
    )

    private val REQUIRED_HYGIENE_WARNINGS = listOf(
        "دور از دسترس کودکان",
        "در صورت تماس با چشم",
        "فقط برای استعمال خارجی"
    )

    /**
     * Validates an item before publication according to SEO, E-E-A-T and medical truthfulness guidelines.
     */
    fun validate(
        title: String,
        content: String,
        metaDescription: String,
        focusKeyword: String,
        isHygieneOrDetergent: Boolean,
        existingKeywordsOnSite: List<String> = emptyList()
    ): ValidationCheckResult {
        val checks = mutableListOf<QualityCheckItem>()
        val reviewReasons = mutableListOf<String>()
        var score = 100

        // 1. Title length check (~60 chars)
        val titleLen = title.length
        val titleOk = titleLen in 40..65
        if (!titleOk) {
            score -= 10
            checks.add(
                QualityCheckItem(
                    name = "طول عنوان سئو",
                    passed = false,
                    detail = "طول عنوان فعلی $titleLen کاراکتر است (استاندارد: ۴۰ تا ۶۵ کاراکتر)"
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "طول عنوان سئو",
                    passed = true,
                    detail = "طول عنوان استاندارد است ($titleLen کاراکتر)"
                )
            )
        }

        // 2. Meta description length check (~160 chars)
        val metaLen = metaDescription.length
        val metaOk = metaLen in 120..165
        if (!metaOk) {
            score -= 10
            checks.add(
                QualityCheckItem(
                    name = "طول توضیحات متا",
                    passed = false,
                    detail = "طول متا دیسکریپشن فعلی $metaLen کاراکتر است (استاندارد: ۱۲۰ تا ۱۶۰ کاراکتر)"
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "طول توضیحات متا",
                    passed = true,
                    detail = "طول توضیحات متا بهینه است ($metaLen کاراکتر)"
                )
            )
        }

        // 3. Focus keyword presence
        val normalizedKeyword = PersianNormalizer.normalize(focusKeyword).trim()
        val keywordInTitle = normalizedKeyword.isNotBlank() && PersianNormalizer.normalize(title).contains(normalizedKeyword)
        val keywordInContent = normalizedKeyword.isNotBlank() && PersianNormalizer.normalize(content).contains(normalizedKeyword)

        if (!keywordInTitle || !keywordInContent) {
            score -= 15
            checks.add(
                QualityCheckItem(
                    name = "حضور کلمه کلیدی کانونی",
                    passed = false,
                    detail = if (!keywordInTitle) "کلمه کلیدی در عنوان یافت نشد." else "کلمه کلیدی در متن اصلی به کار نرفته است."
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "حضور کلمه کلیدی کانونی",
                    passed = true,
                    detail = "کلمه کلیدی در عنوان و متن به درستی توزیع شده است."
                )
            )
        }

        // 4. Keyword Cannibalization check (هم‌پوشانی کلیدواژه‌ها با مقالات دیگر سایت)
        val cannibalizationConflict = existingKeywordsOnSite.any {
            it.isNotBlank() && it.equals(normalizedKeyword, ignoreCase = true)
        }
        if (cannibalizationConflict) {
            score -= 20
            reviewReasons.add("خطر هم‌نوع‌خواری کلمه کلیدی (Cannibalization): این کلمه قبلاً در مقاله دیگری هدف‌گذاری شده است.")
            checks.add(
                QualityCheckItem(
                    name = "عدم هم‌پوشانی کلیدواژه (Cannibalization)",
                    passed = false,
                    detail = "این کلمه کلیدی قبلاً برای یک محتوای دیگر در سایت ثبت شده و ممکن است باعث افت رتبه هر دو صفحه شود.",
                    isCritical = true
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "عدم هم‌پوشانی کلیدواژه",
                    passed = true,
                    detail = "هیچ تداخل محتوایی با صفحات دیگر سایت مشاهده نشد."
                )
            )
        }

        // 5. Medical claims & exaggerated hype check (CRITICAL)
        val normalizedContent = PersianNormalizer.normalize(content)
        val foundBannedClaims = BANNED_MEDICAL_CLAIMS.filter { normalizedContent.contains(it) }

        if (foundBannedClaims.isNotEmpty()) {
            score -= 30
            val claimList = foundBannedClaims.joinToString("، ")
            reviewReasons.add("حاوی ادعاهای اغراق‌آمیز یا پزشکی مسدود: $claimList")
            checks.add(
                QualityCheckItem(
                    name = "منع ادعاهای پزشکی و اغراق‌آمیز",
                    passed = false,
                    detail = "عبارات غیرمجاز ($claimList) شناسایی شد. ادعای معجزه یا درمان در محصولات بهداشتی ممنوع است.",
                    isCritical = true
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "منع ادعاهای پزشکی و اغراق‌آمیز",
                    passed = true,
                    detail = "لحن علمی و استاندارد رعایت شده و ادعای غیرواقعی وجود ندارد."
                )
            )
        }

        // 6. Safety warnings check for hygiene & detergents
        if (isHygieneOrDetergent) {
            val hasWarning = REQUIRED_HYGIENE_WARNINGS.any { normalizedContent.contains(it) }
            if (!hasWarning) {
                score -= 15
                reviewReasons.add("محصولات بهداشتی/شوینده باید دارای هشدار ایمنی (مانند دور از دسترس کودکان) باشند.")
                checks.add(
                    QualityCheckItem(
                        name = "هشدارهای ایمنی شوینده/بهداشتی",
                        passed = false,
                        detail = "هشدار احتیاطی استاندارد در توضیحات یافت نشد. به عنوان «نامشخص - نیازمند بازبینی» علامت خورد.",
                        isCritical = true
                    )
                )
            } else {
                checks.add(
                    QualityCheckItem(
                        name = "هشدارهای ایمنی شوینده/بهداشتی",
                        passed = true,
                        detail = "هشدارهای مصرف و ایمنی به درستی درج شده است."
                    )
                )
            }
        }

        // 7. Thin content & keyword stuffing prevention
        val wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val isThin = wordCount < 50
        if (isThin) {
            score -= 15
            checks.add(
                QualityCheckItem(
                    name = "حجم و غنای محتوا",
                    passed = false,
                    detail = "متن کمتر از ۵۰ کلمه است و محتوای کم‌ارزش (Thin Content) تلقی می‌شود."
                )
            )
        } else {
            checks.add(
                QualityCheckItem(
                    name = "حجم و غنای محتوا",
                    passed = true,
                    detail = "حجم محتوا مطلوب و دارای غنای کافی است ($wordCount کلمه)."
                )
            )
        }

        val requiresReview = reviewReasons.isNotEmpty() || score < 70

        return ValidationCheckResult(
            isValid = score >= 70 && !checks.any { it.isCritical && !it.passed },
            score = score.coerceIn(0, 100),
            checks = checks,
            requiresReview = requiresReview,
            reviewReasons = reviewReasons
        )
    }
}
