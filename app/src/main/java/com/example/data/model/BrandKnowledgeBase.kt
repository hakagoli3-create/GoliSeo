package com.example.data.model

data class BrandFact(
    val brandName: String,
    val origin: String,
    val philosophy: String,
    val certifications: List<String>,
    val safetyCautions: List<String>,
    val verifiedClaims: List<String>,
    val officialAgencyNote: String = ""
)

data class ApprovedSample(
    val id: String,
    val category: String, // "محصول", "مقاله", "متا"
    val originalText: String,
    val approvedText: String,
    val keyLearning: String
)

data class BrandKnowledgeBase(
    val storeName: String = "گلی کالا (golikala.com)",
    val storeRole: String = "وبسایت و فروشگاه آنلاین رسمی، نماینده رسمی و انحصاری برندهای آمالفی، آگرادو و سایرو در ایران و واردکننده محصولات اصل اروپایی (هرگز به عنوان تولیدکننده یا برند سازنده کالا درج نشود)",
    val brandTone: String = "تخصصی، صادقانه، علمی، بهداشتی و متمرکز بر سلامت واقعی پوست و مو بدون بزرگ‌نمایی",
    val audienceProfile: String = "مصرف‌کنندگان آگاه، خریداران محصولات مراقبت از پوست، مو و بهداشت فردی اصل اروپایی",
    val officialAgencyStatement: String = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
    val allowedTerms: List<String> = listOf(
        "گلی کالا نماینده رسمی و انحصاری برندهای آمالفی، آگرادو و سایرو در ایران",
        "واردکننده مستقیم محصولات اصل اروپایی",
        "تضمین ۱۰۰٪ اصالت فیزیکی و آزمایشگاهی",
        "آبرسانی موثر و حفظ رطوبت طبیعی",
        "سازگار با انواع پوست و مو",
        "فرمولاسیون فاقد نمک خشن و سولفات",
        "تاییدشده توسط آزمایشگاه‌های درماتولوژی اروپا",
        "ضمانت اصالت و بازگشت ۷ روزه کالا"
    ),
    val forbiddenTerms: List<String> = listOf(
        "درج گلی کالا به عنوان سازنده یا تولیدکننده محصول",
        "درمان قطعی در ۳ روز",
        "معجزه پزشکی",
        "۱۰۰٪ تضمینی بدون بازگشت",
        "ارزان‌ترین قیمت بازار",
        "بدون رقیب در جهان",
        "حذف دائمی چین و چروک"
    ),
    val officialBrandFacts: List<BrandFact> = listOf(
        BrandFact(
            brandName = "آمالفی (Amalfi)",
            origin = "اسپانیا (اتحادیه اروپا) - Quimi Romar S.L.U",
            philosophy = "تولید تخصصی شوینده‌ها، شامپوها و ماسک‌های مراقبت مو بدون سولفات با فرمولاسیون استاندارد اروپایی و حجم اقتصادی",
            certifications = listOf("استاندارد بهداشتی اتحادیه اروپا (CE)", "گواهی اصالت بارکد بین‌المللی", "ISO 22716"),
            safetyCautions = listOf("فقط برای استعمال خارجی", "دور از تابش مستقیم نور خورشید"),
            verifiedClaims = listOf("تغذیه عمقی فیبر موهای آسیب‌دیده", "فرمولاسیون هایپوآلرژنیک", "بدون تست حیوانی"),
            officialAgencyNote = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد."
        ),
        BrandFact(
            brandName = "آگرادو (Agrado)",
            origin = "اسپانیا (اتحادیه اروپا) - والنسیا",
            philosophy = "لاین‌های تخصصی کراتین، ضد ریزش و بوتاکس مو با فرمولاسیون درماتولوژیک پیشرفته روز دنیا",
            certifications = listOf("تاییدیه تست‌های درماتولوژی اروپا", "گواهی استانداردهای بهداشتی اتحادیه اروپا"),
            safetyCautions = listOf("در صورت تماس با چشم با آب فراوان شستشو داده شود"),
            verifiedClaims = listOf("تثبیت و احیای ماندگار موهای کراتینه‌شده", "محافظت در برابر حرارت سشوار و اتو", "افزایش لطافت و درخشش ابریشمی"),
            officialAgencyNote = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد."
        ),
        BrandFact(
            brandName = "سایرو (Sairo)",
            origin = "اسپانیا (اتحادیه اروپا)",
            philosophy = "لوسیون‌ها، شوینده‌های بدن و کرم‌های مراقبتی بر پایه عصاره‌های خالص زیتون، آلوئه‌ورا و روغن بادام",
            certifications = listOf("استانداردهای بهداشتی اروپا", "تاییدیه درماتولوژیست‌های پوست"),
            safetyCautions = listOf("در جای خشک و خنک نگهداری شود"),
            verifiedClaims = listOf("حفظ رطوبت طبیعی لایه هیدرولیپیدی تا ۲۴ ساعت", "رایحه ماندگار طبیعی", "فاقد پارابن"),
            officialAgencyNote = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد."
        ),
        BrandFact(
            brandName = "لافارر (La Farrerr)",
            origin = "ایران با فرمولاسیون آزمایشگاه‌های فرانسه",
            philosophy = "تولید تخصصی محصولات مراقبت از پوست و مو بر پایه گیاهان دارویی و علم نوین",
            certifications = listOf("سیب سلامت", "استاندارد بین‌المللی ISO 9001"),
            safetyCautions = listOf("فقط برای استعمال خارجی", "در صورت بروز حساسیت مصرف متوقف شود"),
            verifiedClaims = listOf("کنترل ترشح چربی پوست", "کاهش التهابات ناشی از آکنه")
        ),
        BrandFact(
            brandName = "سینره (Cinere)",
            origin = "ایران - شرکت دانش‌بنیان طبیعت زنده",
            philosophy = "بهره‌گیری از عصاره‌های خالص گیاهی با رویکرد عدم تست حیوانی (Cruelty Free)",
            certifications = listOf("تاییدیه درماتولوژیست‌های اروپایی", "گواهینامه تاییدیه ایزو"),
            safetyCautions = listOf("دور از تابش مستقیم نور خورشید نگهداری شود"),
            verifiedClaims = listOf("روشن‌کنندگی تدریجی و طبیعی", "تقویت سد دفاعی پوست")
        )
    ),
    val approvedSamples: List<ApprovedSample> = listOf(
        ApprovedSample(
            id = "sample_agency_1",
            category = "هویت تجاری",
            originalText = "شامپو آمالفی تولید گلی کالا ساخت ایران با قیمت ارزان",
            approvedText = "شامپو کراتینه آمالفی (Amalfi) اصل اسپانیا فاقد سولفات | گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
            keyLearning = "گلی کالا هرگز سازنده نیست؛ نماینده رسمی و واردکننده انحصاری محصولات اصل اروپایی است."
        ),
        ApprovedSample(
            id = "sample_seo_2",
            category = "سئو و جدول",
            originalText = "ماسک مو آگرادو خیلی خوبه و معجزه میکنه برای موهای وز",
            approvedText = "ماسک مو ترمیم‌کننده آگرادو (Agrado) اصل اسپانیا با فرمولاسیون کراتین هیدرولیز شده، فاقد پارابن به همراه جدول مشخصات و لینک‌های داخلی golikala.com",
            keyLearning = "تولید توضیحات جذاب، جدول مشخصات فنی و لینک‌سازی داخلی واقعی به سایت golikala.com."
        )
    ),
    val totalLearnedEditsCount: Int = 18
)
