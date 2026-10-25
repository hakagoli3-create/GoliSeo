package com.example.data.model

data class BrandFact(
    val brandName: String,
    val origin: String,
    val philosophy: String,
    val certifications: List<String>,
    val safetyCautions: List<String>,
    val verifiedClaims: List<String>
)

data class ApprovedSample(
    val id: String,
    val category: String, // "محصول", "مقاله", "متا"
    val originalText: String,
    val approvedText: String,
    val keyLearning: String
)

data class BrandKnowledgeBase(
    val storeName: String = "گلی کالا (GoliKala)",
    val brandTone: String = "صادقانه، علمی، صمیمی، دلسوز و متمرکز بر سلامت واقعی پوست بدون بزرگ‌نمایی",
    val audienceProfile: String = "مصرف‌کنندگان آگاه، علاقه‌مندان به روتین‌های مراقبت از پوست و مو، خریداران شوینده‌ها و محصولات بهداشتی استاندارد",
    val allowedTerms: List<String> = listOf(
        "آبرسانی موثر",
        "حفظ رطوبت طبیعی پوست",
        "سازگار با انواع پوست",
        "فرمولاسیون ملایم",
        "تاییدشده توسط آزمایشگاه‌های معتبر",
        "فاقد ترکیبات حساسیت‌زا",
        "ضمانت اصالت ۷ روزه",
        "فاقد پارابن و الکل مضر"
    ),
    val forbiddenTerms: List<String> = listOf(
        "درمان قطعی در ۳ روز",
        "معجزه پزشکی",
        "۱۰۰٪ تضمینی بدون بازگشت",
        "ارزان‌ترین قیمت بازار",
        "بدون رقیب در جهان",
        "داروی شفا‌بخش پوست",
        "حذف دائمی چین و چروک"
    ),
    val officialBrandFacts: List<BrandFact> = listOf(
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
        ),
        BrandFact(
            brandName = "هیدرودرم (Hydroderm)",
            origin = "ایران - لابراتوار پارس حیان",
            philosophy = "فرمولاسیون تخصصی برای پوست‌های خشک، آسیب‌دیده و خیلی حساس",
            certifications = listOf("تاییدیه بهداشت و غذا و دارو"),
            safetyCautions = listOf("از تماس با چشم و زخم‌های باز خودداری شود"),
            verifiedClaims = listOf("تسکین خارش و خشکی شدید", "ترمیم لایه هیدرولیپیدی پوست")
        )
    ),
    val approvedSamples: List<ApprovedSample> = listOf(
        ApprovedSample(
            id = "sample_1",
            category = "محصول",
            originalText = "کرم معجزه‌گر رفع کامل جوش و لک صد در صد تضمینی",
            approvedText = "کرم ژل ضد جوش موثر، کمک به تنظیم چربی و پاکسازی منافذ پوست مستعد آکنه",
            keyLearning = "حذف کلمات ادعای پزشکی (معجزه‌گر، صد در صد) و جایگزینی با تعابیر علمی و واقع‌بینانه"
        ),
        ApprovedSample(
            id = "sample_2",
            category = "متا",
            originalText = "ارزان‌ترین قیمت کرم لافارر در کل ایران فقط اینجا",
            approvedText = "خرید کرم آبرسان لافارر اصل | ضمانت اصالت و تاریخ انقضای معتبر در گلی کالا",
            keyLearning = "تمرکز روی اصالت کالا و اعتماد به جای شعارهای تبلیغاتی منفی"
        )
    ),
    val totalLearnedEditsCount: Int = 14
)
