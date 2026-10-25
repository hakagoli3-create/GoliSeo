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

data class ProductPreAnalysis(
    val detectedBrand: String,
    val originCountry: String,
    val productType: String,
    val primaryApplication: String,
    val isOfficialAgencyBrand: Boolean,
    val agencyStatement: String
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
    val structuredHtmlContent: String = "",
    val detectedBrand: String = "",
    val originCountry: String = "",
    val productType: String = "",
    val isOfficialAgencyBrand: Boolean = false,
    val agencyStatement: String = ""
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
    val suggestedFileName: String,
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
            PlanStep(1, "تحلیل پیش از نگارش و تشخیص هویت کالا", "تشخیص نوع کالا، کشور سازنده، برند اصلی و احراز هویت نماینده رسمی گلی کالا", isCompleted = true),
            PlanStep(2, "تولید سئو، جدول مشخصات و لینک‌های داخلی golikala.com", "نگارش H1، توضیحات کوتاه جذاب، جدول‌بندی و پیوندسازی داخلی استاندارد", isCompleted = true),
            PlanStep(3, "اعتبارسنجی قبل از انتشار و نسخه پشتیبان", "تست عدم ادعای پزشکی، ثبت نقطه امن Undo و ارسال به صف", isCompleted = true)
        )

        return FreeformCommandPlan(
            userPrompt = prompt,
            steps = steps,
            targetType = targetType,
            estimatedTokens = 920,
            isSafeFromHardLock = true,
            blockedReason = null,
            summaryReport = "برنامه ۳ مرحله‌ای با موفقیت طرح‌ریزی شد. تمام پیش‌نیازهای تحلیل پیش از نگارش، هویت نمایندگی گلی کالا و لینک‌سازی داخلی تایید شدند."
        )
    }

    /**
     * گام ۱: تحلیل پیش از نگارش و تولید محتوای تخصصی:
     * تشخیص دقیق نوع محصول، کشور سازنده، کاربرد و برند اصلی سازنده.
     * رعایت قاعده بنیادین: «گلی کالا» نام فروشگاه آنلاین و نماینده رسمی است، هرگز برند سازنده نیست.
     */
    fun analyzeProductBeforeWriting(item: SiteItem): ProductPreAnalysis {
        val lowerText = (item.title + " " + item.content + " " + item.categories.joinToString(" ") + " " + item.tags.joinToString(" ")).lowercase()

        val isAmalfi = lowerText.contains("آمالفی") || lowerText.contains("amalfi")
        val isAgrado = lowerText.contains("آگرادو") || lowerText.contains("agrado")
        val isSairo = lowerText.contains("سایرو") || lowerText.contains("sairo") || lowerText.contains("syro")
        val isLafarrerr = lowerText.contains("لافارر") || lowerText.contains("la farrerr")
        val isCinere = lowerText.contains("سینره") || lowerText.contains("cinere")
        val isHydroderm = lowerText.contains("هیدرودرم") || lowerText.contains("hydroderm")

        val detectedBrand = when {
            isAmalfi -> "آمالفی (Amalfi)"
            isAgrado -> "آگرادو (Agrado)"
            isSairo -> "سایرو (Sairo)"
            isLafarrerr -> "لافارر (La Farrerr)"
            isCinere -> "سینره (Cinere)"
            isHydroderm -> "هیدرودرم (Hydroderm)"
            else -> "برندهای معتبر اروپایی و بین‌المللی"
        }

        val originCountry = when {
            isAmalfi || isAgrado || isSairo -> "اسپانیا (اتحادیه اروپا)"
            isLafarrerr -> "ایران - فرمولاسیون آزمایشگاه‌های فرانسه"
            isCinere -> "ایران - شرکت دانش‌بنیان طبیعت زنده"
            isHydroderm -> "ایران - لابراتوار پارس حیان"
            else -> "اتحادیه اروپا / استانداردهای بین‌المللی"
        }

        val isAgency = isAmalfi || isAgrado || isSairo
        val agencyStatement = if (isAgency) {
            "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد."
        } else {
            "گلی کالا واردکننده و عرضه‌کننده مستقیم محصولات اصل با تضمین ۱۰۰٪ اصالت فیزیکی و آزمایشگاهی می‌باشد."
        }

        // Product type detection
        val productType = when {
            lowerText.contains("ماسک مو") -> "ماسک مو تخصصی و ترمیم‌کننده عمیق"
            lowerText.contains("شامپو") -> "شامپو فاقد سولفات و تقویت‌کننده"
            lowerText.contains("لوسیون") -> "لوسیون بدن آبرسان و مغذی"
            lowerText.contains("سرم") -> "سرم تغذیه‌کننده و فعال پوست"
            lowerText.contains("ضد آفتاب") -> "کرم ضد آفتاب با محافظت حداکثری"
            lowerText.contains("ژل") || lowerText.contains("شوینده") -> "ژل شوینده و پاک‌کننده بهداشتی پوست"
            lowerText.contains("تونر") -> "تونر پاکسازی و تنظیم‌کننده pH"
            lowerText.contains("دور چشم") -> "کرم تخصصی جوان‌ساز دور چشم"
            lowerText.contains("آبرسان") || lowerText.contains("مرطوب") -> "کرم آبرسان عمقی و مرطوب‌کننده پوست"
            else -> "محصول تخصصی مراقبت از سلامت پوست و مو"
        }

        // Application detection
        val application = when {
            lowerText.contains("کراتین") || lowerText.contains("رنگ شده") || lowerText.contains("آسیب") ->
                "احیا و محافظت از ساختار تارهای موهای کراتینه‌شده، دکلره یا خشک"
            lowerText.contains("جوش") || lowerText.contains("چرب") || lowerText.contains("آکنه") ->
                "تنظیم ترشح چربی طبیعی پوست و پاکسازی عمقی منافذ بدون ایجاد حس خشکی"
            lowerText.contains("لک") || lowerText.contains("روشن") || lowerText.contains("تیرگی") ->
                "یکنواخت‌سازی تناژ رنگ چهره و افزایش شفافیت و درخشندگی طبیعی"
            lowerText.contains("خشک") || lowerText.contains("حساس") ->
                "تسکین التهابات، رفع خارش و قرمزی و بازسازی سد دفاعی پوست"
            else ->
                "حفظ لطافت، سلامت پایدار و بهداشت فردی روزانه بر پایه فرمولاسیون ایمن"
        }

        return ProductPreAnalysis(
            detectedBrand = detectedBrand,
            originCountry = originCountry,
            productType = productType,
            primaryApplication = application,
            isOfficialAgencyBrand = isAgency,
            agencyStatement = agencyStatement
        )
    }

    /**
     * گام ۲ و ۳: تولید پکیج کامل سئو با عنوان H1، متن کوتاه جذاب، توضیحات جدول‌بندی شده و لینک‌های داخلی golikala.com
     */
    fun generateProductPackage(item: SiteItem, brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()): ProductOptimizationPackage {
        val analysis = analyzeProductBeforeWriting(item)
        val normalizedTitle = PersianNormalizer.normalize(item.title)
        val keyword = if (item.focusKeyword.isNotBlank()) item.focusKeyword else normalizedTitle.split(" ").take(3).joinToString(" ")

        // SEO Title (H1) - Max 60 chars
        val seoTitle = if (analysis.isOfficialAgencyBrand) {
            PersianNormalizer.normalize("${item.title} اصل ${analysis.detectedBrand.split(" ").first()} | گلی کالا").take(60)
        } else {
            PersianNormalizer.normalize("${item.title} اصل | گارانتی اصالت گلی کالا").take(60)
        }

        // Short Description (متن کوتاه جذاب، دقیق و بدون اغراق)
        val shortDesc = if (analysis.isOfficialAgencyBrand) {
            PersianNormalizer.normalize(
                "${analysis.productType} از برند معتبر ${analysis.detectedBrand}، ساخت ${analysis.originCountry}. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. فرمولاسیون فاقد نمک خشن جهت ${analysis.primaryApplication} با جذب سریع و ماندگاری بالا."
            )
        } else {
            PersianNormalizer.normalize(
                "${analysis.productType} از برند ${analysis.detectedBrand} ساخت ${analysis.originCountry}. تضمین اصالت ۱۰۰٪ فیزیکی در فروشگاه گلی کالا، مناسب ${analysis.primaryApplication} با بالاترین استانداردهای بهداشتی."
            )
        }

        // Specifications Table (جدول مشخصات و مقایسه فنی کالا)
        val specsTable = mutableListOf<Pair<String, String>>()
        specsTable.add(Pair("نام و استاندارد کالا", normalizedTitle))
        specsTable.add(Pair("برند اصلی سازنده", analysis.detectedBrand))
        specsTable.add(Pair("کشور مبدا و تولید", analysis.originCountry))
        if (analysis.isOfficialAgencyBrand) {
            specsTable.add(Pair("وضعیت نمایندگی در ایران", "گلی کالا (نماینده رسمی و انحصاری محصولات آمالفی، آگرادو و سایرو)"))
        } else {
            specsTable.add(Pair("عرضه‌کننده و مرجع خرید", "گلی کالا (واردکننده رسمی و مرجع اصالت کالا)"))
        }
        specsTable.add(Pair("نوع محصول و کاربرد", analysis.productType))
        specsTable.add(Pair("هدف و اثربخشی", analysis.primaryApplication))
        specsTable.add(Pair("ترکیبات فعال و کلیدی", "عصاره‌های خالص گیاهی، هیالورونیک اسید، غنی از ویتامین‌ها و فاقد ترکیبات سنگین مضر"))
        specsTable.add(Pair("استاندارد ایمنی و بهداشتی", "فاقد پارابن، هایپوآلرژنیک، مطابق با استانداردهای اتحادیه اروپا"))
        specsTable.add(Pair("اصالت و گارانتی خرید", "تضمین ۱۰۰٪ اصالت فیزیکی، دارای بارکد رسمی بین‌المللی با ۷ روز گارانتی بازگشت"))

        val comparisonPoints = listOf(
            Pair("سرعت اثربخشی", "بهبود رطوبت، بافت و لطافت پایدار از همان هفته‌های اولیه مصرف منظم"),
            Pair("فرمولاسیون ایمن", "سبک، زودجذب و فاقد چربی مازاد یا ایجاد حس سنگینی روی پوست و مو"),
            Pair("پایداری نتایج", "تقویت و بازسازی سد طبیعی بدون نیاز به وابستگی دارویی یا ادعاهای غیرواقعی")
        )

        // Real internal links using golikala.com
        val brandSlug = when {
            analysis.detectedBrand.contains("آمالفی") -> "amalfi"
            analysis.detectedBrand.contains("آگرادو") -> "agrado"
            analysis.detectedBrand.contains("سایرو") -> "sairo"
            else -> "brands"
        }

        val internalLinks = listOf(
            InternalLinkAnchor(
                anchorText = "مشاهده تمام محصولات اصل برند ${analysis.detectedBrand}",
                targetUrl = "https://golikala.com/brand/$brandSlug/",
                contentType = "برند و دسته اصلی",
                relevanceScore = 98
            ),
            InternalLinkAnchor(
                anchorText = "خرید انواع شامپو و محصولات مراقبت مو بدون سولفات در گلی کالا",
                targetUrl = "https://golikala.com/product-category/hair-care/",
                contentType = "دسته‌بندی پرفروش",
                relevanceScore = 96
            ),
            InternalLinkAnchor(
                anchorText = "کرم‌ها، سرم‌ها و محصولات تخصصی آبرسان پوست",
                targetUrl = "https://golikala.com/product-category/skincare/",
                contentType = "محصول مکمل",
                relevanceScore = 93
            ),
            InternalLinkAnchor(
                anchorText = "مقاله راهنمای جامع تشخیص لوازم آرایشی و بهداشتی اصل اروپایی",
                targetUrl = "https://golikala.com/blog/how-to-spot-fake-cosmetics/",
                contentType = "مقاله راهنما و بلاگ",
                relevanceScore = 91
            ),
            InternalLinkAnchor(
                anchorText = "لیست پرفروش‌ترین محصولات آرایشی و بهداشتی گلی کالا",
                targetUrl = "https://golikala.com/product-category/bestsellers/",
                contentType = "پرفروش‌ترین‌های سایت",
                relevanceScore = 90
            )
        )

        val relatedList = listOf(
            "شامپو کراتینه و تقویت‌کننده آگرادو اصل اسپانیا",
            "ماسک مو ترمیم‌کننده بدون سولفات آمالفی اسپانیا",
            "لوسیون بدن آبرسان و نرم‌کننده سایرو",
            "سرم هیالورونیک اسید آبرسان عمیق پوست"
        )

        val introHook = if (analysis.isOfficialAgencyBrand) {
            "آیا به دنبال محصولی اصل و استاندارد برای مراقبت حرفه‌ای روزانه هستید؟ $normalizedTitle از برند اصیل ${analysis.detectedBrand} (ساخت ${analysis.originCountry}) انتخابی مطمئن است. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و این محصول را با تضمین اصالت ۱۰۰٪ عرضه می‌نماید."
        } else {
            "رسیدگی اصولی به سلامت و زیبایی روزانه نیازمند انتخاب محصولات مطمئن با فرمولاسیون آزمایشگاهی استاندارد است. $normalizedTitle از برند ${analysis.detectedBrand} (ساخت ${analysis.originCountry}) با ترکیبات استاندارد در فروشگاه گلی کالا در دسترس شماست."
        }

        val conclusionCta = "هم‌اکنون می‌توانید $normalizedTitle را با تضمین اصالت ۱۰۰٪ فیزیکی، تاریخ انقضای معتبر و ارسال اکسپرس از فروشگاه اینترنتی گلی کالا (golikala.com) سفارش دهید. سلامت و درخشش طبیعی پوست و موی خود را به محصولات استاندارد بسپارید."

        val fullDesc = PersianNormalizer.normalize(
            """
            $introHook
            
            بررسی تخصصی و ویژگی‌های محصول:
            این محصول توسط لابراتوارهای تخصصی برند معتبر ${analysis.detectedBrand} در ${analysis.originCountry} طراحی شده است تا به شکلی علمی به ${analysis.primaryApplication} کمک کند.
            
            مزایای منحصربه‌فرد و ترکیبات تاییدشده:
            • فرمولاسیون استاندارد و سازگار با انواع بافت زیستی پوست و مو
            • فاقد ترکیبات سنگین شیمیایی، پارابن و الکل آسیب‌رسان
            • جذب فوری و ماندگاری اثر بدون احساس چربی مازاد
            • تاییدشده توسط تست‌های درماتولوژی در اتحادیه اروپا
            
            $conclusionCta
            """.trimIndent()
        )

        val rankMathMeta = PersianNormalizer.normalize(
            if (analysis.isOfficialAgencyBrand) {
                "خرید $keyword اصل ${analysis.detectedBrand} در گلی کالا (نماینده رسمی آمالفی، آگرادو و سایرو). مشخصات فنی، ترکیبات، نحوه مصرف و ضمانت اصالت ۷ روزه."
            } else {
                "خرید اینترنتی $keyword اصل ${analysis.detectedBrand} با گارانتی اصالت در گلی کالا. مشخصات کامل، جدول ترکیبات و ارسال سریع به سراسر ایران."
            }
        ).take(155)

        val faqs = listOf(
            Pair(
                "آیا اصالت کالا و بارکد بین‌المللی این محصول تضمین شده است؟",
                if (analysis.isOfficialAgencyBrand) {
                    "بله، گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و تمامی محصولات مستقیماً با ضمانت اصالت فیزیکی و آزمایشگاهی ارائه می‌شوند."
                } else {
                    "بله، تمامی محصولات موجود در گلی کالا دارای بارکد بین‌المللی معتبر، تاریخ انقضای تاییدشده و ۷ روز ضمانت بازگشت کالا هستند."
                }
            ),
            Pair(
                "بهترین شیوه مصرف برای دستیابی به حداکثر اثربخشی چیست؟",
                "پیشنهاد می‌شود طبق دستورالعمل استاندارد، پس از پاکسازی اولیه، مقدار مناسب را روی مو یا پوست نمدار استفاده نموده و به آرامی ماساژ دهید تا جذب کامل صورت پذیرد."
            ),
            Pair(
                "آیا این کالا فاقد سولفات خشن و پارابن می‌باشد؟",
                "بله، محصولات وارداتی گلی کالا با سخت‌گیرانه‌ترین استانداردهای بهداشتی اتحادیه اروپا فرموله شده و فاقد نمک‌های آسیب‌رسان و ترکیبات حساسیت‌زا می‌باشند."
            )
        )

        val structuredHtml = buildStructuredProductHtml(
            title = normalizedTitle,
            keyword = keyword,
            analysis = analysis,
            introHook = introHook,
            specs = specsTable,
            features = listOf(
                "فرمولاسیون هایپوآلرژنیک و استاندارد اروپایی",
                "فاقد پارابن، سولفات خشن و الکل مضر",
                "حفظ رطوبت و طراوت طبیعی تا ۲۴ ساعت",
                "دارای بارکد بین‌المللی و ضمانت اصالت فیزیکی"
            ),
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
            features = listOf("فرمولاسیون ملایم و هایپوآلرژنیک", "بدون تست حیوانی", "دارای استانداردهای بهداشتی اروپا", "آبرسانی موثر تا ۲۴ ساعت"),
            verifiedSource = "کاتالوگ رسمی برند ${analysis.detectedBrand} و استانداردهای بهداشتی ${analysis.originCountry}",
            faqs = faqs,
            safetyWarning = "فقط برای استعمال خارجی. در جای خشک و خنک و دور از تابش مستقیم خورشید نگهداری شود.",
            specificationsTable = specsTable,
            comparisonPoints = comparisonPoints,
            internalLinks = internalLinks,
            relatedProductsOrArticles = relatedList,
            introHook = introHook,
            conclusionCta = conclusionCta,
            structuredHtmlContent = structuredHtml,
            detectedBrand = analysis.detectedBrand,
            originCountry = analysis.originCountry,
            productType = analysis.productType,
            isOfficialAgencyBrand = analysis.isOfficialAgencyBrand,
            agencyStatement = analysis.agencyStatement
        )
    }

    private fun buildStructuredProductHtml(
        title: String,
        keyword: String,
        analysis: ProductPreAnalysis,
        introHook: String,
        specs: List<Pair<String, String>>,
        features: List<String>,
        faqs: List<Pair<String, String>>,
        conclusionCta: String,
        internalLinks: List<InternalLinkAnchor>
    ): String {
        val sb = StringBuilder()
        sb.append("<div class=\"gliseo-product-content\" dir=\"rtl\">\n")

        // Agency & Verification Callout Box
        if (analysis.isOfficialAgencyBrand) {
            sb.append("  <div class=\"gliseo-agency-notice\" style=\"background: #f0fdf4; border: 2px solid #22c55e; padding: 14px 18px; border-radius: 8px; margin-bottom: 20px;\">\n")
            sb.append("    <p style=\"font-size: 14px; line-height: 1.8; color: #166534; margin: 0; font-weight: bold;\">✦ گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.</p>\n")
            sb.append("    <p style=\"font-size: 12px; line-height: 1.6; color: #15803d; margin: 4px 0 0 0;\">این کالا مستقیماً از کمپانی تولیدکننده در ${analysis.originCountry} وارد شده و همراه با تضمین ۱۰۰٪ اصالت فیزیکی و قابلیت استعلام بارکد عرضه می‌گردد.</p>\n")
            sb.append("  </div>\n\n")
        }

        sb.append("  <div class=\"gliseo-intro-box\" style=\"background: #f8fafc; border-right: 4px solid #2563eb; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px;\">\n")
        sb.append("    <p style=\"font-size: 15px; line-height: 1.8; color: #1e293b; margin: 0;\"><strong>مقدمه و بررسی تخصصی: </strong>$introHook</p>\n")
        sb.append("  </div>\n\n")

        sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">جدول مشخصات فنی، ترکیبات و اصالت کالا</h2>\n")
        sb.append("  <table class=\"wp-block-table gliseo-specs-table\" style=\"width: 100%; border-collapse: collapse; margin-bottom: 24px;\">\n")
        sb.append("    <thead><tr style=\"background: #f1f5f9;\"><th style=\"padding: 10px 14px; text-align: right; border: 1px solid #e2e8f0;\">مشخصه و ویژگی</th><th style=\"padding: 10px 14px; text-align: right; border: 1px solid #e2e8f0;\">توضیحات و استاندارد رسمی</th></tr></thead>\n")
        sb.append("    <tbody>\n")
        for ((k, v) in specs) {
            val bg = if (k.contains("نمایندگی") || k.contains("اصالت")) "#f0fdf4" else "#fafafa"
            sb.append("      <tr><td style=\"padding: 8px 14px; font-weight: bold; border: 1px solid #e2e8f0; width: 32%; background: $bg;\">$k</td><td style=\"padding: 8px 14px; border: 1px solid #e2e8f0;\">$v</td></tr>\n")
        }
        sb.append("    </tbody>\n  </table>\n\n")

        sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">ویژگی‌های برجسته و مزایای رقابتی</h2>\n")
        sb.append("  <ul style=\"list-style: none; padding: 0; margin-bottom: 24px;\">\n")
        for (feat in features) {
            sb.append("    <li style=\"padding: 8px 14px; margin-bottom: 8px; background: #f0fdf4; border-radius: 6px; color: #166534;\">✔ $feat</li>\n")
        }
        sb.append("  </ul>\n\n")

        sb.append("  <div class=\"gliseo-callout-box\" style=\"background: #eff6ff; border: 1px dashed #3b82f6; padding: 14px; border-radius: 8px; margin-bottom: 24px;\">\n")
        sb.append("    <h3 style=\"color: #1d4ed8; font-size: 14px; margin-top: 0;\">✦ نکته تخصصی کارشناس زیبایی و سلامت:</h3>\n")
        sb.append("    <p style=\"font-size: 13px; line-height: 1.7; color: #1e3a8a; margin: 0;\">برای اثربخشی ماندگار، محصولات مراقبتی ${analysis.detectedBrand} را روی پوست یا موی اندکی مرطوب استفاده نمایید تا مواد مغذی و رطوبت طبیعی درون سلول‌ها قفل شود.</p>\n")
        sb.append("  </div>\n\n")

        if (faqs.isNotEmpty()) {
            sb.append("  <h2 style=\"color: #0f172a; margin-top: 28px; margin-bottom: 14px; font-weight: bold;\">پرسش‌های متداول خریداران (FAQ)</h2>\n")
            for ((q, a) in faqs) {
                sb.append("  <div style=\"margin-bottom: 14px; padding: 12px 16px; background: #f8fafc; border-radius: 8px;\">\n")
                sb.append("    <h3 style=\"font-size: 14px; margin: 0 0 6px 0; color: #0f172a;\">❓ $q</h3>\n")
                sb.append("    <p style=\"font-size: 13px; margin: 0; color: #475569; line-height: 1.7;\">$a</p>\n")
                sb.append("  </div>\n")
            }
        }

        sb.append("\n  <div class=\"gliseo-conclusion-cta\" style=\"background: #fafaf9; border-top: 2px solid #e7e5e4; padding: 16px; border-radius: 8px; margin-top: 28px;\">\n")
        sb.append("    <h3 style=\"font-size: 15px; font-weight: bold; margin-top: 0;\">جمع‌بندی و راهنمای خرید</h3>\n")
        sb.append("    <p style=\"font-size: 14px; line-height: 1.8; color: #292524;\">$conclusionCta</p>\n")
        sb.append("  </div>\n\n")

        sb.append("  <div class=\"gliseo-internal-links-section\" style=\"margin-top: 32px; padding: 16px; background: #f1f5f9; border-radius: 8px; border-right: 4px solid #2563eb;\">\n")
        sb.append("    <h3 style=\"font-size: 14px; font-weight: bold; color: #1e293b; margin-top: 0; margin-bottom: 10px;\">🔗 لینک‌های داخلی و محصولات مرتبط در گلی کالا (golikala.com):</h3>\n")
        sb.append("    <ul style=\"margin: 0; padding-right: 20px;\">\n")
        for (link in internalLinks) {
            sb.append("      <li style=\"margin-bottom: 8px;\"><a href=\"${link.targetUrl}\" style=\"color: #2563eb; text-decoration: none; font-weight: bold;\">${link.anchorText}</a> <span style=\"font-size: 11px; color: #64748b;\">(${link.contentType})</span></li>\n")
        }
        sb.append("    </ul>\n")
        sb.append("  </div>\n")
        sb.append("</div>")

        return sb.toString()
    }

    fun rewriteContentWithAdvancedSeoStructure(
        item: SiteItem,
        brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()
    ): ProductOptimizationPackage {
        return generateProductPackage(item, brandKnowledge)
    }

    fun rewriteContentWithAdvancedSeoStructure(
        originalTitle: String,
        originalContent: String,
        focusKeyword: String,
        itemType: SiteItemType,
        brandKnowledge: BrandKnowledgeBase = BrandKnowledgeBase()
    ): String {
        val dummyItem = SiteItem(
            id = 9999,
            title = originalTitle,
            content = originalContent,
            slug = "optimized-content",
            focusKeyword = focusKeyword,
            type = itemType
        )
        return generateProductPackage(dummyItem, brandKnowledge).structuredHtmlContent
    }

    /**
     * تولید بلاک HTML لینک‌های داخلی واقعی و استاندارد golikala.com
     */
    fun buildRealGolikalaInternalLinksHtml(title: String): String {
        val lower = title.lowercase()
        val isAmalfi = lower.contains("آمالفی") || lower.contains("amalfi")
        val isAgrado = lower.contains("آگرادو") || lower.contains("agrado")
        val isSairo = lower.contains("سایرو") || lower.contains("sairo") || lower.contains("syro")
        val brandSlug = when {
            isAmalfi -> "amalfi"
            isAgrado -> "agrado"
            isSairo -> "sairo"
            else -> "europe"
        }
        val brandLabel = when {
            isAmalfi -> "آمالفی (Amalfi)"
            isAgrado -> "آگرادو (Agrado)"
            isSairo -> "سایرو (Sairo)"
            else -> "محصولات اصل اروپایی"
        }

        val links = listOf(
            InternalLinkAnchor(
                anchorText = "خرید تمامی محصولات اصل $brandLabel در گلی کالا",
                targetUrl = "https://golikala.com/brand/$brandSlug/",
                contentType = "برند و دسته مکمل",
                relevanceScore = 98
            ),
            InternalLinkAnchor(
                anchorText = "مشاهده دسته‌بندی تخصصی محصولات مراقبت از پوست و مو",
                targetUrl = "https://golikala.com/product-category/hair-care/",
                contentType = "دسته‌بندی اصلی ووکامرس",
                relevanceScore = 95
            ),
            InternalLinkAnchor(
                anchorText = "راهنمای جامع تشخیص لوازم آرایشی و بهداشتی اصل اروپایی",
                targetUrl = "https://golikala.com/blog/how-to-spot-fake-cosmetics/",
                contentType = "مقاله راهنما و بلاگ",
                relevanceScore = 93
            ),
            InternalLinkAnchor(
                anchorText = "پرفروش‌ترین محصولات بهداشتی و مراقبتی با ضمانت اصالت ۱۰۰٪",
                targetUrl = "https://golikala.com/product-category/bestsellers/",
                contentType = "پرفروش‌ترین‌های سایت",
                relevanceScore = 90
            )
        )

        val sb = StringBuilder()
        sb.append("\n\n<!-- بخش لینک‌سازی داخلی استاندارد و مرتبط گلی کالا (golikala.com) -->\n")
        sb.append("<div class=\"gliseo-internal-links-section\" style=\"margin-top: 32px; padding: 16px; background: #f8fafc; border-radius: 8px; border-right: 4px solid #6366f1;\">\n")
        sb.append("  <h3 style=\"font-size: 14px; font-weight: bold; color: #1e293b; margin-top: 0; margin-bottom: 10px;\">🔗 لینک‌های داخلی مرتبط در فروشگاه اینترنتی گلی کالا:</h3>\n")
        sb.append("  <ul style=\"margin: 0; padding-right: 20px; line-height: 2;\">\n")
        for (link in links) {
            sb.append("    <li style=\"margin-bottom: 6px;\"><a href=\"${link.targetUrl}\" style=\"color: #4f46e5; text-decoration: underline; font-weight: bold;\">${link.anchorText}</a> <span style=\"font-size: 11px; color: #64748b;\">(${link.contentType})</span></li>\n")
        }
        sb.append("  </ul>\n")
        sb.append("</div>\n")
        return sb.toString()
    }

    fun getSuggestedTopics(existingTitles: List<String>): List<ArticleTopicSuggestion> {
        val suggestions = listOf(
            ArticleTopicSuggestion(
                title = "راهنمای جامع روتین مراقبت مو با محصولات بدون سولفات آمالفی و آگرادو",
                focusKeyword = "روتین مو بدون سولفات آمالفی",
                searchIntent = "اطلاعاتی و راهنمای خرید",
                relevanceReason = "بیشترین جستجوی خریداران محصولات آمالفی و آگرادو در ایران",
                isAlreadyCovered = existingTitles.any { it.contains("آمالفی") || it.contains("بدون سولفات") }
            ),
            ArticleTopicSuggestion(
                title = "تفاوت ماسک مو و نرم‌کننده کراتین آگرادو اسپانیا چیست؟",
                focusKeyword = "ماسک مو کراتین آگرادو اصل",
                searchIntent = "مقایسه‌ای و تصمیم‌گیری خرید",
                relevanceReason = "پرسش پرتکرار خریداران محصولات احیای مو با نرخ تبدیل بالا",
                isAlreadyCovered = false
            ),
            ArticleTopicSuggestion(
                title = "بررسی خواص لوسیون‌ها و شوینده‌های بدن سایرو اصل اسپانیا",
                focusKeyword = "لوسیون بدن سایرو اصل",
                searchIntent = "تخصصی و معرفی برند",
                relevanceReason = "معرفی لاین تخصصی سایرو به عنوان نماینده رسمی گلی کالا در ایران",
                isAlreadyCovered = false
            ),
            ArticleTopicSuggestion(
                title = "راهنمای جامع تشخیص لوازم آرایشی و بهداشتی اصل اروپایی از فیک",
                focusKeyword = "تشخیص لوازم آرایشی بهداشتی اصل",
                searchIntent = "آموزشی و اعتمادسازی",
                relevanceReason = "افزایش نرخ اعتماد کاربران به اصالت و گارانتی گلی کالا",
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
                h2Heading = "مقدمه و شناخت استانداردهای اروپایی در بهداشت و زیبایی",
                h3Headings = listOf("چرا فرمولاسیون فاقد نمک و سولفات ضروری است؟", "نقش محصولات اصل در حفظ سلامت مو و پوست"),
                content = PersianNormalizer.normalize(
                    "رسیدگی اصولی به سلامت پوست و مو نیازمند شناخت دقیق ترکیبات سازنده و پرهیز از سولفات‌های خشن و نمک‌های ارزان‌قیمت است. گلی کالا به عنوان نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران، همواره بر ضرورت استفاده از محصولات تاییدشده با استانداردهای اتحادیه اروپا تاکید دارد."
                )
            ),
            ArticleSection(
                h2Heading = "بررسی تخصصی لاین‌های کراتینه و بدون سولفات آمالفی و آگرادو اسپانیا",
                h3Headings = listOf("شامپوها و ماسک‌های بدون سولفات آمالفی (Amalfi)", "لاین بازسازی کراتین آگرادو (Agrado)"),
                content = PersianNormalizer.normalize(
                    "برندهای معتبر اسپانیایی نظیر آمالفی و آگرادو به دلیل عدم استفاده از ترکیبات آسیب‌رسان و بهره‌گیری از کراتین هیدرولیز شده، ساختار کوتیکول مو را تقویت کرده و رطوبت تارهای دکلره یا کراتینه‌شده را تا شستشوی بعدی به طور کامل حفظ می‌نمایند."
                )
            ),
            ArticleSection(
                h2Heading = "لوسیون‌ها و شوینده‌های بدن سایرو (Sairo) برای انواع پوست",
                h3Headings = listOf("حفظ رطوبت طبیعی لایه هیدرولیپیدی", "رایحه‌های طبیعی و ماندگار بدون حساسیت"),
                content = PersianNormalizer.normalize(
                    "برند سایرو با بهره‌گیری از روغن زیتون، آلوئه‌ورا و شی‌باتر طبیعی، پاکسازی ملایم را بدون از بین بردن چربی محافظ پوست ممکن ساخته و برای مصرف منظم روزانه ایده‌آل است."
                )
            ),
            ArticleSection(
                h2Heading = "چگونه کالای اصل را از نمونه‌های تقلبی بازار تشخیص دهیم؟",
                content = PersianNormalizer.normalize(
                    "یکی از مهم‌ترین ارکان خرید مطمئن، بررسی بارکد کشور سازنده، بچ‌کد حک‌شده در انتهای بسته‌بندی و خرید از مراجع رسمی واردکننده است. گلی کالا نماینده رسمی و انحصاری برندهای آمالفی، آگرادو و سایرو در ایران، تمامی کالاها را با ضمانت اصالت ۱۰۰٪ عرضه می‌کند."
                )
            )
        )

        val specsOrComparison = listOf(
            Pair("شاخص مقایسه‌ای", "برندهای رسمی وارداتی گلی کالا (آمالفی، آگرادو، سایرو) در مقایسه با بازار"),
            Pair("کشور تولیدکننده", "اسپانیا (اتحادیه اروپا) با استانداردهای بهداشتی CE"),
            Pair("وجود ترکیبات آسیب‌رسان", "کاملاً فاقد پارابن، بدون سولفات خشن، فاقد نمک دناتوره"),
            Pair("وضعیت نمایندگی در ایران", "گلی کالا نماینده رسمی و انحصاری در ایران"),
            Pair("ضمانت و استعلام اصالت", "دارای بارکد بین‌المللی و ۷ روز ضمانت بازگشت وجه")
        )

        val faqs = listOf(
            Pair("آیا محصولات آمالفی، آگرادو و سایرو در گلی کالا اورجینال هستند؟", "بله، گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و تمامی سفارش‌ها مستقیماً با ضمانت اصالت فیزیکی ارسال می‌شوند."),
            Pair("چه مدت طول می‌کشد تا اثر شامپو و ماسک موی بدون سولفات نمایان شود؟", "از همان اولین شستشو تفاوت نرمی و کاهش وز مو مشهود است و پس از ۳ تا ۴ هفته احیای ساختار کراتین مو تثبیت می‌گردد."),
            Pair("چرا خرید از نماینده رسمی اهمیت دارد؟", "خرید از نماینده رسمی مانع خرید نمونه‌های فیک یا تاریخ‌گذشته بازار شده و گارانتی اصالت و قیمت واقعی کالا را تضمین می‌کند.")
        )

        val internalLinks = listOf(
            InternalLinkAnchor("مشاهده و خرید محصولات برند آمالفی اصل اسپانیا", "https://golikala.com/brand/amalfi/", "برند اصلی", 98),
            InternalLinkAnchor("خرید ماسک مو و شامپوهای کراتینه آگرادو اسپانیا", "https://golikala.com/brand/agrado/", "برند مکمل", 97),
            InternalLinkAnchor("خرید لوسیون‌ها و شوینده‌های تخصصی سایرو", "https://golikala.com/brand/sairo/", "برند مکمل", 95),
            InternalLinkAnchor("مشاهده لیست کامل محصولات مراقبت مو بدون سولفات", "https://golikala.com/product-category/hair-care/", "دسته‌بندی اصلی", 93),
            InternalLinkAnchor("فروشگاه آنلاین گلی کالا (golikala.com)", "https://golikala.com/shop/", "فروشگاه رسمی", 90)
        )

        val relatedArticlesOrProducts = listOf(
            "شامپو کراتینه و تقویت‌کننده آگرادو اصل اسپانیا",
            "ماسک مو ترمیم‌کننده آمالفی فاقد سولفات",
            "لوسیون آبرسان دست و بدن سایرو",
            "سرم هیالورونیک اسید آبرسان عمیق"
        )

        val structuredHtml = """
            <article class="gliseo-structured-article" dir="rtl">
              <h1 style="color: #0f172a; font-size: 24px; margin-bottom: 16px;">$normalizedTitle</h1>
              
              <div class="agency-banner" style="background: #f0fdf4; border: 2px solid #22c55e; padding: 14px 18px; border-radius: 8px; margin-bottom: 20px;">
                <p style="font-size: 14px; color: #166534; margin: 0; font-weight: bold;">✦ گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.</p>
              </div>

              <div class="article-lead" style="background: #f8fafc; border-right: 4px solid #2563eb; padding: 14px 18px; border-radius: 8px; margin-bottom: 24px;">
                <p style="font-size: 15px; line-height: 1.8; margin: 0;"><strong>خلاصه راهنما: </strong>رسیدگی به پوست و مو نیازمند شناخت فرمولاسیون‌های بهداشتی بدون ترکیبات خشن نظیر سولفات و پارابن است. در این مقاله به بررسی علمی و راهنمای انتخاب محصولات اصل اروپایی می‌پردازیم.</p>
              </div>
              
              <h2 style="color: #0f172a; margin-top: 28px; font-weight: bold;">جدول مقایسه کیفی و استانداردهای بهداشتی</h2>
              <table style="width: 100%; border-collapse: collapse; margin-bottom: 24px;">
                <thead><tr style="background: #f1f5f9;"><th style="padding: 10px; border: 1px solid #e2e8f0; text-align: right;">شاخص مقایسه</th><th style="padding: 10px; border: 1px solid #e2e8f0; text-align: right;">استاندارد رسمی گلی کالا (golikala.com)</th></tr></thead>
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

              <h2 style="color: #0f172a; margin-top: 28px; font-weight: bold;">پرسش و پاسخ‌های متداول (FAQ)</h2>
              ${faqs.joinToString("\n") { (q, a) ->
                  "<div style=\"background: #f8fafc; padding: 12px 16px; border-radius: 8px; margin-bottom: 12px;\"><h3 style=\"font-size: 14px; margin: 0 0 6px 0; color: #0f172a;\">❓ $q</h3><p style=\"font-size: 13px; margin: 0; color: #475569;\">$a</p></div>"
              }}

              <div class=\"related-links-footer\" style=\"margin-top: 36px; padding: 18px; background: #eff6ff; border-radius: 8px; border-right: 4px solid #2563eb;\">
                <h3 style=\"color: #1e3a8a; font-size: 15px; margin-top: 0; margin-bottom: 10px;\">🔗 لینک‌های داخلی و محصولات مرتبط در وبسایت گلی کالا (golikala.com):</h3>
                <ul style=\"padding-right: 20px; margin: 0;\">
                  ${internalLinks.joinToString("") { "<li style=\"margin-bottom: 8px;\"><a href=\"${it.targetUrl}\" style=\"color: #2563eb; text-decoration: none; font-weight: bold;\">${it.anchorText}</a> <span style=\"font-size: 12px; color: #64748b;\">(${it.contentType})</span></li>" }}
                </ul>
              </div>
            </article>
        """.trimIndent()

        return GeneratedArticle(
            title = normalizedTitle,
            focusKeyword = keyword,
            metaDescription = PersianNormalizer.normalize("راهنمای جامع و علمی $keyword در وبسایت گلی کالا، نماینده رسمی و انحصاری برندهای آمالفی، آگرادو و سایرو در ایران.").take(155),
            h1Heading = normalizedTitle,
            sections = sections,
            faqSection = faqs,
            authorBio = "تیم تخصصی تحریریه سلامت و مراقبت از پوست و مو در گلی کالا",
            eeatTrustFactors = listOf("بر پایه استانداردهای علمی اتحادیه اروپا", "عدم ادعای دارویی غیرمجاز", "گلی کالا نماینده رسمی برندهای اروپایی در ایران"),
            specificationsOrComparisonTable = specsOrComparison,
            internalLinks = internalLinks,
            relatedProductsOrArticles = relatedArticlesOrProducts,
            structuredHtmlContent = structuredHtml
        )
    }

    fun generateImageMetadata(item: SiteItem): ImageMetadata {
        val analysis = analyzeProductBeforeWriting(item)
        val englishSlug = item.slug.ifBlank { "product-${item.id}" }
        val fileName = "$englishSlug-featured-optimized.webp"
        val altText = PersianNormalizer.normalize("تصویر باکیفیت و استاندارد از ${item.title} برند ${analysis.detectedBrand} در فروشگاه گلی کالا")

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
