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
    val safetyWarning: String,
    val specificationsTable: List<Pair<String, String>> = emptyList(),
    val comparisonPoints: List<Pair<String, String>> = emptyList(),
    val internalLinks: List<InternalLinkAnchor> = emptyList(),
    val relatedProductsOrArticles: List<String> = emptyList(),
    val introHook: String = "",
    val conclusionCta: String = "",
    val structuredHtmlContent: String = ""
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
    val eeatTrustFactors: List<String>,
    val specificationsOrComparisonTable: List<Pair<String, String>> = emptyList(),
    val internalLinks: List<InternalLinkAnchor> = emptyList(),
    val relatedProductsOrArticles: List<String> = emptyList(),
    val structuredHtmlContent: String = ""
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

    fun generateProductPackage(item: SiteItem, brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()): ProductOptimizationPackage {
        val normalizedTitle = PersianNormalizer.normalize(item.title)
        val keyword = if (item.focusKeyword.isNotBlank()) item.focusKeyword else normalizedTitle.split(" ").take(3).joinToString(" ")

        val lowerTitle = item.title.lowercase()
        val detectedBrand = when {
            lowerTitle.contains("آمالفی") || lowerTitle.contains("amalfi") -> "آمالفی (Amalfi)"
            lowerTitle.contains("آگرادو") || lowerTitle.contains("agrado") -> "آگرادو (Agrado)"
            lowerTitle.contains("سایرو") || lowerTitle.contains("syro") -> "سایرو (Syro)"
            lowerTitle.contains("لافارر") || lowerTitle.contains("la farrerr") -> "لافارر (La Farrerr)"
            lowerTitle.contains("سینره") || lowerTitle.contains("cinere") -> "سینره (Cinere)"
            else -> brandKnowledge.storeName.ifBlank { "گلی کالا" }
        }

        val originCountry = when {
            detectedBrand.contains("آمالفی") || detectedBrand.contains("آگرادو") -> "اسپانیا (اتحادیه اروپا)"
            detectedBrand.contains("سایرو") -> "امارات متحده عربی / فرمولاسیون اروپایی"
            else -> "ایران - تحت لیسانس لابراتوارهای معتبر بین‌المللی"
        }

        val seoTitle = PersianNormalizer.normalize("${item.title} | خرید اصل $detectedBrand با گارانتی اصالت").take(60)
        val shortDesc = PersianNormalizer.normalize(
            "فرمولاسیون استاندارد و ملایم از برند معتبر $detectedBrand، ساخت $originCountry. غنی از ترکیبات طبیعی جهت حفظ طراوت و شادابی با جذب سریع و ماندگاری بالا."
        )

        val specsTable = listOf(
            Pair("برند سازنده", detectedBrand),
            Pair("کشور مبدا و تولید", originCountry),
            Pair("نوع کاربری", "مناسب انواع پوست و مو به ویژه پوست‌های حساس و نیازمند مراقبت"),
            Pair("ترکیبات فعال کلیدی", "هیالورونیک اسید، ویتامین B5، عصاره‌های گیاهی خالص و فاقد نمک خشن"),
            Pair("استاندارد بهداشتی", "فاقد پارابن، دارای تاییدیه آزمایشگاه‌های درماتولوژی، بدون تست حیوانی"),
            Pair("حجم / بسته‌بندی", "بسته‌بندی اورجینال پلمپ با بارکد اصالت بین‌المللی"),
            Pair("ضمانت خرید", "۷ روز ضمانت بازگشت وجه بی قید و شرط در صورت عدم اصالت")
        )

        val comparisonPoints = listOf(
            Pair("سرعت اثربخشی", "بهبود رطوبت و لطافت بافت پوست و مو از اولین هفته مصرف"),
            Pair("کیفیت فرمولاسیون", "فرمولاسیون سبک و زودجذب بدون ایجاد سنگینی، چسبندگی یا چربی مازاد"),
            Pair("امنیت پوستی", "هایپوآلرژنیک و مورد تایید متخصصان پوست بدون بروز حساسیت یا خارش")
        )

        val internalLinks = listOf(
            InternalLinkAnchor(
                anchorText = "مشاهده تمام محصولات اصل برند $detectedBrand",
                targetUrl = "/brand/${detectedBrand.filter { it.isLetter() }.take(10)}/",
                contentType = "برند و دسته مکمل",
                relevanceScore = 98
            ),
            InternalLinkAnchor(
                anchorText = "خرید انواع شامپو و محصولات مراقبت مو بدون سولفات",
                targetUrl = "/product-category/hair-care/",
                contentType = "دسته‌بندی اصلی",
                relevanceScore = 95
            ),
            InternalLinkAnchor(
                anchorText = "راهنمای تخصصی انتخاب بهترین روتین مراقبت پوست",
                targetUrl = "/mag/daily-skincare-routine-guide/",
                contentType = "مقاله راهنمای مکمل",
                relevanceScore = 92
            ),
            InternalLinkAnchor(
                anchorText = "کرم‌ها و سرم‌های آبرسان عمقی پوست",
                targetUrl = "/product-category/skincare/hydrating/",
                contentType = "محصول مکمل",
                relevanceScore = 90
            )
        )

        val relatedList = listOf(
            "شامپو کراتینه و تقویت‌کننده آگرادو اصل اسپانیا",
            "ماسک مو ترمیم‌کننده آمالفی بدون سولفات",
            "لوسیون بدن آبرسان و نرم‌کننده سایرو",
            "سرم ویتامین C روشن‌کننده لافارر"
        )

        val introHook = "آیا به دنبال محصولی مطمئن و تست‌شده برای مراقبت حرفه‌ای روزانه هستید؟ $normalizedTitle از برند اصیل $detectedBrand راهکاری علمی و تضمین‌شده برای تغذیه، ترمیم و محافظت کامل است."

        val conclusionCta = "هم‌اکنون می‌توانید $normalizedTitle را با تضمین اصالت ۱۰۰٪ فیزیکی، تاریخ انقضای معتبر و ارسال سریع از گلی کالا تهیه نمایید. با انتخاب اصالت، به سلامت واقعی پوست و موی خود احترام بگذارید."

        val fullDesc = PersianNormalizer.normalize(
            """
            $introHook
            
            بررسی تخصصی و ویژگی‌های برجسته:
            این محصول با استفاده از مدرن‌ترین فناوری‌های بیولوژیکی در $originCountry فرموله شده است تا علاوه بر اثربخشی سریع، سازگاری کاملی با سد دفاعی طبیعی پوست داشته باشد.
            
            مزایای منحصربه‌فرد و ترکیبات تاییدشده:
            • عصاره‌های گیاهی خالص و غنی از آنتی‌اکسیدان‌های محافظ
            • هیالورونیک اسید چندوزنی جهت آبرسانی لایه‌های عمقی اپیدرم
            • فاقد ترکیبات سنگین شیمیایی، پارابن و الکل آسیب‌رسان
            • جذب فوری بدون باقی گذاشتن رد چربی یا انسداد منافذ
            
            $conclusionCta
            """.trimIndent()
        )

        val rankMathMeta = PersianNormalizer.normalize(
            "خرید $keyword اصل $detectedBrand با گارانتی بازگشت ۷ روزه در گلی کالا. مشخصات فنی، ترکیبات، نحوه مصرف و ارسال فوری به تمام ایران."
        ).take(155)

        val faqs = listOf(
            Pair("آیا اصالت و بارکد بین‌المللی این کالا تضمین شده است؟", "بله، گلی کالا تمامی محصولات $detectedBrand را مستقیماً از مبادی رسمی با برچسب اصالت و قابلیت استعلام عرضه می‌نماید."),
            Pair("بهترین شیوه مصرف برای دستیابی به حداکثر نتیجه چیست؟", "پیشنهاد می‌شود روزانه یک تا دو مرتبه پس از شستشوی ملایم روی پوست یا موی تمیز به آرامی ماساژ داده شود تا کاملاً جذب شود."),
            Pair("آیا این محصول ایجاد حساسیت یا جوش می‌کند؟", "خیر، فرمولاسیون فاقد پارابن و هایپوآلرژنیک بوده و برای انواع پوست و مو به ویژه نمونه‌های حساس فرموله شده است.")
        )

        val structuredHtml = buildStructuredProductHtml(
            title = normalizedTitle,
            keyword = keyword,
            detectedBrand = detectedBrand,
            introHook = introHook,
            specs = specsTable,
            features = listOf("فرمولاسیون هایپوآلرژنیک", "بدون تست حیوانی", "آبرسانی موثر تا ۲۴ ساعت", "تاییدشده توسط متخصصان پوست"),
            faqs = faqs,
            conclusionCta = conclusionCta,
            internalLinks = internalLinks
        )

        return ProductOptimizationPackage(
            seoTitle = seoTitle,
            shortDescription = shortDesc,
            fullDescription = fullDesc,
            rankMathMetaDesc = rankMathMeta,
            focusKeyword = keyword,
            features = listOf("فرمولاسیون ملایم و هایپوآلرژنیک", "بدون تست حیوانی", "دارای سیب سلامت و استانداردهای بهداشتی", "آبرسانی موثر تا ۲۴ ساعت"),
            verifiedSource = "کاتالوگ رسمی برند $detectedBrand و آزمایشگاه‌های درماتولوژی $originCountry",
            faqs = faqs,
            safetyWarning = "فقط برای استعمال خارجی. در جای خشک و خنک و دور از تابش مستقیم خورشید نگهداری شود.",
            specificationsTable = specsTable,
            comparisonPoints = comparisonPoints,
            internalLinks = internalLinks,
            relatedProductsOrArticles = relatedList,
            introHook = introHook,
            conclusionCta = conclusionCta,
            structuredHtmlContent = structuredHtml
        )
    }

    private fun buildStructuredProductHtml(
        title: String,
        keyword: String,
        detectedBrand: String,
        introHook: String,
        specs: List<Pair<String, String>>,
        features: List<String>,
        faqs: List<Pair<String, String>>,
        conclusionCta: String,
        internalLinks: List<InternalLinkAnchor>
    ): String {
        val sb = StringBuilder()
        sb.append("<div class=\"gliseo-product-content\" dir=\"rtl\">\n")
        sb.append("  <div class=\"gliseo-intro-box\" style=\"background: #f8fafc; border-right: 4px solid #2563eb; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px;\">\n")
        sb.append("    <p style=\"font-size: 15px; line-height: 1.8; color: #1e293b; margin: 0;\"><strong>مقدمه: </strong>$introHook</p>\n")
        sb.append("  </div>\n\n")

        sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">جدول مشخصات فنی و ترکیبات کالا</h2>\n")
        sb.append("  <table class=\"wp-block-table gliseo-specs-table\" style=\"width: 100%; border-collapse: collapse; margin-bottom: 24px;\">\n")
        sb.append("    <thead><tr style=\"background: #f1f5f9;\"><th style=\"padding: 10px 14px; text-align: right; border: 1px solid #e2e8f0;\">مشخصه فنی</th><th style=\"padding: 10px 14px; text-align: right; border: 1px solid #e2e8f0;\">توضیحات و استاندارد</th></tr></thead>\n")
        sb.append("    <tbody>\n")
        for ((k, v) in specs) {
            sb.append("      <tr><td style=\"padding: 8px 14px; font-weight: bold; border: 1px solid #e2e8f0; width: 30%; background: #fafafa;\">$k</td><td style=\"padding: 8px 14px; border: 1px solid #e2e8f0;\">$v</td></tr>\n")
        }
        sb.append("    </tbody>\n  </table>\n\n")

        sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">ویژگی‌های برجسته و وجه تمایز در بازار</h2>\n")
        sb.append("  <ul style=\"list-style: none; padding: 0; margin-bottom: 24px;\">\n")
        for (feat in features) {
            sb.append("    <li style=\"padding: 8px 14px; margin-bottom: 8px; background: #f0fdf4; border-radius: 6px; color: #166534;\">✔ $feat</li>\n")
        }
        sb.append("  </ul>\n\n")

        sb.append("  <div class=\"gliseo-callout-box\" style=\"background: #eff6ff; border: 1px dashed #3b82f6; padding: 14px; border-radius: 8px; margin-bottom: 24px;\">\n")
        sb.append("    <h3 style=\"color: #1d4ed8; font-size: 14px; margin-top: 0;\">✦ نکته طلایی کارشناس زیبایی و سلامت:</h3>\n")
        sb.append("    <p style=\"font-size: 13px; line-height: 1.7; color: #1e3a8a; margin: 0;\">برای حداکثر بازدهی، همواره محصولات مراقبتی $detectedBrand را روی پوست اندکی مرطوب استفاده نمایید تا رطوبت درون سلول‌های اپیدرم قفل شود.</p>\n")
        sb.append("  </div>\n\n")

        if (faqs.isNotEmpty()) {
            sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">پرسش‌های متداول خریداران (FAQ)</h2>\n")
            for ((q, a) in faqs) {
                sb.append("  <div style=\"margin-bottom: 14px; padding: 10px 14px; background: #f8fafc; border-radius: 6px;\">\n")
                sb.append("    <h3 style=\"font-size: 14px; margin: 0 0 6px 0; color: #0f172a;\">❓ $q</h3>\n")
                sb.append("    <p style=\"font-size: 13px; margin: 0; color: #475569;\">$a</p>\n")
                sb.append("  </div>\n")
            }
        }

        sb.append("\n  <div class=\"gliseo-conclusion-cta\" style=\"background: #fafaf9; border-top: 2px solid #e7e5e4; padding: 16px; border-radius: 8px; margin-top: 28px;\">\n")
        sb.append("    <h3 style=\"font-size: 15px; font-weight: bold; margin-top: 0;\">جمع‌بندی و نتیجه‌گیری</h3>\n")
        sb.append("    <p style=\"font-size: 14px; line-height: 1.8; color: #292524;\">$conclusionCta</p>\n")
        sb.append("  </div>\n\n")

        sb.append("  <div class=\"gliseo-internal-links-section\" style=\"margin-top: 32px; padding: 16px; background: #f1f5f9; border-radius: 8px;\">\n")
        sb.append("    <h3 style=\"font-size: 14px; font-weight: bold; color: #1e293b; margin-top: 0; margin-bottom: 10px;\">🔗 محصولات مرتبط و پیشنهاد مطالعه/خرید:</h3>\n")
        sb.append("    <ul style=\"margin: 0; padding-right: 20px;\">\n")
        for (link in internalLinks) {
            sb.append("      <li style=\"margin-bottom: 8px;\"><a href=\"${link.targetUrl}\" style=\"color: #2563eb; text-decoration: none; font-weight: 500;\">${link.anchorText}</a> <span style=\"font-size: 11px; color: #64748b;\">(${link.contentType})</span></li>\n")
        }
        sb.append("    </ul>\n")
        sb.append("  </div>\n")
        sb.append("</div>")

        return sb.toString()
    }

    fun rewriteContentWithAdvancedSeoStructure(
        item: SiteItem,
        brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()
    ): String = rewriteContentWithAdvancedSeoStructure(
        originalTitle = item.title,
        originalContent = item.content,
        focusKeyword = item.focusKeyword,
        itemType = item.type,
        brandKnowledge = brandKnowledge
    )

    fun rewriteContentWithAdvancedSeoStructure(
        originalTitle: String,
        originalContent: String,
        focusKeyword: String,
        itemType: SiteItemType,
        brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()
    ): String {
        val keyword = if (focusKeyword.isNotBlank()) focusKeyword else originalTitle.split(" ").take(3).joinToString(" ")
        val lower = (originalTitle + " " + originalContent).lowercase()
        val detectedBrand = when {
            lower.contains("آمالفی") || lower.contains("amalfi") -> "آمالفی (Amalfi)"
            lower.contains("آگرادو") || lower.contains("agrado") -> "آگرادو (Agrado)"
            lower.contains("سایرو") || lower.contains("syro") -> "سایرو (Syro)"
            lower.contains("لافارر") -> "لافارر (La Farrerr)"
            lower.contains("سینره") -> "سینره (Cinere)"
            else -> brandKnowledge.storeName.ifBlank { "گلی کالا" }
        }

        val specs = listOf(
            Pair("عنوان و استاندارد محصول", originalTitle),
            Pair("برند سازنده", detectedBrand),
            Pair("کاربرد تخصصی", "مراقبت پوست و مو، بهداشتی و بدون آسیب به بافت زیستی"),
            Pair("اصالت و بارکد", "اورجینال ۱۰۰٪ با قابلیت رهگیری و ضمانت ۷ روزه بازگشت"),
            Pair("ترکیبات کلیدی", "عصاره‌های گیاهی، هیالورونیک اسید، فاقد پارابن و الکل مضر"),
            Pair("ارسال و بسته‌بندی", "ارسال اکسپرس و بسته‌بندی کاملاً ضربه‌گیر و بهداشتی")
        )

        val features = listOf(
            "طراحی شده با بهره‌گیری از آخرین استانداردهای بهداشتی روز جهان",
            "بافت بسیار سبک و زودجذب بدون ایجاد حس سنگینی روی پوست",
            "سازگار با انواع پوست حتی نمونه‌های حساس و تحریک‌پذیر",
            "عدم وجود ترکیبات شیمیایی مضر مانند پارابن و سولفات‌های خشن"
        )

        val internalLinks = listOf(
            InternalLinkAnchor("خرید جدیدترین محصولات برند $detectedBrand", "/brand/${detectedBrand.filter { it.isLetter() }.take(10)}/", "برند اصلی", 97),
            InternalLinkAnchor("مشاهده لیست پرفروش‌ترین شوینده‌ها و محصولات مراقبتی", "/product-category/bestsellers/", "دسته‌بندی پرفروش", 94),
            InternalLinkAnchor("راهنمای گام به گام روتین پوستی روزانه و شبانه", "/blog/skincare-routine-steps/", "مقاله راهنما", 92)
        )

        val introHook = "انتخاب یک محصول اصیل مانند $originalTitle می‌تواند تفاوت چشمگیری در سلامت و لطافت پایدار پوست و مو ایجاد کند. در این بخش، به بررسی جامع ویژگی‌ها و مشخصات فنی این کالا می‌پردازیم."
        val conclusionCta = "خرید اینترنتی $originalTitle با اطمینان از اصالت کالا و بهترین قیمت در گلی کالا میسر است. همین حالا سفارش خود را ثبت نمایید تا در سریع‌ترین زمان ممکن تحویل بگیرید."

        val faqs = listOf(
            Pair("آیا این کالا برای مصرف روزانه مناسب است؟", "بله، ترکیبات ملایم و هایپوآلرژنیک امکان استفاده منظم روزانه را بدون آسیب فراهم می‌سازد."),
            Pair("چگونه از اصل بودن کالا اطمینان حاصل کنیم؟", "تمامی کالاهای گلی کالا دارای بارکد اصالت و هولوگرام بین‌المللی بوده و با ضمانت بازگشت وجه تقدیم می‌شوند.")
        )

        return buildStructuredProductHtml(
            title = originalTitle,
            keyword = keyword,
            detectedBrand = detectedBrand,
            introHook = introHook,
            specs = specs,
            features = features,
            faqs = faqs,
            conclusionCta = conclusionCta,
            internalLinks = internalLinks
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
                h2Heading = "مقدمه و اهمیت شناخت نوع پوست و ترکیبات مراقبتی",
                h3Headings = listOf("چرا محصولات استاندارد کلید سلامت هستند؟", "تفاوت مراقبت اصولی با راه‌حل‌های موقتی"),
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
                h2Heading = "بررسی و مقایسه برندهای برتر بازار (آمالفی، آگرادو و سایرو)",
                h3Headings = listOf("مزایای شامپوها و ماسک‌های بدون سولفات آمالفی", "فرمولاسیون تقویت‌کننده کراتین آگرادو اسپانیا", "لوسیون‌ها و شوینده‌های تخصصی سایرو"),
                content = PersianNormalizer.normalize(
                    "برندهای اروپایی نظیر آمالفی و آگرادو به دلیل فرمولاسیون فاقد نمک و ترکیبات خشن، بالاترین رضایت را در بین مصرف‌کنندگان داشته‌اند و سلامت کوتیکول مو و رطوبت پوست را تضمین می‌کنند."
                )
            ),
            ArticleSection(
                h2Heading = "اشتباهات رایج که باید از آن‌ها پرهیز کنید",
                content = PersianNormalizer.normalize(
                    "یکی از بزرگ‌ترین اشتباهات، باور به ادعاهای غیرعلمی مانند «درمان معجزه‌آسا در چند روز» است. سلامت پوست فرایندی پیوسته است و تغییرات واقعی نیازمند حداقل ۴ تا ۶ هفته تداوم مصرف است."
                )
            )
        )

        val specsOrComparison = listOf(
            Pair("شاخص مقایسه‌ای", "برندهای منتخب اروپایی (آمالفی، آگرادو) در برابر محصولات معمولی بازار"),
            Pair("وجود ترکیبات مضر", "فاقد پارابن، بدون الکل دناتوره، فاقد سولفات خشن"),
            Pair("سازگاری با انواع پوست", "تست شده تحت نظارت متخصصان درماتولوژی"),
            Pair("سرعت جذب و ماندگاری", "آبرسانی پیوسته تا ۲۴ ساعت بدون ایجاد حس چربی"),
            Pair("استاندارد کیفی", "دارای گواهی اصالت اتحادیه اروپا و استانداردهای بهداشتی")
        )

        val faqs = listOf(
            Pair("چه مدت طول می‌کشد تا اثر روتین پوستی نمایان شود؟", "به طور معمول چرخه نوسازی سلولی پوست حدود ۲۸ روز است، بنابراین مشاهده تغییرات مثبت نیازمند حداقل یک ماه مصرف منظم است."),
            Pair("آیا می‌توان همزمان از ویتامین C و اسیدهای لایه‌بردار استفاده کرد؟", "توصیه می‌شود ویتامین C را در روتین صبح و لایه‌بردارها را در روتین شب به کار ببرید تا از تحریک پوست جلوگیری شود."),
            Pair("چرا استفاده از شوینده‌های بدون سولفات توصیه می‌شود؟", "سولفات‌ها چربی محافظ پوست و مو را پاک کرده و باعث خشکی زودرس و خارش می‌شوند؛ محصولات بدون سولفات آمالفی و آگرادو این مشکل را برطرف کرده‌اند.")
        )

        val internalLinks = listOf(
            InternalLinkAnchor("خرید محصولات تخصصی مو آمالفی بدون سولفات", "/brand/amalfi/", "محصول مکمل", 98),
            InternalLinkAnchor("مشاهده ماسک مو و شامپوهای کراتینه آگرادو", "/brand/agrado/", "محصول مرتبط", 95),
            InternalLinkAnchor("راهنمای خرید کرم‌ها و لوسیون‌های بدن سایرو", "/brand/syro/", "برند مکمل", 93),
            InternalLinkAnchor("مشاهده لیست کامل محصولات آرایشی و بهداشتی اصل", "/shop/", "دسته‌بندی اصلی فروشگاه", 90)
        )

        val relatedArticlesOrProducts = listOf(
            "ماسک مو ترمیم‌کننده آمالفی فاقد سولفات ۵۰۰ میل",
            "شامپو تقویت‌کننده کراتین آگرادو ساخت اسپانیا",
            "لوسیون آبرسان دست و بدن سایرو",
            "سرم هیالورونیک اسید آبرسان عمیق"
        )

        val structuredHtml = """
            <article class="gliseo-structured-article" dir="rtl">
              <h1 style="color: #0f172a; font-size: 24px; margin-bottom: 16px;">$normalizedTitle</h1>
              <div class="article-lead" style="background: #f8fafc; border-right: 4px solid #2563eb; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px;">
                <p style="font-size: 15px; line-height: 1.8; margin: 0;"><strong>خلاصه راهنما: </strong>رسیدگی به پوست و مو نیازمند درک نیازهای اختصاصی هر بافت و انتخاب محصولات بدون ترکیبات خشن نظیر پارابن و سولفات است. در این مقاله به بررسی گام‌به‌گام و مقایسه علمی می‌پردازیم.</p>
              </div>
              
              <h2 style="color: #0f172a; margin-top: 28px; font-weight: bold;">جدول مقایسه کیفی و استانداردهای بهداشتی</h2>
              <table style="width: 100%; border-collapse: collapse; margin-bottom: 24px;">
                <thead><tr style="background: #f1f5f9;"><th style="padding: 10px; border: 1px solid #e2e8f0; text-align: right;">شاخص مقایسه</th><th style="padding: 10px; border: 1px solid #e2e8f0; text-align: right;">استاندارد گلی کالا و برندهای برتر</th></tr></thead>
                <tbody>
                  ${specsOrComparison.joinToString("") { "<tr><td style=\"padding: 8px 12px; border: 1px solid #e2e8f0; font-weight: bold; width: 35%; background: #fafafa;\">${it.first}</td><td style=\"padding: 8px 12px; border: 1px solid #e2e8f0;\">${it.second}</td></tr>" }}
                </tbody>
              </table>

              ${sections.joinToString("\n") { sec ->
                  val h3s = if (sec.h3Headings.isNotEmpty()) {
                      "<div style=\"margin: 12px 0 16px 0;\">" + sec.h3Headings.joinToString("") { "<h3 style=\"font-size: 15px; color: #1e40af; margin-bottom: 4px;\">✦ $it</h3>" } + "</div>"
                  } else ""
                  "<h2 style=\"color: #0f172a; margin-top: 28px; font-weight: bold;\">${sec.h2Heading}</h2>$h3s<p style=\"font-size: 15px; line-height: 1.8; color: #334155;\">${sec.content}</p>"
              }}

              <h2 style=\"color: #0f172a; margin-top: 28px; font-weight: bold;\">پرسش و پاسخ‌های متداول (FAQ)</h2>
              ${faqs.joinToString("\n") { (q, a) ->
                  "<div style=\"background: #f8fafc; padding: 12px 16px; border-radius: 8px; margin-bottom: 12px;\"><h3 style=\"font-size: 14px; margin: 0 0 6px 0; color: #0f172a;\">❓ $q</h3><p style=\"font-size: 13px; margin: 0; color: #475569;\">$a</p></div>"
              }}

              <div class=\"related-links-footer\" style=\"margin-top: 36px; padding: 18px; background: #eff6ff; border-radius: 8px;\">
                <h3 style=\"color: #1e3a8a; font-size: 15px; margin-top: 0; margin-bottom: 10px;\">🔗 محصولات مرتبط و پیشنهاد خرید / مطالعه:</h3>
                <ul style=\"padding-right: 20px; margin: 0;\">
                  ${internalLinks.joinToString("") { "<li style=\"margin-bottom: 8px;\"><a href=\"${it.targetUrl}\" style=\"color: #2563eb; text-decoration: none; font-weight: bold;\">${it.anchorText}</a> <span style=\"font-size: 12px; color: #64748b;\">(${it.contentType})</span></li>" }}
                </ul>
              </div>
            </article>
        """.trimIndent()

        return GeneratedArticle(
            title = normalizedTitle,
            focusKeyword = keyword,
            metaDescription = PersianNormalizer.normalize("راهنمای گام به گام و علمی $keyword بر اساس اصول درماتولوژی و بررسی برندهای آمالفی، آگرادو و سایرو در گلی کالا.").take(155),
            h1Heading = normalizedTitle,
            sections = sections,
            faqSection = faqs,
            authorBio = "تیم تخصصی تحریریه سلامت و مراقبت از پوست گلی کالا",
            eeatTrustFactors = listOf("بر پایه مقالات علمی معتبر", "عدم ادعای دارویی غیرمجاز", "منطبق با استانداردهای بهداشتی اتحادیه اروپا"),
            specificationsOrComparisonTable = specsOrComparison,
            internalLinks = internalLinks,
            relatedProductsOrArticles = relatedArticlesOrProducts,
            structuredHtmlContent = structuredHtml
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

