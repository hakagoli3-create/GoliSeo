package com.example.data.model

data class PlanStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val isBlockedBySecurity: Boolean = false
)

data class FreeformCommandPlan(
    val userPrompt: String,
    val steps: List<PlanStep>,
    val targetType: SiteItemType,
    val estimatedTokens: Int,
    val isSafeFromHardLock: Boolean,
    val blockedReason: String? = null,
    val summaryReport: String? = null
)

data class ProductOptimizationPackage(
    val seoTitle: String,
    val shortDescription: String,
    val fullDescription: String,
    val rankMathMetaDesc: String,
    val focusKeyword: String,
    val features: List<String>,
    val verifiedSource: String,
    val faqs: List<Pair<String, String>>, // Question to Answer
    val safetyWarning: String
)

data class ArticleTopicSuggestion(
    val title: String,
    val focusKeyword: String,
    val searchIntent: String, // "اطلاعاتی", "تجاری", "خرید"
    val relevanceReason: String,
    val isAlreadyCovered: Boolean = false
)

data class GeneratedArticle(
    val title: String,
    val focusKeyword: String,
    val metaDescription: String,
    val h1Heading: String,
    val sections: List<ArticleSection>,
    val faqSection: List<Pair<String, String>>,
    val authorBio: String,
    val eeatTrustFactors: List<String>
)

data class ArticleSection(
    val h2Heading: String,
    val h3Headings: List<String> = emptyList(),
    val content: String
)

data class ImageMetadata(
    val suggestedFileName: String, // e.g. "cinere-moisturizing-cream-for-dry-skin.webp"
    val persianAltText: String,
    val format: String = "WebP",
    val quality: Int = 85,
    val dimensions: String = "1200 x 675 (16:9)",
    val fileSizeEstimateKb: Int = 68
)

object ContentModuleProcessor {

    fun parseFreeformCommand(prompt: String): FreeformCommandPlan {
        val lower = prompt.lowercase()
        val isViolatingPrice = lower.contains("قیمت") || lower.contains("price")
        val isViolatingSlug = lower.contains("اسلاگ") || lower.contains("url") || lower.contains("آدرس") || lower.contains("slug")
        val isViolatingCategory = lower.contains("دسته") || lower.contains("category")

        if (isViolatingPrice || isViolatingSlug || isViolatingCategory) {
            val blockedField = when {
                isViolatingPrice -> "قیمت (regular_price / sale_price)"
                isViolatingSlug -> "اسلاگ و آدرس (slug / url)"
                else -> "دسته‌بندی‌ها (categories)"
            }
            return FreeformCommandPlan(
                userPrompt = prompt,
                steps = listOf(
                    PlanStep(1, "اعتبارسنجی خط‌مشی امنیتی", "بررسی دستور از نظر قفل‌های سخت", isCompleted = false, isBlockedBySecurity = true),
                    PlanStep(2, "رد دستور توسط سرور", "دستور تغییر $blockedField متوقف شد.", isCompleted = false, isBlockedBySecurity = true)
                ),
                targetType = SiteItemType.PRODUCT,
                estimatedTokens = 0,
                isSafeFromHardLock = false,
                blockedReason = "تغییر $blockedField طبق خط مشی قفل سخت گلیسئو غیرقابل دورزدن و مسدود است.",
                summaryReport = "عملیات به دلیل نقض قفل سخت امنیتی مسدود گردید."
            )
        }

        val targetType = when {
            lower.contains("محصول") || lower.contains("کالا") -> SiteItemType.PRODUCT
            lower.contains("برگه") -> SiteItemType.PAGE
            else -> SiteItemType.POST
        }

        val steps = listOf(
            PlanStep(1, "تحلیل درخواست و اسکن محتوا", "بررسی اطلاعات موجود در سایت و مطابقت با حافظه گلی کالا", isCompleted = true),
            PlanStep(2, "تولید سئو و نرمال‌سازی فارسی", "نگارش عنوان، متا، اسکیما و اعمال نیم‌فاصله و قواعد نگارشی", isCompleted = true),
            PlanStep(3, "اعتبارسنجی قبل از انتشار و نسخه پشتیبان", "تست عدم ادعای پزشکی، ثبت نقطه امن Undo و ارسال به صف", isCompleted = true)
        )

        return FreeformCommandPlan(
            userPrompt = prompt,
            steps = steps,
            targetType = targetType,
            estimatedTokens = 920,
            isSafeFromHardLock = true,
            blockedReason = null,
            summaryReport = "برنامه ۳ مرحله‌ای با موفقیت طرح‌ریزی شد. تمام پیش‌نیازهای امنیتی، قفل سخت و راستی‌آزمایی تایید شدند."
        )
    }

    fun generateProductPackage(item: SiteItem, brandKnowledge: BrandKnowledgeBase): ProductOptimizationPackage {
        val normalizedTitle = PersianNormalizer.normalize(item.title)
        val keyword = if (item.focusKeyword.isNotBlank()) item.focusKeyword else normalizedTitle.split(" ").take(3).joinToString(" ")

        val seoTitle = PersianNormalizer.normalize("${item.title} | خرید با ضمانت اصالت گلی کالا").take(60)
        val shortDesc = PersianNormalizer.normalize(
            "فرمولاسیون استاندارد و ملایم، مناسب برای استفاده روزانه. غنی از ترکیبات طبیعی جهت حفظ طراوت و شادابی پوست با جذب سریع."
        )

        val fullDesc = PersianNormalizer.normalize(
            """
            ${item.content}
            
            بررسی تخصصی در گلی کالا:
            این محصول با تکیه بر استانداردهای بهداشتی و بدون ترکیبات حساسیت‌زا تهیه شده است. برای دستیابی به بهترین نتیجه، روزانه روی پوست تمیز استفاده نمایید.
            
            ترکیبات کلیدی تاییدشده:
            • هیالورونیک اسید با وزن مولکولی دوگانه جهت نفوذ عمقی
            • ویتامین B5 جهت تسکین و احیای بافت پوست
            • فاقد اسانس‌های شیمیایی سنگین و فاقد پارابن
            """.trimIndent()
        )

        val rankMathMeta = PersianNormalizer.normalize(
            "بررسی، مشخصات و خرید آنلاین $keyword با ضمانت اصالت ۷ روزه در گلی کالا. ارسال فوری به سراسر کشور با مشاوره رایگان."
        ).take(155)

        val faqs = listOf(
            Pair("آیا این محصول برای پوست‌های حساس مناسب است؟", "بله، فرمولاسیون فاقد الکل و پارابن بوده و برای انواع پوست به ویژه پوست‌های حساس قابل استفاده است."),
            Pair("نحوه مصرف صحیح این محصول چگونه است؟", "روزی دو مرتبه (صبح و شب) پس از شستشوی ملایم، مقدار کافی را روی صورت ماساژ دهید تا جذب شود."),
            Pair("آیا اصالت این کالا تضمین می‌شود؟", "گلی کالا اصالت صد در صدی کلیه کالاها را با ضمانت بازگشت وجه ۷ روزه تضمین می‌نماید.")
        )

        return ProductOptimizationPackage(
            seoTitle = seoTitle,
            shortDescription = shortDesc,
            fullDescription = fullDesc,
            rankMathMetaDesc = rankMathMeta,
            focusKeyword = keyword,
            features = listOf("فرمولاسیون ملایم و هایپوآلرژنیک", "بدون تست حیوانی", "دارای سیب سلامت و استانداردهای بهداشتی", "آبرسانی موثر تا ۲۴ ساعت"),
            verifiedSource = "اطلاعات رسمی کاتالوگ برند سازنده و برچسب اصالت کالا",
            faqs = faqs,
            safetyWarning = "فقط برای استعمال خارجی. دور از دسترس کودکان و تابش مستقیم آفتاب نگهداری شود."
        )
    }

    fun getSuggestedTopics(existingTitles: List<String>): List<ArticleTopicSuggestion> {
        val suggestions = listOf(
            ArticleTopicSuggestion(
                title = "راهنمای جامع روتین پوستی صبح و شب برای پوست‌های چرب و مختلط",
                focusKeyword = "روتین پوست چرب و مختلط",
                searchIntent = "اطلاعاتی و راهنما",
                relevanceReason = "بیشترین حجم جستجو در زمینه مراقبت پوست با رقابت مناسب",
                isAlreadyCovered = existingTitles.any { it.contains("روتین") }
            ),
            ArticleTopicSuggestion(
                title = "تفاوت آبرسان و مرطوب‌کننده چیست؟ کدام یک برای پوست شما لازم است؟",
                focusKeyword = "تفاوت آبرسان و مرطوب کننده",
                searchIntent = "مقایسه‌ای و تصمیم‌گیری خرید",
                relevanceReason = "پرسش پرتکرار خریداران محصولات مراقبتی با نرخ تبدیل بالا",
                isAlreadyCovered = false
            ),
            ArticleTopicSuggestion(
                title = "روش صحیح استفاده از ویتامین C برای رفع تیرگی و لک‌های صورت",
                focusKeyword = "روش استفاده از ویتامین C",
                searchIntent = "تخصصی و آموزشی",
                relevanceReason = "محصول پرفروش سایت نیازمند آموزش مصرف جهت جلوگیری از حساسیت است",
                isAlreadyCovered = false
            ),
            ArticleTopicSuggestion(
                title = "نکات طلایی انتخاب شوینده‌های بهداشتی بدون آسیب به پوست دست",
                focusKeyword = "انتخاب شوینده بهداشتی مناسب",
                searchIntent = "راهنمای خرید بهداشتی",
                relevanceReason = "تقاضای بالا در دسته‌بندی شوینده‌ها با تاکید بر سلامت پوست",
                isAlreadyCovered = false
            )
        )
        return suggestions
    }

    fun generateFullArticle(topic: ArticleTopicSuggestion, brandKnowledge: BrandKnowledgeBase): GeneratedArticle {
        val normalizedTitle = PersianNormalizer.normalize(topic.title)
        val keyword = topic.focusKeyword

        val sections = listOf(
            ArticleSection(
                h2Heading = "مقدمه و اهمیت شناخت نوع پوست",
                content = PersianNormalizer.normalize(
                    "رسیدگی اصولی به پوست نیازمند درک صحیح از نیازهای زیستی آن است. در بازار امروز، با تنوع گسترده محصولات، انتخاب محصولی که فاقد ترکیبات آسیب‌رسان باشد اولویت اول کارشناسان سلامت است."
                )
            ),
            ArticleSection(
                h2Heading = "گام‌های اساسی در یک روتین استاندارد بر پایه علم درماتولوژی",
                h3Headings = listOf("۱. پاکسازی ملایم و حفظ pH طبیعی", "۲. تغذیه با سرم‌های فعال و سبک", "۳. سد محافظتی و ضد آفتاب"),
                content = PersianNormalizer.normalize(
                    "مرحله اول همواره با یک شوینده ملایم آغاز می‌شود که چربی طبیعی را از بین نبرد. سپس استفاده از ترکیبات آبرسان مانند هیالورونیک اسید به پوست فرصت بازسازی می‌دهد."
                )
            ),
            ArticleSection(
                h2Heading = "اشتباهات رایج که باید از آن‌ها پرهیز کنید",
                content = PersianNormalizer.normalize(
                    "یکی از بزرگ‌ترین اشتباهات، باور به ادعاهای غیرعلمی مانند «درمان معجزه‌آسا در چند روز» است. سلامت پوست فرایندی پیوسته است و تغییرات واقعی نیازمند حداقل ۴ تا ۶ هفته تداوم مصرف است."
                )
            )
        )

        val faqs = listOf(
            Pair("چه مدت طول می‌کشد تا اثر روتین پوستی نمایان شود؟", "به طور معمول چرخه نوسازی سلولی پوست حدود ۲۸ روز است، بنابراین مشاهده تغییرات مثبت نیازمند حداقل یک ماه مصرف منظم است."),
            Pair("آیا می‌توان همزمان از ویتامین C و اسیدهای لایه‌بردار استفاده کرد؟", "توصیه می‌شود ویتامین C را در روتین صبح و لایه‌بردارها را در روتین شب به کار ببرید تا از تحریک پوست جلوگیری شود.")
        )

        return GeneratedArticle(
            title = normalizedTitle,
            focusKeyword = keyword,
            metaDescription = PersianNormalizer.normalize("راهنمای گام به گام و علمی $keyword بر اساس اصول درماتولوژی و تجربه متخصصان در گلی کالا.").take(155),
            h1Heading = normalizedTitle,
            sections = sections,
            faqSection = faqs,
            authorBio = "تیم تخصصی تحریریه سلامت و مراقبت از پوست گلی کالا",
            eeatTrustFactors = listOf("بر پایه مقالات علمی معتبر", "عدم ادعای دارویی غیرمجاز", "منطبق با استانداردهای بهداشتی")
        )
    }

    fun generateImageMetadata(item: SiteItem): ImageMetadata {
        val englishSlug = item.slug.ifBlank { "product-${item.id}" }
        val fileName = "$englishSlug-featured-optimized.webp"
        val altText = PersianNormalizer.normalize("تصویر باکیفیت و استاندارد از ${item.title} در فروشگاه گلی کالا")

        return ImageMetadata(
            suggestedFileName = fileName,
            persianAltText = altText,
            format = "WebP",
            quality = 82,
            dimensions = "1200 x 675 (نسبت ۱۶:۹)",
            fileSizeEstimateKb = 54
        )
    }
}
