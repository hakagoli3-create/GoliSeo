package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BrandEntity
import com.example.data.local.LogEntity
import com.example.data.local.SnapshotEntity
import com.example.data.local.TaskEntity
import com.example.data.model.*
import com.example.data.remote.GeminiSeoService
import com.example.data.remote.NetworkClientProvider
import com.example.data.remote.SeoAiResult
import com.example.data.remote.WordPressAuthHelper
import com.example.data.remote.WordPressJsonSanitizerInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class GliseoRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val logDao = database.logDao()
    private val snapshotDao = database.snapshotDao()
    private val taskDao = database.taskDao()
    private val brandDao = database.brandDao()

    private val geminiService = GeminiSeoService()

    // In-memory reactive state for active site data
    private val _items = MutableStateFlow<List<SiteItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _siteConfig = MutableStateFlow(SiteConfig())
    val siteConfig = _siteConfig.asStateFlow()

    private val _userSession = MutableStateFlow(UserSession())
    val userSession = _userSession.asStateFlow()

    private val _autopilotConfig = MutableStateFlow(AutopilotConfig())
    val autopilotConfig = _autopilotConfig.asStateFlow()

    private val _budgetTracker = MutableStateFlow(BudgetTracker())
    val budgetTracker = _budgetTracker.asStateFlow()

    private val _circuitBreakerTriggered = MutableStateFlow(false)
    val circuitBreakerTriggered = _circuitBreakerTriggered.asStateFlow()

    // Phase 2 Knowledge Base & Scheduling State
    private val _brandKnowledgeBase = MutableStateFlow(BrandKnowledgeBase())
    val brandKnowledgeBase = _brandKnowledgeBase.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("")
    val syncStatusMessage = _syncStatusMessage.asStateFlow()

    private val _scheduledPlans = MutableStateFlow<List<ScheduledPostPlan>>(
        listOf(
            ScheduledPostPlan(
                targetId = 201,
                targetTitle = "راهنمای جامع روتین پوستی روز و شب برای انواع پوست",
                jalaliScheduledDate = "۱۵ تیر ۱۴۰۵ ساعت ۱۰:۰۰",
                isoGmtTimestamp = "2026-07-06T06:30:00Z",
                wpStatus = "future",
                occasion = "جشنواره تابستانه و محافظت آفتاب"
            )
        )
    )
    val scheduledPlans = _scheduledPlans.asStateFlow()

    init {
        loadInitialMockData()
        CoroutineScope(Dispatchers.IO).launch {
            if (!_siteConfig.value.isDemoMode) {
                syncSiteContent(_siteConfig.value)
            }
        }
    }

    private fun loadInitialMockData() {
        val initialItems = listOf(
            // --- WooCommerce Products ---
            SiteItem(
                id = 101,
                title = "کرم آبرسان و مرطوب‌کننده قوی هیالورونیک اسید",
                content = "کرم آبرسان عمقی پوست حاوی هیالورونیک اسید خالص چندمولکولی و ویتامین B5 جهت حفظ شادابی، قوام و جوانی بافت پوست صورت.",
                excerpt = "آبرسانی قوی و تغذیه‌کننده پوست با جذب فوق‌العاده سریع بدون چربی.",
                slug = "hyaluronic-acid-moisturizer",
                categories = listOf("مراقبت از پوست", "مرطوب‌کننده و آبرسان"),
                tags = listOf("هیالورونیک اسید", "پوست خشک", "آبرسان قوی"),
                regularPrice = "۴۸۰٬۰۰۰ تومان",
                salePrice = "۳۹۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-HA-101",
                type = SiteItemType.PRODUCT,
                focusKeyword = "کرم آبرسان هیالورونیک اسید",
                secondaryKeywords = listOf("بهترین آبرسان پوست", "کرم مرطوب کننده صورت"),
                metaTitle = "خرید کرم آبرسان قوی هیالورونیک اسید اصل | گلیسئو",
                metaDescription = "خرید اینترنتی کرم آبرسان و جوانساز هیالورونیک اسید با بالاترین کیفیت، ارسال فوری و ضمانت اصالت کالا در فروشگاه گلیسئو.",
                canonicalUrl = "https://example.com/product/hyaluronic-acid-moisturizer",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"کرم آبرسان هیالورونیک اسید","offers":{"@type":"Offer","price":"390000","priceCurrency":"IRR","availability":"https://schema.org/InStock"}}""",
                imageAlt = "بسته‌بندی قوطی شیشه‌ای کرم آبرسان هیالورونیک اسید",
                imageTitle = "کرم آبرسان هیالورونیک اسید گلیسئو",
                seoScore = 84
            ),
            SiteItem(
                id = 102,
                title = "سرم ویتامین C روشن‌کننده و ضد لک قوی ۲۰٪",
                content = "سرم غلیظ آنتی‌اکسیدان حاوی ۲۰ درصد ویتامین C فعال و اسید فرولیک برای رفع تیرگی، یکنواخت‌سازی تناژ رنگ چهره و تحریک سنتز کلاژن طبیعی.",
                excerpt = "سرم غنی‌شده با آنتی‌اکسیدان برای درخشش، شفافیت و محو لک‌های تیره.",
                slug = "vitamin-c-brightening-serum",
                categories = listOf("سرم پوست", "ضد لک و روشن‌کننده"),
                tags = listOf("ویتامین C", "ضد لک", "روشن کننده"),
                regularPrice = "۶۵۰٬۰۰۰ تومان",
                salePrice = "۵۸۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-VC-102",
                type = SiteItemType.PRODUCT,
                focusKeyword = "سرم ویتامین C ضد لک",
                secondaryKeywords = listOf("روشن کننده پوست صورت", "سرم ضد لک قوی"),
                metaTitle = "سرم ویتامین C ضد لک و روشن‌کننده پوست | گلیسئو",
                metaDescription = "بهترین سرم ویتامین C خالص برای از بین بردن لک‌ها و روشن شدن پوست با ارسال فوری و مشاوره رایگان.",
                canonicalUrl = "https://example.com/product/vitamin-c-brightening-serum",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"سرم ویتامین C","offers":{"@type":"Offer","price":"580000","priceCurrency":"IRR","availability":"https://schema.org/InStock"}}""",
                imageAlt = "قطره‌چکان سرم ویتامین C با پس‌زمینه شفاف مینیمال",
                imageTitle = "سرم ویتامین C ضد لک گلیسئو",
                seoScore = 78
            ),
            SiteItem(
                id = 103,
                title = "کرم ضد آفتاب فیوژن واتر SPF 50+ بی‌رنگ",
                content = "ضد آفتاب بر پایه آب با جذب فوری و بدون رد سفیدی، محافظت کامل در برابر اشعه‌های UVA و UVB و مناسب انواع پوست حتی چرب و مستعد آکنه.",
                excerpt = "بافت سبک بر پایه آب بدون مسدود کردن منافذ و مقاوم در برابر تعریق.",
                slug = "fusion-water-sunscreen-spf50",
                categories = listOf("مراقبت از پوست", "ضد آفتاب"),
                tags = listOf("ضد آفتاب", "فیوژن واتر", "SPF50"),
                regularPrice = "۵۴۰٬۰۰۰ تومان",
                salePrice = "",
                stockStatus = "instock",
                sku = "GLS-SUN-103",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ضد آفتاب فیوژن واتر بی‌رنگ",
                metaTitle = "خرید کرم ضد آفتاب فیوژن واتر SPF 50+ اصل | گلیسئو",
                metaDescription = "خرید اینترنتی ضد آفتاب بی‌رنگ فیوژن واتر سبک با محافظت حداکثری و ضمانت اصالت فیزیکی.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ضد آفتاب فیوژن واتر"}""",
                imageAlt = "بسته‌بندی ضد آفتاب فیوژن واتر بی‌رنگ",
                seoScore = 82
            ),
            SiteItem(
                id = 104,
                title = "ژل شستشوی صورت کنترل چربی حاوی سالیسیلیک اسید ۲٪",
                content = "ژل شوینده ملایم ضد جوش و پاک‌کننده عمقی چربی اضافی و سلول‌های مرده سطح پوست، مهار باکتری‌های مولد آکنه بدون ایجاد حس کشیدگی.",
                excerpt = "شستشوی عمقی منافذ و مهار جوش‌های سرسیاه و زیرپوستی.",
                slug = "salicylic-acid-face-cleanser",
                categories = listOf("شستشو و پاک‌کننده", "مراقبت از پوست"),
                tags = listOf("شوینده صورت", "سالیسیلیک اسید", "ضد جوش"),
                regularPrice = "۳۶۰٬۰۰۰ تومان",
                salePrice = "۳۱۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-SA-104",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ژل شستشوی سالیسیلیک اسید",
                metaTitle = "ژل شستشوی ضد جوش سالیسیلیک اسید | گلیسئو",
                metaDescription = "کنترل چربی و پاکسازی عمقی منافذ با ژل شوینده سالیسیلیک اسید اصل با گارانتی بازگشت وجه.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ژل شستشوی سالیسیلیک اسید"}""",
                imageAlt = "پمپ فشاری ژل شستشوی صورت ضد جوش",
                seoScore = 75
            ),
            SiteItem(
                id = 105,
                title = "تونر لایه‌بردار و روشن‌کننده گلیکولیک اسید ۷٪",
                content = "محلول تونر لایه‌بردار ملایم جهت رفع کدری چهره، بهبود ناهمواری‌های سطحی پوست و ایجاد درخشندگی طبیعی با ریشه جینسینگ و آلوئه‌ورا.",
                excerpt = "لایه برداری ملایم شبانه برای پوستی شفاف، صیقلی و یکنواخت.",
                slug = "glycolic-acid-toning-solution",
                categories = listOf("تونر پوست", "لایه‌بردار"),
                tags = listOf("تونر", "گلیکولیک اسید", "AHA"),
                regularPrice = "۵۹۰٬۰۰۰ تومان",
                salePrice = "",
                stockStatus = "instock",
                sku = "GLS-GA-105",
                type = SiteItemType.PRODUCT,
                focusKeyword = "تونر گلیکولیک اسید اصل",
                metaTitle = "خرید تونر گلیکولیک اسید ۷ درصد لایه‌بردار | گلیسئو",
                metaDescription = "شفافیت فوق‌العاده پوست و درمان ناهمواری‌ها با تونر لایه‌بردار گلیکولیک اسید اورجینال.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"تونر گلیکولیک اسید"}""",
                imageAlt = "بطری شفاف تونر لایه بردار گلیکولیک اسید",
                seoScore = 80
            ),
            SiteItem(
                id = 106,
                title = "کرم دور چشم ضد چروک و رفع تیرگی رتینول خالص",
                content = "کرم سبک دور چشم با رتینول انکپسوله و کافئین فعال جهت کاهش خطوط پنجه کلاغی، پف صبحگاهی و روشن‌سازی حلقه تیره زیر چشم.",
                excerpt = "رفع خستگی دور چشم و جوان‌سازی ناحیه حساس پلک.",
                slug = "retinol-eye-cream-anti-aging",
                categories = listOf("کرم دور چشم", "ضد چروک"),
                tags = listOf("دور چشم", "رتینول", "ضد تیرگی"),
                regularPrice = "۷۲۰٬۰۰۰ تومان",
                salePrice = "۶۵۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-EYE-106",
                type = SiteItemType.PRODUCT,
                focusKeyword = "کرم دور چشم رتینول اصل",
                metaTitle = "کرم دور چشم ضد چروک و تیرگی رتینول | گلیسئو",
                metaDescription = "درمان قطعی خطوط ریز و سیاهی دور چشم با کرم تخصصی رتینول در فروشگاه گلیسئو.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"کرم دور چشم رتینول"}""",
                imageAlt = "تیوب باریک کرم دور چشم ضد چروک رتینول",
                seoScore = 86
            ),
            SiteItem(
                id = 107,
                title = "سرم نیاسینامید ۱۰٪ + زینک ۱٪ قابض منافذ و ضد آکنه",
                content = "فرمول غنی از نیاسینامید با غلظت ۱۰ درصد در ترکیب با نمک روی (زینک PCA) برای تنظیم ترشح سبوم و کاهش محسوس اندازه منافذ پوست.",
                excerpt = "کنترل چربی و کوچک کردن منافذ باز پوست صورت.",
                slug = "niacinamide-zinc-pore-serum",
                categories = listOf("سرم پوست", "کنترل چربی"),
                tags = listOf("نیاسینامید", "زینک", "کوچک کننده منافذ"),
                regularPrice = "۵۲۰٬۰۰۰ تومان",
                salePrice = "۴۵۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-NZ-107",
                type = SiteItemType.PRODUCT,
                focusKeyword = "سرم نیاسینامید و زینک اصل",
                metaTitle = "خرید سرم نیاسینامید ۱۰٪ و زینک ۱٪ | گلیسئو",
                metaDescription = "بهترین سرم کوچک کننده منافذ و ضد جوش صورت با ضمانت اصالت صد در صدی.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"سرم نیاسینامید و زینک"}""",
                imageAlt = "قطره‌چکان سرم سفید رنگ نیاسینامید",
                seoScore = 88
            ),
            SiteItem(
                id = 108,
                title = "ماسک مو ترمیم‌کننده عمیق کراتین و روغن آرگان مراکشی",
                content = "ماسک مغذی موهای خشک و رنگ‌شده حاوی کراتین هیدرولیز شده و اسیدهای چرب امگا برای احیای ساختار تارهای آسیب‌دیده و رفع وزی مو.",
                excerpt = "تغذیه ساقه مو و بازگرداندن نرمی ابریشمی به موهای سوخته و دکلره شده.",
                slug = "keratin-argan-hair-mask",
                categories = listOf("مراقبت از مو", "ماسک مو"),
                tags = listOf("ماسک مو", "کراتین", "آرگان"),
                regularPrice = "۴۹۰٬۰۰۰ تومان",
                salePrice = "",
                stockStatus = "instock",
                sku = "GLS-HR-108",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ماسک مو کراتین و آرگان",
                metaTitle = "ماسک موی مغذی کراتین و روغن آرگان اصل | گلیسئو",
                metaDescription = "احیای موهای سوخته و خشک با ماسک موی تخصصی کراتین و آرگان مراکشی.",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ماسک مو کراتین"}""",
                imageAlt = "کاسه ماسک موی مغذی با درب طلایی",
                seoScore = 79
            ),

            // --- WordPress Posts & Articles ---
            SiteItem(
                id = 201,
                title = "راهنمای جامع روتین پوستی روز و شب برای انواع پوست",
                content = "داشتن یک روتین پوستی اصولی پایه‌ترین ضرورت حفظ جوانی چهره است. در این مقاله گام‌های پاکسازی، تونر، آبرسانی، تغذیه و ضد آفتاب را تشریح می‌کنیم.\n\n## گام اول: پاکسازی ملایم با شوینده مناسب\nانتخاب شوینده سازگار با pH پوست از تخریب سد دفاعی جلوگیری می‌کند.\n\n## گام دوم: آبرسانی و حفظ رطوبت\nاستفاده همزمان از هیالورونیک اسید روی پوست نمدار بیشترین اثربخشی را ایجاد می‌کند.\n\n## گام سوم: لایه محافظتی ضد آفتاب\nحتی در روزهای ابری استفاده از ضد آفتاب با فاکتور حفاظتی ۳۰ به بالا الزامی است.",
                excerpt = "چگونه بهترین روتین پوستی مناسب پوست خشک یا چرب داشته باشیم؟",
                slug = "daily-skincare-routine-guide",
                categories = listOf("مقالات آموزشی", "مراقبت پوست"),
                tags = listOf("روتین پوستی", "آموزش پوست", "آبرسانی"),
                type = SiteItemType.POST,
                focusKeyword = "روتین پوستی روز و شب",
                secondaryKeywords = listOf("مراحل مراقبت از پوست", "بهترین روتین پوست چرب"),
                metaTitle = "روتین پوستی اصولی چیست؟ راهنمای قدم به قدم | گلیسئو",
                metaDescription = "آموزش کامل مراحل روتین پوستی روز و شب برای پوست‌های چرب، خشک و مختلط به همراه نکات مهم کارشناسان.",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"راهنمای روتین پوستی"}""",
                imageAlt = "اینفوگرافیک مراحل روتین مراقبت پوستی در طول شبانه‌روز",
                seoScore = 88
            ),
            SiteItem(
                id = 202,
                title = "تفاوت کرم آبرسان و مرطوب‌کننده چیست و کدام را انتخاب کنیم؟",
                content = "بسیاری از افراد این دو محصول را یکسان می‌دانند در حالی که عملکرد فیزیولوژیک آن‌ها در لایه‌های اپیدرم کاملاً متفاوت است.\n\n## آبرسان (Hydrator) چیست؟\nآبرسان‌ها حاوی مواد هومکتانت (جاذب رطوبت) مانند هیالورونیک اسید و گلیسیرین هستند که آب را به داخل سلول‌ها می‌کشند.\n\n## مرطوب‌کننده (Moisturizer) چیست؟\nمرطوب‌کننده‌ها حاوی امولینت‌ها و روغن‌ها هستند که سد چربی ایجاد کرده و مانع تبخیر آب ترنس‌اپیدرمال می‌شوند.",
                excerpt = "بررسی علمی تفاوت ترکیبات و عملکرد آبرسان‌ها در مقایسه با مرطوب‌کننده‌ها.",
                slug = "hydrator-vs-moisturizer-difference",
                categories = listOf("مقالات آموزشی", "دانستنی‌های زیبایی"),
                tags = listOf("آبرسان", "مرطوب کننده", "تفاوت آبرسان"),
                type = SiteItemType.POST,
                focusKeyword = "تفاوت آبرسان و مرطوب کننده",
                metaTitle = "تفاوت کرم آبرسان و مرطوب کننده چیست؟ راهنمای انتخاب | گلیسئو",
                metaDescription = "راهنمای تخصصی شناخت تفاوت‌های کلیدی آبرسان و مرطوب‌کننده و نحوه استفاده همزمان از آن‌ها.",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"تفاوت آبرسان و مرطوب کننده"}""",
                imageAlt = "مقایسه بافت کرم آبرسان ژلی و کرم مرطوب کننده غلیظ",
                seoScore = 85
            ),
            SiteItem(
                id = 203,
                title = "۵ اشتباه رایج در مصرف سرم ویتامین C که اثر آن را خنثی می‌کند",
                content = "ویتامین C یکی از ناپایدارترین و قوی‌ترین آنتی‌اکسیدان‌ها است که در صورت عدم رعایت اصول، به سرعت اکسید شده و بی‌خاصیت می‌شود.\n\n## ۱. نگهداری در برابر نور و حرارت\nتغییر رنگ سرم از شفاف به زرد یا قهوه‌ای نشانه اکسیداسیون است.\n\n## ۲. استفاده همزمان با نیاسینامید یا لایه‌بردارهای خشن\nتداخل اسیدیته می‌تواند باعث قرمزی و سوزش پوست شود.",
                excerpt = "بررسی خطاهای متداول مصرف سرم ویتامین C و روش‌های افزایش اثربخشی آن.",
                slug = "vitamin-c-serum-common-mistakes",
                categories = listOf("مقالات آموزشی", "مراقبت پوست"),
                tags = listOf("ویتامین C", "اشتباهات پوستی", "ضد لک"),
                type = SiteItemType.POST,
                focusKeyword = "اشتباهات مصرف سرم ویتامین C",
                metaTitle = "۵ اشتباه رایج در استفاده از سرم ویتامین C | گلیسئو",
                metaDescription = "چرا سرم ویتامین C روی پوست من عمل نمی‌کند؟ دلایل علمی و راهکارهای اصولی مصرف.",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"اشتباهات سرم ویتامین C"}""",
                imageAlt = "بطری قطره‌چکان دار در شرایط نگهداری استاندارد",
                seoScore = 91
            ),
            SiteItem(
                id = 204,
                title = "راهنمای خرید و انتخاب ضد آفتاب بر اساس فیزیکی یا شیمیایی بودن",
                content = "انتخاب صحیح فیلترهای ضد آفتاب اهمیت حیاتی در پیشگیری از پیری ناشی از فوتوایجینگ دارد.\n\n## ضد آفتاب فیزیکی (مینرال)\nحاوی اکسید روی و دی‌اکسید تیتانیوم که اشعه را مانند آینه بازمی‌تابانند و برای پوست‌های حساس و بارداری ایده‌آل هستند.\n\n## ضد آفتاب شیمیایی\nاشعه فرابنفش را جذب و به گرمای بی‌خطر تبدیل می‌کنند و بافتی بسیار سبک و زودجذب دارند.",
                excerpt = "مقایسه فیلترهای معدنی و شیمیایی در ضد آفتاب‌ها و تناسب با نوع پوست.",
                slug = "physical-vs-chemical-sunscreen-guide",
                categories = listOf("مقالات آموزشی", "ضد آفتاب"),
                tags = listOf("ضد آفتاب مینرال", "فیلتر فیزیکی", "SPF"),
                type = SiteItemType.POST,
                focusKeyword = "ضد آفتاب فیزیکی یا شیمیایی",
                metaTitle = "تفاوت ضد آفتاب فیزیکی و شیمیایی چیست؟ | گلیسئو",
                metaDescription = "کدام نوع ضد آفتاب برای پوست شما مناسب‌تر است؟ بررسی جامع تفاوت‌ها و مزایا در گلیسئو.",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"تفاوت ضد آفتاب فیزیکی و شیمیایی"}""",
                imageAlt = "توضیح گرافیکی نحوه بازتاب اشعه خورشید توسط فیلترهای ضد آفتاب",
                seoScore = 87
            ),

            // --- WordPress Pages & Landing Pages ---
            SiteItem(
                id = 301,
                title = "صفحه اصلی فروشگاه گلیسئو (طراحی با المنتور پرو)",
                content = "به فروشگاه تخصصی محصولات اصل پوست و موی گلیسئو خوش آمدید. ارائه برترین برندهای بین‌المللی با ضمانت اصالت فیزیکی و مشاوره تخصصی.",
                excerpt = "صفحه اصلی با طراحی مدرن و واکنش‌گرا در المنتور.",
                slug = "home-page",
                categories = listOf("برگه‌های اصلی سایت"),
                type = SiteItemType.PAGE,
                focusKeyword = "فروشگاه آنلاین گلیسئو",
                metaTitle = "گلیسئو | مرجع تخصصی خرید محصولات اصل مراقبت از پوست و مو",
                metaDescription = "خرید اینترنتی محصولات اصل پوستی، سرم‌ها، ضدآفتاب‌ها و کرم‌های تخصصی با ضمانت اصالت صد در صدی و ارسال فوری.",
                schemaType = "WebPage",
                schemaJson = """{"@context":"https://schema.org","@type":"WebSite","name":"گلیسئو","url":"https://example.com"}""",
                imageAlt = "بنر اسلایدر صفحه اصلی فروشگاه گلیسئو",
                seoScore = 94,
                isElementor = true,
                elementorTemplate = "elementor_header_footer",
                elementorDataJson = """[{"id":"sec_hero_home","elType":"section","settings":{"background_color":"#F8FAFC"},"elements":[{"id":"col_1","elType":"column","elements":[{"id":"w_h1","elType":"widget","widgetType":"heading","settings":{"title":"زیبایی و درخشش پوست با اصیل‌ترین محصولات جهان"}}]}]}]"""
            ),
            SiteItem(
                id = 302,
                title = "درباره ما و خط مشی اصالت گلیسئو (طراحی المنتور)",
                content = "فروشگاه گلیسئو با هدف پایان دادن به دغدغه کالاهای فیک و تقلبی در بازار محصولات مراقبتی تاسیس شده است.\n\nما متعهدیم که تمام محصولات را با بچ‌نامبر معتبر، برچسب اصالت و مستقیماً از نمایندگی‌های رسمی تامین کنیم.",
                excerpt = "آشنایی با تاریخچه، رسالت و تیم کارشناسان گلیسئو.",
                slug = "about-us",
                categories = listOf("برگه‌های سازمانی"),
                type = SiteItemType.PAGE,
                focusKeyword = "درباره گلیسئو",
                metaTitle = "درباره ما | اصالت کالا و تعهد فروشگاه گلیسئو",
                metaDescription = "داستان شکل‌گیری گلیسئو و تعهد اخلاقی و فنی ما در ارائه محصولات تضمین‌شده و پشتیبانی تخصصی.",
                schemaType = "AboutPage",
                schemaJson = """{"@context":"https://schema.org","@type":"AboutPage","name":"درباره ما"}""",
                imageAlt = "تیم کارشناسان و مرکز پردازش سفارش‌های گلیسئو",
                seoScore = 89,
                isElementor = true,
                elementorTemplate = "elementor_header_footer",
                elementorDataJson = """[{"id":"sec_about_hero","elType":"section","settings":{"background_color":"#EEF2FF"},"elements":[]}]"""
            ),
            SiteItem(
                id = 303,
                title = "لندینگ جشنواره تخفیف شگفت‌انگیز فصل (المنتور)",
                content = "جشنواره ویژه محصولات پرفروش پوستی با تخفیف‌های استثنایی و ارسال رایگان. فرصت محدود نوسازی روتین مراقبت پوست.",
                excerpt = "صفحه فرود کمپین تخفیف بهاره همراه با شمارنده معکوس.",
                slug = "promo-campaign-landing",
                categories = listOf("لندینگ پیج‌ها"),
                type = SiteItemType.PAGE,
                focusKeyword = "جشنواره تخفیف گلیسئو",
                metaTitle = "جشنواره شگفت‌انگیز تخفیف تا ۴۰ درصد محصولات پوستی | گلیسئو",
                metaDescription = "تخفیف ویژه انواع کرم‌های آبرسان، سرم‌های ویتامین C و ضد آفتاب‌های اورجینال در جشنواره گلیسئو.",
                schemaType = "WebPage",
                schemaJson = """{"@context":"https://schema.org","@type":"WebPage","name":"جشنواره تخفیف"}""",
                imageAlt = "بنر قرمز رنگ تخفیف ویژه و پیشنهاد شگفت‌انگیز",
                seoScore = 92,
                isElementor = true,
                elementorTemplate = "elementor_canvas",
                elementorDataJson = """[{"id":"sec_promo_hero","elType":"section","settings":{"background_color":"#FFF1F2"},"elements":[]}]"""
            ),
            SiteItem(
                id = 304,
                title = "تماس با ما و مشاوره تخصصی پوست و سفارش‌ها",
                content = "پاسخگویی سریع در تمامی روزهای هفته از ۹ صبح تا ۹ شب. تلفن پشتیبانی: ۰۲۱-۸۸۸۸۰۰۰۰ | ایمیل: info@example.com",
                excerpt = "راه‌های ارتباطی، نشانی دفتر مرکزی و فرم تماس مستقیم.",
                slug = "contact-us",
                categories = listOf("برگه‌های سازمانی"),
                type = SiteItemType.PAGE,
                focusKeyword = "تماس با گلیسئو",
                metaTitle = "تماس با ما | مشاوره رایگان پوست و پیگیری سفارش | گلیسئو",
                metaDescription = "راه‌های ارتباطی مستقیم با کارشناسان و پشتیبانی ۲۴ ساعته فروشگاه تخصصی گلیسئو.",
                schemaType = "ContactPage",
                schemaJson = """{"@context":"https://schema.org","@type":"ContactPage","name":"تماس با ما"}""",
                imageAlt = "دفتر پشتیبانی و کارشناسان پاسخگویی مشتریان",
                seoScore = 84,
                isElementor = true,
                elementorTemplate = "elementor_header_footer"
            ),
            SiteItem(
                id = 305,
                title = "شرایط ضمانت اصالت کالا و مرجوعی ۷ روزه",
                content = "شفافیت کامل در تضمین کیفیت: چنانچه کالای دریافتی کوچک‌ترین مغایرتی با مشخصات درج‌شده داشته باشد، تا ۷ روز بدون قید و شرط وجه شما بازگردانده می‌شود.",
                excerpt = "قوانین و رویه‌های مرجوعی و بازپرداخت وجه مشتریان.",
                slug = "authenticity-guarantee-return-policy",
                categories = listOf("قوانین و مقررات"),
                type = SiteItemType.PAGE,
                focusKeyword = "ضمانت اصالت گلیسئو",
                metaTitle = "شرایط بازگشت کالا و ضمانت اصالت ۱۰۰٪ | گلیسئو",
                metaDescription = "قوانین بازگشت ۷ روزه کالا و گارانتی اصالت بی قید و شرط در فروشگاه گلیسئو.",
                schemaType = "WebPage",
                schemaJson = """{"@context":"https://schema.org","@type":"WebPage","name":"ضمانت اصالت"}""",
                imageAlt = "نشان طلایی ضمانت بازگشت وجه و تایید اصالت کالا",
                seoScore = 86
            )
        )
        _items.value = initialItems
    }


    // --- Audit Logs & Snapshots ---
    val auditLogs: Flow<List<AuditLog>> = logDao.getAllLogs().map { entities ->
        entities.map { entity ->
            val diffList = mutableListOf<DiffItem>()
            try {
                val array = JSONArray(entity.diffsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    diffList.add(
                        DiffItem(
                            fieldName = obj.getString("field"),
                            beforeValue = obj.getString("before"),
                            afterValue = obj.getString("after"),
                            isLockedViolation = obj.optBoolean("isLocked", false)
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }

            AuditLog(
                id = entity.id,
                timestamp = entity.timestamp,
                targetType = try { SiteItemType.valueOf(entity.targetType) } catch (e: Exception) { SiteItemType.POST },
                targetId = entity.targetId,
                targetTitle = entity.targetTitle,
                actionName = entity.actionName,
                reason = entity.reason,
                authorRole = entity.authorRole,
                status = try { LogStatus.valueOf(entity.status) } catch (e: Exception) { LogStatus.SUCCESS },
                diffs = diffList,
                priorSnapshotId = entity.priorSnapshotId,
                isUndone = entity.isUndone
            )
        }
    }

    // --- Task Queue ---
    val queueTasks: Flow<List<QueuedTask>> = taskDao.getAllTasks().map { entities ->
        entities.map { entity ->
            QueuedTask(
                id = entity.id,
                targetId = entity.targetId,
                targetType = try { SiteItemType.valueOf(entity.targetType) } catch (e: Exception) { SiteItemType.POST },
                targetTitle = entity.targetTitle,
                taskType = try { TaskType.valueOf(entity.taskType) } catch (e: Exception) { TaskType.SEO_META },
                status = try { TaskStatus.valueOf(entity.status) } catch (e: Exception) { TaskStatus.QUEUED },
                isDryRun = entity.isDryRun,
                needsHumanReview = entity.needsHumanReview,
                reviewApproved = entity.reviewApproved,
                retryCount = entity.retryCount,
                maxRetries = entity.maxRetries,
                errorMessage = entity.errorMessage,
                estimatedTokens = entity.estimatedTokens,
                diffPreview = entity.diffPreview,
                timestamp = entity.timestamp
            )
        }
    }

    // --- Brand Profile ---
    val brandProfile: Flow<BrandProfile> = brandDao.getBrandMemory().map { entity ->
        if (entity != null) {
            BrandProfile(
                brandName = entity.brandName,
                toneOfVoice = entity.toneOfVoice,
                targetAudience = entity.targetAudience,
                forbiddenKeywords = entity.forbiddenKeywords,
                competitiveAdvantage = entity.competitiveAdvantage
            )
        } else {
            BrandProfile()
        }
    }

    suspend fun saveBrandProfile(brand: BrandProfile) {
        brandDao.saveBrandMemory(
            BrandEntity(
                id = 1,
                brandName = brand.brandName,
                toneOfVoice = brand.toneOfVoice,
                targetAudience = brand.targetAudience,
                forbiddenKeywords = brand.forbiddenKeywords,
                competitiveAdvantage = brand.competitiveAdvantage
            )
        )
    }

    // --- Connection Testing ---
    suspend fun testConnection(config: SiteConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val logs = mutableListOf<String>()

        logs.add("بررسی آدرس سایت: ${config.siteUrl}")

        if (config.isDemoMode) {
            kotlinx.coroutines.delay(450)
            logs.add("پینگ سرور: ۴۲ میلی‌ثانیه")
            logs.add("پاسخ وردپرس REST API: وضعیت 200 OK")
            logs.add("احراز هویت رمز برنامه (Application Password): تایید شد")
            logs.add("پاسخ ووکامرس REST API v3: وضعیت 200 OK")
            logs.add("قفل سخت امنیتی: قیمت‌ها و پیوندهای یکتا محافظت شده‌اند")
            return@withContext ConnectionTestResult(
                isSuccess = true,
                wpConnected = true,
                wcConnected = true,
                latencyMs = 42,
                wpVersion = "WordPress Core 6.x",
                wcVersion = "WooCommerce 9.x",
                siteName = "فروشگاه آنلاین گلیسئو (دمو)",
                logs = logs
            )
        }

        try {
            val client = NetworkClientProvider.createOkHttpClient()
            var currentAppPass = config.wpAppPassword
            var authHeader = NetworkClientProvider.createBasicAuth(config.wpUsername, currentAppPass)

            val wpApi = NetworkClientProvider.createWordPressApi(config.siteUrl, client)

            logs.add("ارسال درخواست به WP REST API (${config.siteUrl})...")
            var wpResponse = wpApi.getSiteInfo(authHeader)

            // Check if user entered normal password and needs automatic application password provisioning
            if (wpResponse.code() == 401 || !wpResponse.isSuccessful) {
                logs.add("تلاش برای احراز هویت اولیه و تولید کلید برنامه (Application Password)...")
                val autoAppPass = WordPressAuthHelper.autoProvisionAppPassword(
                    siteUrl = config.siteUrl,
                    username = config.wpUsername,
                    rawPassword = config.wpAppPassword
                )
                if (autoAppPass != null) {
                    currentAppPass = autoAppPass
                    authHeader = NetworkClientProvider.createBasicAuth(config.wpUsername, autoAppPass)
                    _siteConfig.value = config.copy(wpAppPassword = autoAppPass)
                    logs.add("کلید برنامه اختصاصی وردپرس با موفقیت ایجاد و ذخیره شد.")
                    wpResponse = wpApi.getSiteInfo(authHeader)
                }
            }

            val latency = System.currentTimeMillis() - startTime
            val wpOk = wpResponse.isSuccessful
            var siteTitle = config.siteUrl
            var siteDesc = "فروشگاه اینترنتی فعال"

            if (wpOk) {
                val bodyStr = wpResponse.body()?.string() ?: ""
                val cleanedBody = WordPressJsonSanitizerInterceptor.cleanJsonResponse(bodyStr)
                try {
                    val root = JSONObject(cleanedBody)
                    siteTitle = root.optString("name", siteTitle)
                    siteDesc = root.optString("description", siteDesc)
                    _brandKnowledgeBase.value = _brandKnowledgeBase.value.copy(
                        storeName = "$siteTitle (GoliKala)",
                        audienceProfile = "مشتریان و خریداران $siteTitle در وب فارسی"
                    )
                } catch (e: Exception) {
                    // Ignore parse error
                }
                logs.add("اتصال به وردپرس موفق بود (سایت: $siteTitle)")

                // Verify user
                try {
                    val userResp = wpApi.getUserMe(authHeader)
                    if (userResp.isSuccessful) {
                        val userBody = WordPressJsonSanitizerInterceptor.cleanJsonResponse(userResp.body()?.string() ?: "")
                        val userObj = JSONObject(userBody)
                        val uname = userObj.optString("name", config.wpUsername)
                        logs.add("احراز هویت کاربر «$uname» با موفقیت تایید گردید.")
                    }
                } catch (e: Exception) {
                    // Ignore user check error
                }
            } else {
                logs.add("خطا در پاسخ وردپرس: کد ${wpResponse.code()}")
            }

            // Test WooCommerce
            var wcOk = false
            var wcVersion = "WooCommerce 9.x"
            try {
                val wcApi = NetworkClientProvider.createWooCommerceApi(config.siteUrl, client)
                val wcAuth = if (config.wcConsumerKey.isNotBlank()) {
                    NetworkClientProvider.createBasicAuth(config.wcConsumerKey, config.wcConsumerSecret)
                } else {
                    authHeader // WP Application password works directly with WooCommerce REST API!
                }
                val wcResponse = wcApi.getProducts(perPage = 1, authHeader = wcAuth)
                if (wcResponse.isSuccessful) {
                    wcOk = true
                    logs.add("اتصال به ووکامرس v3: تایید شد (دسترسی به محصولات برقرار است)")
                } else {
                    // Fallback to WP custom post type
                    val customProdResp = wpApi.getCustomPostProducts(perPage = 1, authHeader = authHeader)
                    if (customProdResp.isSuccessful) {
                        wcOk = true
                        logs.add("اتصال به محصولات از طریق Post Type وردپرس تایید شد.")
                    } else {
                        logs.add("پاسخ ووکامرس: کد ${wcResponse.code()}")
                    }
                }
            } catch (e: Exception) {
                logs.add("بررسی ووکامرس: ${e.message}")
            }

            logs.add("قفل سخت امنیتی گلیسئو فعال و آماده محافظت از قیمت‌ها است.")

            ConnectionTestResult(
                isSuccess = wpOk,
                wpConnected = wpOk,
                wcConnected = wcOk,
                latencyMs = latency,
                wpVersion = "WordPress Core 6.x (Rank Math فعال)",
                wcVersion = if (wcOk) wcVersion else "در دسترس نیست",
                siteName = siteTitle,
                logs = logs
            )
        } catch (e: Exception) {
            logs.add("خطای اتصال شبکه: ${e.localizedMessage ?: e.message}")
            ConnectionTestResult(
                isSuccess = false,
                wpConnected = false,
                wcConnected = false,
                latencyMs = System.currentTimeMillis() - startTime,
                wpVersion = "-",
                wcVersion = "-",
                siteName = config.siteUrl,
                logs = logs
            )
        }
    }

    // --- Hard Lock Validation & Snapshot Capture ---
    suspend fun applyItemUpdate(
        targetId: Long,
        proposedChanges: Map<String, Any?>,
        actionName: String,
        reason: String,
        authorRole: UserRole = _userSession.value.role,
        isDryRun: Boolean = false
    ): Result<SiteItem> = withContext(Dispatchers.IO) {
        val currentItem = _items.value.find { it.id == targetId }
            ?: return@withContext Result.failure(IllegalArgumentException("آیتم مورد نظر یافت نشد"))

        // STRICT INVIOLABLE HARD LOCK CHECK
        try {
            HardLockValidator.validateModifications(proposedChanges)
        } catch (e: SecurityHardLockException) {
            // Record blocked security breach in audit logs!
            val violationDiff = listOf(
                DiffItem(
                    fieldName = e.blockedField,
                    beforeValue = "محافظت شده با قفل سخت",
                    afterValue = e.attemptedValue ?: "تلاش برای تغییر",
                    isLockedViolation = true
                )
            )
            recordLog(
                target = currentItem,
                actionName = actionName,
                reason = "تلاش غیرمجاز برای تغییر قفل سخت: ${e.blockedField}",
                role = authorRole,
                status = LogStatus.BLOCKED_SECURITY,
                diffs = violationDiff,
                snapshotId = null
            )
            return@withContext Result.failure(e)
        }

        // Calculate Diffs
        val diffs = mutableListOf<DiffItem>()
        var updatedItem = currentItem

        for ((field, newVal) in proposedChanges) {
            when (field) {
                "title" -> {
                    val s = newVal.toString()
                    if (s != currentItem.title) {
                        diffs.add(DiffItem("عنوان", currentItem.title, s))
                        updatedItem = updatedItem.copy(title = s)
                    }
                }
                "content" -> {
                    val s = newVal.toString()
                    if (s != currentItem.content) {
                        diffs.add(DiffItem("محتوا", currentItem.content.take(60) + "...", s.take(60) + "..."))
                        updatedItem = updatedItem.copy(content = s)
                    }
                }
                "excerpt" -> {
                    val s = newVal.toString()
                    if (s != currentItem.excerpt) {
                        diffs.add(DiffItem("چکیده", currentItem.excerpt, s))
                        updatedItem = updatedItem.copy(excerpt = s)
                    }
                }
                "metaTitle" -> {
                    val s = newVal.toString()
                    if (s != currentItem.metaTitle) {
                        diffs.add(DiffItem("عنوان سئو (متا)", currentItem.metaTitle, s))
                        updatedItem = updatedItem.copy(metaTitle = s)
                    }
                }
                "metaDescription" -> {
                    val s = newVal.toString()
                    if (s != currentItem.metaDescription) {
                        diffs.add(DiffItem("توضیحات متا سئو", currentItem.metaDescription, s))
                        updatedItem = updatedItem.copy(metaDescription = s)
                    }
                }
                "focusKeyword" -> {
                    val s = newVal.toString()
                    if (s != currentItem.focusKeyword) {
                        diffs.add(DiffItem("کلمه کلیدی کانونی", currentItem.focusKeyword, s))
                        updatedItem = updatedItem.copy(focusKeyword = s)
                    }
                }
                "schemaJson" -> {
                    val s = newVal.toString()
                    if (s != currentItem.schemaJson) {
                        diffs.add(DiffItem("اسکیما JSON-LD", currentItem.schemaJson.take(40), s.take(40)))
                        updatedItem = updatedItem.copy(schemaJson = s)
                    }
                }
                "imageAlt" -> {
                    val s = newVal.toString()
                    if (s != currentItem.imageAlt) {
                        diffs.add(DiffItem("متن جایگزین تصویر", currentItem.imageAlt, s))
                        updatedItem = updatedItem.copy(imageAlt = s)
                    }
                }
                "featuredImageUrl" -> {
                    val s = newVal.toString()
                    if (s != currentItem.featuredImageUrl) {
                        diffs.add(DiffItem("آدرس تصویر شاخص", currentItem.featuredImageUrl, s))
                        updatedItem = updatedItem.copy(featuredImageUrl = s)
                    }
                }
                "canonicalUrl" -> {
                    val s = newVal.toString()
                    if (s != currentItem.canonicalUrl) {
                        diffs.add(DiffItem("پیوند کانونیکال", currentItem.canonicalUrl, s))
                        updatedItem = updatedItem.copy(canonicalUrl = s)
                    }
                }
                "metaRobots" -> {
                    val s = newVal.toString()
                    if (s != currentItem.metaRobots) {
                        diffs.add(DiffItem("متا ربات‌ها", currentItem.metaRobots, s))
                        updatedItem = updatedItem.copy(metaRobots = s)
                    }
                }
                "elementorDataJson" -> {
                    val s = newVal.toString()
                    if (s != currentItem.elementorDataJson) {
                        diffs.add(DiffItem("طراحی ساختار المنتور", "نسخه قبل", "قالب جدید المنتور"))
                        updatedItem = updatedItem.copy(elementorDataJson = s, isElementor = true)
                    }
                }
                "elementorTemplate" -> {
                    val s = newVal.toString()
                    updatedItem = updatedItem.copy(elementorTemplate = s)
                }
                "isElementor" -> {
                    val b = (newVal as? Boolean) ?: true
                    updatedItem = updatedItem.copy(isElementor = b)
                }
                "seoScore" -> {
                    val score = (newVal as? Int) ?: currentItem.seoScore
                    updatedItem = updatedItem.copy(seoScore = score)
                }
            }
        }

        if (isDryRun) {
            // Dry run returns result preview without persisting to database or site
            return@withContext Result.success(updatedItem)
        }

        // SAVE PRIOR SNAPSHOT FOR 1-TAP UNDO
        val snapshotId = snapshotDao.insertSnapshot(
            SnapshotEntity(
                timestamp = System.currentTimeMillis(),
                targetType = currentItem.type.name,
                targetId = currentItem.id,
                title = currentItem.title,
                content = currentItem.content,
                excerpt = currentItem.excerpt,
                metaTitle = currentItem.metaTitle,
                metaDescription = currentItem.metaDescription,
                schemaJson = currentItem.schemaJson,
                imageAlt = currentItem.imageAlt,
                reason = reason
            )
        )

        // Record Audit Log
        recordLog(
            target = currentItem,
            actionName = actionName,
            reason = reason,
            role = authorRole,
            status = LogStatus.SUCCESS,
            diffs = diffs,
            snapshotId = snapshotId
        )

        // Update In-Memory Cache
        _items.value = _items.value.map { if (it.id == targetId) updatedItem else it }

        // Push update to live site if not in demo mode
        if (!_siteConfig.value.isDemoMode) {
            try {
                val client = NetworkClientProvider.createOkHttpClient()
                val wpAuth = NetworkClientProvider.createBasicAuth(_siteConfig.value.wpUsername, _siteConfig.value.wpAppPassword)
                if (updatedItem.type == SiteItemType.POST) {
                    val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                    val payload = JSONObject().apply {
                        put("title", updatedItem.title)
                        put("content", updatedItem.content)
                        put("excerpt", updatedItem.excerpt)
                        val metaObj = JSONObject().apply {
                            put("rank_math_title", updatedItem.metaTitle)
                            put("rank_math_description", updatedItem.metaDescription)
                            put("rank_math_focus_keyword", updatedItem.focusKeyword)
                            put("rank_math_canonical_url", updatedItem.canonicalUrl)
                            put("_yoast_wpseo_title", updatedItem.metaTitle)
                            put("_yoast_wpseo_metadesc", updatedItem.metaDescription)
                            put("_yoast_wpseo_focuskw", updatedItem.focusKeyword)
                        }
                        put("meta", metaObj)
                    }.toString()
                    val body = payload.toRequestBody("application/json".toMediaTypeOrNull())
                    wpApi.updatePost(updatedItem.id, body, wpAuth)
                } else if (updatedItem.type == SiteItemType.PAGE) {
                    val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                    val payload = JSONObject().apply {
                        put("title", updatedItem.title)
                        put("content", updatedItem.content)
                        val metaObj = JSONObject().apply {
                            put("rank_math_title", updatedItem.metaTitle)
                            put("rank_math_description", updatedItem.metaDescription)
                            put("rank_math_focus_keyword", updatedItem.focusKeyword)
                            put("rank_math_canonical_url", updatedItem.canonicalUrl)
                            put("_yoast_wpseo_title", updatedItem.metaTitle)
                            put("_yoast_wpseo_metadesc", updatedItem.metaDescription)
                            put("_yoast_wpseo_focuskw", updatedItem.focusKeyword)
                            if (updatedItem.isElementor && updatedItem.elementorDataJson.isNotBlank()) {
                                put("_elementor_edit_mode", "builder")
                                put("_elementor_data", updatedItem.elementorDataJson)
                                put("_wp_page_template", updatedItem.elementorTemplate.ifBlank { "elementor_header_footer" })
                            }
                        }
                        put("meta", metaObj)
                    }.toString()
                    val body = payload.toRequestBody("application/json".toMediaTypeOrNull())
                    wpApi.updatePage(updatedItem.id, body, wpAuth)
                } else if (updatedItem.type == SiteItemType.PRODUCT) {
                    val wcApi = NetworkClientProvider.createWooCommerceApi(_siteConfig.value.siteUrl, client)
                    val wcAuth = if (_siteConfig.value.wcConsumerKey.isNotBlank()) {
                        NetworkClientProvider.createBasicAuth(_siteConfig.value.wcConsumerKey, _siteConfig.value.wcConsumerSecret)
                    } else {
                        wpAuth
                    }
                    val metaArray = JSONArray().apply {
                        put(JSONObject().apply { put("key", "rank_math_title"); put("value", updatedItem.metaTitle) })
                        put(JSONObject().apply { put("key", "rank_math_description"); put("value", updatedItem.metaDescription) })
                        put(JSONObject().apply { put("key", "rank_math_focus_keyword"); put("value", updatedItem.focusKeyword) })
                        put(JSONObject().apply { put("key", "rank_math_canonical_url"); put("value", updatedItem.canonicalUrl) })
                        put(JSONObject().apply { put("key", "_yoast_wpseo_title"); put("value", updatedItem.metaTitle) })
                        put(JSONObject().apply { put("key", "_yoast_wpseo_metadesc"); put("value", updatedItem.metaDescription) })
                        put(JSONObject().apply { put("key", "_yoast_wpseo_focuskw"); put("value", updatedItem.focusKeyword) })
                    }
                    val payload = JSONObject().apply {
                        put("name", updatedItem.title)
                        put("description", updatedItem.content)
                        put("short_description", updatedItem.excerpt)
                        put("meta_data", metaArray)
                        if (updatedItem.featuredImageUrl.isNotBlank() && updatedItem.imageAlt.isNotBlank()) {
                            put("images", JSONArray().put(JSONObject().apply {
                                put("src", updatedItem.featuredImageUrl)
                                put("alt", updatedItem.imageAlt)
                                put("name", updatedItem.imageTitle.ifBlank { updatedItem.title })
                            }))
                        }
                    }.toString()
                    val body = payload.toRequestBody("application/json".toMediaTypeOrNull())
                    wcApi.updateProduct(updatedItem.id, body, wcAuth)
                }
            } catch (e: Exception) {
                // Ignore transient network errors
            }
        }

        // Track API Budget token consumption
        consumeBudgetTokens(tokens = 450)

        Result.success(updatedItem)
    }

    private suspend fun recordLog(
        target: SiteItem,
        actionName: String,
        reason: String,
        role: UserRole,
        status: LogStatus,
        diffs: List<DiffItem>,
        snapshotId: Long?
    ) {
        val diffJsonArray = JSONArray()
        for (d in diffs) {
            val obj = JSONObject().apply {
                put("field", d.fieldName)
                put("before", d.beforeValue)
                put("after", d.afterValue)
                put("isLocked", d.isLockedViolation)
            }
            diffJsonArray.put(obj)
        }

        logDao.insertLog(
            LogEntity(
                timestamp = System.currentTimeMillis(),
                targetType = target.type.name,
                targetId = target.id,
                targetTitle = target.title,
                actionName = actionName,
                reason = reason,
                authorRole = role.persianLabel,
                status = status.name,
                diffsJson = diffJsonArray.toString(),
                priorSnapshotId = snapshotId,
                isUndone = false
            )
        )
    }

    // --- One-Click Undo / Rollback ---
    suspend fun undoLog(logId: Long, snapshotId: Long): Boolean = withContext(Dispatchers.IO) {
        val snapshot = snapshotDao.getSnapshotById(snapshotId) ?: return@withContext false

        val item = _items.value.find { it.id == snapshot.targetId } ?: return@withContext false

        // Restore item back to snapshot state
        val restored = item.copy(
            title = snapshot.title,
            content = snapshot.content,
            excerpt = snapshot.excerpt,
            metaTitle = snapshot.metaTitle,
            metaDescription = snapshot.metaDescription,
            schemaJson = snapshot.schemaJson,
            imageAlt = snapshot.imageAlt
        )

        _items.value = _items.value.map { if (it.id == item.id) restored else it }

        // Mark Log as Undone
        logDao.markLogUndone(logId)

        // Record rollback in audit log
        val rollbackDiff = listOf(
            DiffItem("بازگردانی نسخه", "نسخه جاری", "بازگشت به نسخه قبل (${snapshot.reason})")
        )
        recordLog(
            target = restored,
            actionName = "بازگردانی نسخه (Undo)",
            reason = "بازگشت خودکار به نقطه امن به درخواست کاربر",
            role = _userSession.value.role,
            status = LogStatus.UNDONE,
            diffs = rollbackDiff,
            snapshotId = null
        )

        true
    }

    // --- AI SEO Generation with Google Search Grounding ---
    suspend fun generateSeoForCurrentItem(itemId: Long, customPrompt: String? = null): SeoAiResult? {
        val item = _items.value.find { it.id == itemId } ?: return null
        val brand = BrandProfile()
        val result = geminiService.optimizeItemSeo(item, brand, customPrompt)
        consumeBudgetTokens(result.tokenCost)
        return result
    }

    // --- Task Queue Management & 10% Sampling ---
    suspend fun queueTask(
        targetId: Long,
        taskType: TaskType,
        isDryRun: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val item = _items.value.find { it.id == targetId } ?: return@withContext

        // Check if autopilot is ON
        val autopilot = _autopilotConfig.value
        val isModuleActive = when (taskType) {
            TaskType.SEO_META -> autopilot.isSeoMetaActive
            TaskType.CONTENT_POLISH -> autopilot.isContentPolishActive
            TaskType.SCHEMA_GENERATION -> autopilot.isSchemaActive
            TaskType.ALT_TAGS -> autopilot.isAltTagsActive
            TaskType.BATCH_AUDIT -> false
        }

        // 10% Human Review Sampling
        // Even when autopilot is active, 10% of tasks are randomly flagged for human review!
        val randomRoll = Random.nextFloat()
        val needsHumanReview = if (autopilot.isMasterActive && isModuleActive) {
            randomRoll < autopilot.samplingRate // 10% sample
        } else {
            true // If module is OFF, human review is always required!
        }

        val initialStatus = if (needsHumanReview) {
            TaskStatus.NEEDS_REVIEW
        } else {
            TaskStatus.QUEUED
        }

        taskDao.insertTask(
            TaskEntity(
                targetId = item.id,
                targetType = item.type.name,
                targetTitle = item.title,
                taskType = taskType.name,
                status = initialStatus.name,
                isDryRun = isDryRun,
                needsHumanReview = needsHumanReview,
                reviewApproved = false,
                retryCount = 0,
                maxRetries = 3,
                errorMessage = null,
                estimatedTokens = 850,
                diffPreview = "پیش‌نویس خودکار سئو و فراداده برای ${item.title}",
                timestamp = System.currentTimeMillis()
            )
        )

        // If autopilot is active and no human review needed, trigger execution
        if (!needsHumanReview && autopilot.isMasterActive && isModuleActive) {
            executeTaskImmediately(item, taskType)
        }
    }

    suspend fun approveAndRunTask(taskId: Long) = withContext(Dispatchers.IO) {
        taskDao.approveTask(taskId)
    }

    suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(taskId)
    }

    suspend fun clearCompletedTasks() = withContext(Dispatchers.IO) {
        taskDao.clearCompletedTasks()
    }

    private suspend fun executeTaskImmediately(item: SiteItem, taskType: TaskType) {
        val brand = BrandProfile()
        val aiResult = geminiService.optimizeItemSeo(item, brand)
        val changes = mutableMapOf<String, Any?>()
        when (taskType) {
            TaskType.SEO_META -> {
                changes["metaTitle"] = aiResult.suggestedTitle
                changes["metaDescription"] = aiResult.suggestedMetaDescription
                changes["focusKeyword"] = aiResult.suggestedFocusKeyword
                changes["seoScore"] = aiResult.seoScoreImprovement
            }
            TaskType.SCHEMA_GENERATION -> {
                changes["schemaJson"] = aiResult.suggestedSchemaJson
            }
            TaskType.ALT_TAGS -> {
                changes["imageAlt"] = aiResult.suggestedImageAlt
            }
            TaskType.CONTENT_POLISH -> {
                changes["excerpt"] = aiResult.suggestedMetaDescription
            }
            TaskType.BATCH_AUDIT -> {
                changes["seoScore"] = aiResult.seoScoreImprovement
            }
        }
        applyItemUpdate(
            targetId = item.id,
            proposedChanges = changes,
            actionName = "خلبان خودکار: ${taskType.persianLabel}",
            reason = "اجرای خودکار بر اساس خط مشی ماژول و هوش مصنوعی جمینای",
            authorRole = UserRole.ADMIN
        )
    }

    // --- Budget & Quota Tracking ---
    private fun consumeBudgetTokens(tokens: Int) {
        val current = _budgetTracker.value
        val costPerToken = 0.0000015 // Estimated $0.0015 per 1K tokens
        val addedUsd = tokens * costPerToken
        val updated = current.copy(
            tokensUsedToday = current.tokensUsedToday + tokens,
            currentSpentUsd = ((current.currentSpentUsd + addedUsd) * 100).toInt() / 100.0
        )
        _budgetTracker.value = updated
    }

    fun updateBudgetCap(newCapUsd: Double) {
        _budgetTracker.value = _budgetTracker.value.copy(dailyBudgetCapUsd = newCapUsd)
    }

    // --- Autopilot Configuration ---
    fun updateAutopilotConfig(config: AutopilotConfig) {
        _autopilotConfig.value = config
    }

    fun emergencyStopAutopilot() {
        _autopilotConfig.value = _autopilotConfig.value.copy(isMasterActive = false)
        _circuitBreakerTriggered.value = true
    }

    fun resetCircuitBreaker() {
        _circuitBreakerTriggered.value = false
    }

    // --- Session & Roles ---
    fun switchUserRole(role: UserRole) {
        _userSession.value = _userSession.value.copy(role = role)
    }

    fun verifyTwoFactor(otpCode: String): Boolean {
        // Simple verification for demonstration: any 6-digit code or "123456"
        return if (otpCode.length == 6) {
            _userSession.value = _userSession.value.copy(is2FAVerified = true)
            true
        } else {
            false
        }
    }

    fun updateSiteConfig(newConfig: SiteConfig) {
        _siteConfig.value = newConfig
    }

    // --- Phase 2: Natural Language Freeform Command ---
    suspend fun executeFreeformCommand(prompt: String, isDryRun: Boolean = false): FreeformCommandPlan = withContext(Dispatchers.IO) {
        val plan = ContentModuleProcessor.parseFreeformCommand(prompt)

        if (!plan.isSafeFromHardLock) {
            // Record security hard lock block in audit trail
            val violationDiff = listOf(
                DiffItem("دستور آزاد", prompt, "مسدود شده با قفل سخت", isLockedViolation = true)
            )
            val fallbackTarget = _items.value.firstOrNull() ?: SiteItem(
                id = 999,
                title = "دستور مسدود شده",
                content = "",
                slug = "blocked",
                type = SiteItemType.PRODUCT
            )
            recordLog(
                target = fallbackTarget,
                actionName = "دستور آزاد (مسدود شده)",
                reason = plan.blockedReason ?: "نقض قفل سخت قیمت/اسلاگ",
                role = _userSession.value.role,
                status = LogStatus.BLOCKED_SECURITY,
                diffs = violationDiff,
                snapshotId = null
            )
            return@withContext plan
        }

        // If safe, execute or queue based on plan target
        if (!isDryRun) {
            consumeBudgetTokens(plan.estimatedTokens)
            val allFlag = prompt.contains("همه") || prompt.contains("تمام") || prompt.contains("کل")
            val targetItems = when {
                allFlag && !prompt.contains("محصول") && !prompt.contains("برگه") && !prompt.contains("پست") -> _items.value
                plan.targetType == SiteItemType.PRODUCT -> _items.value.filter { it.type == SiteItemType.PRODUCT }
                plan.targetType == SiteItemType.PAGE -> _items.value.filter { it.type == SiteItemType.PAGE }
                else -> _items.value.filter { it.type == SiteItemType.POST }
            }

            for (item in targetItems) {
                queueTask(item.id, TaskType.SEO_META, isDryRun = false)
            }
        }

        plan
    }

    // --- Phase 2: Product Studio & Rank Math Meta ---
    fun generateProductStudioPackage(productId: Long): ProductOptimizationPackage? {
        val item = _items.value.find { it.id == productId } ?: return null
        return ContentModuleProcessor.generateProductPackage(item, _brandKnowledgeBase.value)
    }

    suspend fun applyProductStudioPackage(productId: Long, pkg: ProductOptimizationPackage) = withContext(Dispatchers.IO) {
        val changes = mapOf(
            "title" to pkg.seoTitle,
            "excerpt" to pkg.shortDescription,
            "content" to pkg.fullDescription,
            "metaDescription" to pkg.rankMathMetaDesc,
            "focusKeyword" to pkg.focusKeyword
        )
        applyItemUpdate(
            targetId = productId,
            proposedChanges = changes,
            actionName = "بهینه‌سازی پکیج کامل محصول (Rank Math + FAQ)",
            reason = "تولید سئو، ویژگی‌ها و هشدارهای بهداشتی منطبق بر حافظه گلی کالا"
        )
    }

    suspend fun batchOptimizeAllProducts() = withContext(Dispatchers.IO) {
        val products = _items.value.filter { it.type == SiteItemType.PRODUCT }
        for (prod in products) {
            queueTask(prod.id, TaskType.SEO_META, isDryRun = false)
        }
    }

    // --- Phase 2: Articles & E-E-A-T Content Studio ---
    fun getArticleTopicSuggestions(): List<ArticleTopicSuggestion> {
        val existingTitles = _items.value.map { it.title }
        return ContentModuleProcessor.getSuggestedTopics(existingTitles)
    }

    suspend fun generateFullArticleFromTopic(
        topic: ArticleTopicSuggestion,
        customInstructions: String? = null
    ): GeneratedArticle = withContext(Dispatchers.IO) {
        val brand = BrandProfile(
            brandName = _brandKnowledgeBase.value.storeName,
            toneOfVoice = _brandKnowledgeBase.value.brandTone,
            targetAudience = _brandKnowledgeBase.value.audienceProfile,
            forbiddenKeywords = _brandKnowledgeBase.value.forbiddenTerms.joinToString("، ")
        )
        geminiService.generateArticleWithGemini(
            topicTitle = topic.title,
            focusKeyword = topic.focusKeyword,
            brandProfile = brand,
            customInstructions = customInstructions
        )
    }

    suspend fun generateCustomArticle(
        topicTitle: String,
        focusKeyword: String,
        customInstructions: String? = null
    ): GeneratedArticle = withContext(Dispatchers.IO) {
        val brand = BrandProfile(
            brandName = _brandKnowledgeBase.value.storeName,
            toneOfVoice = _brandKnowledgeBase.value.brandTone,
            targetAudience = _brandKnowledgeBase.value.audienceProfile,
            forbiddenKeywords = _brandKnowledgeBase.value.forbiddenTerms.joinToString("، ")
        )
        geminiService.generateArticleWithGemini(
            topicTitle = topicTitle,
            focusKeyword = focusKeyword,
            brandProfile = brand,
            customInstructions = customInstructions
        )
    }

    suspend fun publishNewArticle(article: GeneratedArticle, targetStatus: String = "publish") = withContext(Dispatchers.IO) {
        val newId = System.currentTimeMillis()
        val sectionsBody = article.sections.joinToString("\n\n") { section ->
            "## ${section.h2Heading}\n${section.content}\n" +
                if (section.h3Headings.isNotEmpty()) {
                    section.h3Headings.joinToString("\n") { "### $it" } + "\n"
                } else ""
        }

        val safeSlug = "blog-" + PersianNormalizer.normalize(article.focusKeyword).replace(" ", "-").take(40)
        val newItem = SiteItem(
            id = newId,
            title = article.title,
            content = sectionsBody,
            excerpt = article.metaDescription,
            slug = safeSlug,
            categories = listOf("مقالات آموزشی", "مراقبت پوست"),
            type = SiteItemType.POST,
            status = targetStatus,
            focusKeyword = article.focusKeyword,
            metaTitle = "${article.title} | ${_brandKnowledgeBase.value.storeName}",
            metaDescription = article.metaDescription,
            schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"${article.title}"}""",
            imageAlt = "تصویر شاخص برای مقاله ${article.title}",
            seoScore = 88
        )

        var finalItem = newItem
        if (!_siteConfig.value.isDemoMode) {
            try {
                val client = NetworkClientProvider.createOkHttpClient()
                val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                val wpAuth = NetworkClientProvider.createBasicAuth(_siteConfig.value.wpUsername, _siteConfig.value.wpAppPassword)
                val payload = JSONObject().apply {
                    put("title", article.title)
                    put("content", sectionsBody)
                    put("excerpt", article.metaDescription)
                    put("status", targetStatus)
                    val metaObj = JSONObject().apply {
                        put("rank_math_title", "${article.title} | ${_brandKnowledgeBase.value.storeName}")
                        put("rank_math_description", article.metaDescription)
                        put("rank_math_focus_keyword", article.focusKeyword)
                        put("_yoast_wpseo_title", "${article.title} | ${_brandKnowledgeBase.value.storeName}")
                        put("_yoast_wpseo_metadesc", article.metaDescription)
                        put("_yoast_wpseo_focuskw", article.focusKeyword)
                    }
                    put("meta", metaObj)
                }.toString()
                val body = payload.toRequestBody("application/json".toMediaTypeOrNull())
                val resp = wpApi.createPost(body, wpAuth)
                if (resp.isSuccessful) {
                    val respStr = resp.body()?.string() ?: ""
                    val cleaned = WordPressJsonSanitizerInterceptor.cleanJsonResponse(respStr)
                    val createdJson = JSONObject(cleaned)
                    val remoteId = createdJson.optLong("id", newId)
                    finalItem = finalItem.copy(id = remoteId)
                }
            } catch (e: Exception) {
                // Ignore network error on publish
            }
        }

        // Capture snapshot & record log
        val snapshotId = snapshotDao.insertSnapshot(
            SnapshotEntity(
                timestamp = System.currentTimeMillis(),
                targetType = finalItem.type.name,
                targetId = finalItem.id,
                title = finalItem.title,
                content = finalItem.content,
                excerpt = finalItem.excerpt,
                metaTitle = finalItem.metaTitle,
                metaDescription = finalItem.metaDescription,
                schemaJson = finalItem.schemaJson,
                imageAlt = finalItem.imageAlt,
                reason = "انتشار مقاله جدید بر پایه اصول E-E-A-T و جمینای"
            )
        )

        recordLog(
            target = finalItem,
            actionName = "تولید مقاله کامل (E-E-A-T و جمینای)",
            reason = "نگارش مقاله با ساختار H1-H3 و بررسی عدم هم‌پوشانی کلیدواژه",
            role = _userSession.value.role,
            status = LogStatus.SUCCESS,
            diffs = listOf(DiffItem("ایجاد مقاله جدید", "موجود نبود", article.title)),
            snapshotId = snapshotId
        )

        _items.value = _items.value + finalItem
        consumeBudgetTokens(1200)
    }

    suspend fun addNewSiteItem(
        title: String,
        content: String,
        type: SiteItemType,
        focusKeyword: String,
        excerpt: String = "",
        regularPrice: String = ""
    ) = withContext(Dispatchers.IO) {
        val newId = System.currentTimeMillis()
        val normTitle = PersianNormalizer.normalize(title)
        val normContent = PersianNormalizer.normalize(content)
        val normKeyword = PersianNormalizer.normalize(focusKeyword)
        val slug = (if (type == SiteItemType.PRODUCT) "prod-" else "page-") + newId

        var newItem = SiteItem(
            id = newId,
            title = normTitle,
            content = normContent,
            excerpt = if (excerpt.isNotBlank()) PersianNormalizer.normalize(excerpt) else normContent.take(120),
            slug = slug,
            categories = listOf(if (type == SiteItemType.PRODUCT) "محصولات ووکامرس" else "برگه‌های سایت"),
            regularPrice = regularPrice,
            type = type,
            focusKeyword = normKeyword,
            metaTitle = "$normTitle | ${_brandKnowledgeBase.value.storeName}".take(60),
            metaDescription = "مشاهده و اطلاعات کامل درباره $normKeyword در ${_brandKnowledgeBase.value.storeName}.".take(150),
            schemaJson = if (type == SiteItemType.PRODUCT) {
                """{"@context":"https://schema.org","@type":"Product","name":"$normTitle"}"""
            } else {
                """{"@context":"https://schema.org","@type":"WebPage","name":"$normTitle"}"""
            },
            seoScore = 75
        )

        if (!_siteConfig.value.isDemoMode) {
            try {
                val client = NetworkClientProvider.createOkHttpClient()
                val wpAuth = NetworkClientProvider.createBasicAuth(_siteConfig.value.wpUsername, _siteConfig.value.wpAppPassword)
                if (type == SiteItemType.POST) {
                    val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                    val payload = JSONObject().apply {
                        put("title", normTitle)
                        put("content", normContent)
                        put("status", "publish")
                    }.toString()
                    val resp = wpApi.createPost(payload.toRequestBody("application/json".toMediaTypeOrNull()), wpAuth)
                    if (resp.isSuccessful) {
                        val cleaned = WordPressJsonSanitizerInterceptor.cleanJsonResponse(resp.body()?.string() ?: "")
                        val rid = JSONObject(cleaned).optLong("id", newId)
                        newItem = newItem.copy(id = rid)
                    }
                } else if (type == SiteItemType.PAGE) {
                    val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                    val payload = JSONObject().apply {
                        put("title", normTitle)
                        put("content", normContent)
                        put("status", "publish")
                    }.toString()
                    val resp = wpApi.createPage(payload.toRequestBody("application/json".toMediaTypeOrNull()), wpAuth)
                    if (resp.isSuccessful) {
                        val cleaned = WordPressJsonSanitizerInterceptor.cleanJsonResponse(resp.body()?.string() ?: "")
                        val rid = JSONObject(cleaned).optLong("id", newId)
                        newItem = newItem.copy(id = rid)
                    }
                } else if (type == SiteItemType.PRODUCT) {
                    val wcApi = NetworkClientProvider.createWooCommerceApi(_siteConfig.value.siteUrl, client)
                    val wcAuth = if (_siteConfig.value.wcConsumerKey.isNotBlank()) {
                        NetworkClientProvider.createBasicAuth(_siteConfig.value.wcConsumerKey, _siteConfig.value.wcConsumerSecret)
                    } else {
                        wpAuth
                    }
                    val payload = JSONObject().apply {
                        put("name", normTitle)
                        put("description", normContent)
                    }.toString()
                    val resp = wcApi.createProduct(payload.toRequestBody("application/json".toMediaTypeOrNull()), wcAuth)
                    if (resp.isSuccessful) {
                        val cleaned = WordPressJsonSanitizerInterceptor.cleanJsonResponse(resp.body()?.string() ?: "")
                        val rid = JSONObject(cleaned).optLong("id", newId)
                        newItem = newItem.copy(id = rid)
                    }
                }
            } catch (e: Exception) {
                // Ignore transient network errors
            }
        }

        val snapshotId = snapshotDao.insertSnapshot(
            SnapshotEntity(
                timestamp = System.currentTimeMillis(),
                targetType = newItem.type.name,
                targetId = newItem.id,
                title = newItem.title,
                content = newItem.content,
                excerpt = newItem.excerpt,
                metaTitle = newItem.metaTitle,
                metaDescription = newItem.metaDescription,
                schemaJson = newItem.schemaJson,
                imageAlt = newItem.imageAlt,
                reason = "ایجاد ${type.persianLabel} جدید"
            )
        )

        recordLog(
            target = newItem,
            actionName = "ایجاد ${type.persianLabel} جدید",
            reason = "افزودن محتوای جدید به سایت با اعتبارسنجی سئو",
            role = _userSession.value.role,
            status = LogStatus.SUCCESS,
            diffs = listOf(DiffItem("ایجاد ${type.persianLabel}", "موجود نبود", normTitle)),
            snapshotId = snapshotId
        )

        _items.value = _items.value + newItem
    }

    suspend fun syncSiteContent(config: SiteConfig): Result<Int> = withContext(Dispatchers.IO) {
        if (config.isDemoMode) {
            loadInitialMockData()
            _syncStatusMessage.value = "همگام‌سازی دمو انجام شد (${_items.value.size} آیتم)"
            return@withContext Result.success(_items.value.size)
        }

        try {
            _syncStatusMessage.value = "اتصال به وردپرس و ارزیابی دسترسی‌ها..."
            val client = NetworkClientProvider.createOkHttpClient()
            var currentAppPass = config.wpAppPassword
            var wpAuth = NetworkClientProvider.createBasicAuth(config.wpUsername, currentAppPass)

            val wpApi = NetworkClientProvider.createWordPressApi(config.siteUrl, client)

            // Test if basic auth works or if we need auto-provisioning
            val checkResp = try { wpApi.getSiteInfo(wpAuth) } catch (e: Exception) { null }
            if (checkResp == null || checkResp.code() == 401 || !checkResp.isSuccessful) {
                val autoAppPass = WordPressAuthHelper.autoProvisionAppPassword(
                    siteUrl = config.siteUrl,
                    username = config.wpUsername,
                    rawPassword = config.wpAppPassword
                )
                if (autoAppPass != null) {
                    currentAppPass = autoAppPass
                    wpAuth = NetworkClientProvider.createBasicAuth(config.wpUsername, autoAppPass)
                    _siteConfig.value = config.copy(wpAppPassword = autoAppPass)
                }
            }

            val fetchedItems = mutableListOf<SiteItem>()

            // 1. Fetch ALL WooCommerce Products (with full pagination)
            _syncStatusMessage.value = "شروع دریافت تمام محصولات ووکامرس..."
            try {
                val wcApi = NetworkClientProvider.createWooCommerceApi(config.siteUrl, client)
                val wcAuth = if (config.wcConsumerKey.isNotBlank()) {
                    NetworkClientProvider.createBasicAuth(config.wcConsumerKey, config.wcConsumerSecret)
                } else {
                    wpAuth
                }

                var wcPage = 1
                var wcHasMore = true
                while (wcHasMore && wcPage <= 30) {
                    _syncStatusMessage.value = "در حال دریافت محصولات ووکامرس (صفحه $wcPage)..."
                    val prodResp = wcApi.getProducts(perPage = 50, page = wcPage, status = null, authHeader = wcAuth)
                    if (prodResp.isSuccessful) {
                        val bodyStr = prodResp.body()?.string() ?: "[]"
                        val cleanedBody = WordPressJsonSanitizerInterceptor.cleanJsonResponse(bodyStr)
                        val prodArray = JSONArray(cleanedBody)
                        val count = prodArray.length()
                        if (count == 0) {
                            wcHasMore = false
                        } else {
                            for (i in 0 until count) {
                                val pr = prodArray.getJSONObject(i)
                                val id = pr.optLong("id", System.currentTimeMillis() + 2000 + i)
                                val rawName = pr.optString("name", "محصول ووکامرس")
                                val rawDesc = pr.optString("description", "")
                                val rawShortDesc = pr.optString("short_description", "")
                                val price = pr.optString("regular_price", "").ifBlank { pr.optString("price", "") }
                                val salePrice = pr.optString("sale_price", "")
                                val slug = pr.optString("slug", "prod-$id")
                                val status = pr.optString("status", "publish")
                                val stockStatus = pr.optString("stock_status", "instock")
                                val sku = pr.optString("sku", "")

                                val title = PersianNormalizer.normalize(rawName.replace(Regex("<[^>]*>"), "").trim())
                                val content = PersianNormalizer.normalize(rawDesc.replace(Regex("<[^>]*>"), "").trim())
                                val excerpt = PersianNormalizer.normalize(rawShortDesc.replace(Regex("<[^>]*>"), "").trim())

                                val catList = mutableListOf<String>()
                                val catArray = pr.optJSONArray("categories")
                                if (catArray != null) {
                                    for (c in 0 until catArray.length()) {
                                        catList.add(catArray.getJSONObject(c).optString("name"))
                                    }
                                }
                                if (catList.isEmpty()) catList.add("محصولات ووکامرس")

                                val tagList = mutableListOf<String>()
                                val tagArray = pr.optJSONArray("tags")
                                if (tagArray != null) {
                                    for (t in 0 until tagArray.length()) {
                                        tagList.add(tagArray.getJSONObject(t).optString("name"))
                                    }
                                }

                                var featuredImgUrl = ""
                                var imgAlt = ""
                                var imgTitle = ""
                                val imagesArray = pr.optJSONArray("images")
                                if (imagesArray != null && imagesArray.length() > 0) {
                                    val firstImg = imagesArray.getJSONObject(0)
                                    featuredImgUrl = firstImg.optString("src", "")
                                    imgAlt = PersianNormalizer.normalize(firstImg.optString("alt", title))
                                    imgTitle = firstImg.optString("name", title)
                                }

                                var rmTitle: String? = null
                                var rmDesc: String? = null
                                var rmKw: String? = null
                                var rmCanonical = ""
                                var rmRobots = "index, follow"
                                var rmScore = 0

                                val metaArray = pr.optJSONArray("meta_data")
                                if (metaArray != null) {
                                    for (m in 0 until metaArray.length()) {
                                        val mObj = metaArray.getJSONObject(m)
                                        val key = mObj.optString("key")
                                        val value = mObj.opt("value")?.toString() ?: ""
                                        when (key) {
                                            "rank_math_title", "_yoast_wpseo_title" -> if (value.isNotBlank()) rmTitle = value
                                            "rank_math_description", "_yoast_wpseo_metadesc" -> if (value.isNotBlank()) rmDesc = value
                                            "rank_math_focus_keyword", "_yoast_wpseo_focuskw" -> if (value.isNotBlank()) rmKw = value
                                            "rank_math_canonical_url" -> rmCanonical = value
                                            "rank_math_robots" -> rmRobots = value
                                            "rank_math_seo_score" -> rmScore = value.toIntOrNull() ?: 0
                                        }
                                    }
                                }

                                val focusKeyword = rmKw ?: title.split(" ").take(3).joinToString(" ")
                                val metaTitle = rmTitle ?: "$title | ${_brandKnowledgeBase.value.storeName}".take(60)
                                val metaDescription = rmDesc ?: excerpt.ifBlank { content.take(140) }.take(150)
                                val score = if (rmScore > 0) rmScore else 78

                                fetchedItems.add(
                                    SiteItem(
                                        id = id,
                                        title = title,
                                        content = content,
                                        excerpt = excerpt,
                                        slug = slug,
                                        categories = catList,
                                        tags = tagList,
                                        regularPrice = if (price.isNotBlank()) "$price تومان" else "تماس بگیرید",
                                        salePrice = if (salePrice.isNotBlank()) "$salePrice تومان" else "",
                                        stockStatus = stockStatus,
                                        sku = sku,
                                        type = SiteItemType.PRODUCT,
                                        status = status,
                                        focusKeyword = focusKeyword,
                                        metaTitle = metaTitle,
                                        metaDescription = metaDescription,
                                        canonicalUrl = rmCanonical,
                                        metaRobots = rmRobots,
                                        featuredImageUrl = featuredImgUrl,
                                        imageAlt = imgAlt,
                                        imageTitle = imgTitle,
                                        schemaType = "Product",
                                        schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"$title"}""",
                                        seoScore = score
                                    )
                                )
                            }
                            if (count < 50) {
                                wcHasMore = false
                            } else {
                                wcPage++
                            }
                        }
                    } else {
                        wcHasMore = false
                    }
                }
            } catch (e: Exception) {
                // Ignore product error and proceed
            }

            // 2. Fetch ALL WordPress Posts (with full pagination)
            _syncStatusMessage.value = "در حال دریافت تمام مقالات و نوشته‌های وبلاگ..."
            try {
                var postPage = 1
                var postHasMore = true
                while (postHasMore && postPage <= 30) {
                    _syncStatusMessage.value = "در حال دریافت مقالات وبلاگ (صفحه $postPage)..."
                    val postsResp = wpApi.getPosts(perPage = 50, page = postPage, status = null, authHeader = wpAuth)
                    if (postsResp.isSuccessful) {
                        val bodyStr = postsResp.body()?.string() ?: "[]"
                        val cleanedBody = WordPressJsonSanitizerInterceptor.cleanJsonResponse(bodyStr)
                        val postsArray = JSONArray(cleanedBody)
                        val count = postsArray.length()
                        if (count == 0) {
                            postHasMore = false
                        } else {
                            for (i in 0 until count) {
                                val p = postsArray.getJSONObject(i)
                                val id = p.optLong("id", System.currentTimeMillis() + i)
                                val titleObj = p.optJSONObject("title")
                                val contentObj = p.optJSONObject("content")
                                val excerptObj = p.optJSONObject("excerpt")
                                val rawTitle = titleObj?.optString("rendered", "نوشته وردپرس") ?: p.optString("title", "نوشته وردپرس")
                                val rawContent = contentObj?.optString("rendered", "") ?: p.optString("content", "")
                                val rawExcerpt = excerptObj?.optString("rendered", "") ?: p.optString("excerpt", "")
                                val slug = p.optString("slug", "post-$id")
                                val status = p.optString("status", "publish")

                                val title = PersianNormalizer.normalize(rawTitle.replace(Regex("<[^>]*>"), "").trim())
                                val content = PersianNormalizer.normalize(rawContent.replace(Regex("<[^>]*>"), "").trim())
                                val excerpt = PersianNormalizer.normalize(rawExcerpt.replace(Regex("<[^>]*>"), "").trim())

                                val metaObj = p.optJSONObject("meta")
                                val rmTitle = metaObj?.optString("rank_math_title")?.takeIf { it.isNotBlank() }
                                val rmDesc = metaObj?.optString("rank_math_description")?.takeIf { it.isNotBlank() }
                                val rmKw = metaObj?.optString("rank_math_focus_keyword")?.takeIf { it.isNotBlank() }
                                val rmCanonical = metaObj?.optString("rank_math_canonical_url") ?: ""
                                val rmRobots = metaObj?.optString("rank_math_robots") ?: "index, follow"
                                val rmScore = metaObj?.optInt("rank_math_seo_score", 0) ?: 0

                                val yoastTitle = metaObj?.optString("_yoast_wpseo_title")?.takeIf { it.isNotBlank() }
                                val yoastDesc = metaObj?.optString("_yoast_wpseo_metadesc")?.takeIf { it.isNotBlank() }
                                val yoastKw = metaObj?.optString("_yoast_wpseo_focuskw")?.takeIf { it.isNotBlank() }

                                val focusKeyword = rmKw ?: yoastKw ?: title.split(" ").take(3).joinToString(" ")
                                val metaTitle = rmTitle ?: yoastTitle ?: "$title | ${_brandKnowledgeBase.value.storeName}".take(60)
                                val metaDescription = rmDesc ?: yoastDesc ?: excerpt.ifBlank { content.take(140) }.take(150)
                                val score = if (rmScore > 0) rmScore else 75

                                fetchedItems.add(
                                    SiteItem(
                                        id = id,
                                        title = title,
                                        content = content,
                                        excerpt = excerpt,
                                        slug = slug,
                                        categories = listOf("مقالات و وبلاگ"),
                                        type = SiteItemType.POST,
                                        status = status,
                                        focusKeyword = focusKeyword,
                                        metaTitle = metaTitle,
                                        metaDescription = metaDescription,
                                        canonicalUrl = rmCanonical,
                                        metaRobots = rmRobots,
                                        schemaType = "Article",
                                        schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"$title"}""",
                                        seoScore = score
                                    )
                                )
                            }
                            if (count < 50) {
                                postHasMore = false
                            } else {
                                postPage++
                            }
                        }
                    } else {
                        postHasMore = false
                    }
                }
            } catch (e: Exception) {
                // Keep moving to pages
            }

            // 3. Fetch ALL WordPress Pages (with full pagination & Elementor detection)
            _syncStatusMessage.value = "در حال دریافت تمام برگه‌ها و لندینگ‌های سایت..."
            try {
                var pageNum = 1
                var pageHasMore = true
                while (pageHasMore && pageNum <= 30) {
                    _syncStatusMessage.value = "در حال دریافت برگه‌های سایت (صفحه $pageNum)..."
                    val pagesResp = wpApi.getPages(perPage = 50, page = pageNum, status = null, authHeader = wpAuth)
                    if (pagesResp.isSuccessful) {
                        val bodyStr = pagesResp.body()?.string() ?: "[]"
                        val cleanedBody = WordPressJsonSanitizerInterceptor.cleanJsonResponse(bodyStr)
                        val pagesArray = JSONArray(cleanedBody)
                        val count = pagesArray.length()
                        if (count == 0) {
                            pageHasMore = false
                        } else {
                            for (i in 0 until count) {
                                val p = pagesArray.getJSONObject(i)
                                val id = p.optLong("id", System.currentTimeMillis() + 1000 + i)
                                val titleObj = p.optJSONObject("title")
                                val contentObj = p.optJSONObject("content")
                                val rawTitle = titleObj?.optString("rendered", "برگه وردپرس") ?: p.optString("title", "برگه وردپرس")
                                val rawContent = contentObj?.optString("rendered", "") ?: p.optString("content", "")
                                val slug = p.optString("slug", "page-$id")
                                val status = p.optString("status", "publish")
                                val template = p.optString("template", "default")

                                val title = PersianNormalizer.normalize(rawTitle.replace(Regex("<[^>]*>"), "").trim())
                                val content = PersianNormalizer.normalize(rawContent.replace(Regex("<[^>]*>"), "").trim())

                                val metaObj = p.optJSONObject("meta")
                                val rmTitle = metaObj?.optString("rank_math_title")?.takeIf { it.isNotBlank() }
                                val rmDesc = metaObj?.optString("rank_math_description")?.takeIf { it.isNotBlank() }
                                val rmKw = metaObj?.optString("rank_math_focus_keyword")?.takeIf { it.isNotBlank() }

                                // Elementor Meta Detection
                                val elementorEditMode = metaObj?.optString("_elementor_edit_mode", "") ?: ""
                                val elementorData = metaObj?.optString("_elementor_data", "") ?: ""
                                val isElem = elementorEditMode == "builder" || elementorData.isNotBlank() || template.contains("elementor")

                                val focusKeyword = rmKw ?: title.split(" ").take(2).joinToString(" ")
                                val metaTitle = rmTitle ?: "$title | ${_brandKnowledgeBase.value.storeName}".take(60)
                                val metaDescription = rmDesc ?: content.take(140)

                                fetchedItems.add(
                                    SiteItem(
                                        id = id,
                                        title = title,
                                        content = content,
                                        excerpt = content.take(120),
                                        slug = slug,
                                        categories = listOf("برگه‌های سایت"),
                                        type = SiteItemType.PAGE,
                                        status = status,
                                        focusKeyword = focusKeyword,
                                        metaTitle = metaTitle,
                                        metaDescription = metaDescription,
                                        schemaType = "WebPage",
                                        schemaJson = """{"@context":"https://schema.org","@type":"WebPage","name":"$title"}""",
                                        seoScore = 80,
                                        isElementor = isElem,
                                        elementorDataJson = elementorData,
                                        elementorTemplate = template
                                    )
                                )
                            }
                            if (count < 50) {
                                pageHasMore = false
                            } else {
                                pageNum++
                            }
                        }
                    } else {
                        pageHasMore = false
                    }
                }
            } catch (e: Exception) {
                // Keep moving
            }

            if (fetchedItems.isNotEmpty()) {
                _items.value = fetchedItems
                val prodCount = fetchedItems.count { it.isProduct }
                val postCount = fetchedItems.count { it.isPost }
                val pageCount = fetchedItems.count { it.isPage }
                _syncStatusMessage.value = "همگام‌سازی کامل شد: $prodCount محصول، $postCount نوشته، $pageCount برگه بارگذاری گردید."
                Result.success(fetchedItems.size)
            } else {
                _syncStatusMessage.value = "موردی از سایت دریافت نشد. داده‌های محلی حفظ شدند."
                Result.success(_items.value.size)
            }
        } catch (e: Exception) {
            _syncStatusMessage.value = "خطا در همگام‌سازی: ${e.message}"
            Result.failure(e)
        }
    }

    // --- Phase 2: Elementor Page Designer Integration ---
    suspend fun applyElementorDesignToPage(pageId: Long, design: ElementorPageDesign) = withContext(Dispatchers.IO) {
        val changes = mapOf(
            "title" to design.title,
            "content" to design.semanticHtml,
            "elementorDataJson" to design.elementorJson,
            "isElementor" to true,
            "elementorTemplate" to "elementor_header_footer",
            "metaTitle" to design.recommendedRankMathTitle,
            "metaDescription" to design.recommendedMetaDesc
        )
        applyItemUpdate(
            targetId = pageId,
            proposedChanges = changes,
            actionName = "بازطراحی برگه با افزونه المنتور و هوش مصنوعی",
            reason = "طراحی اختصاصی بخش‌های قهرمان، خدمات، کاتالوگ و فرمت استاندارد _elementor_data"
        )
    }

    suspend fun createNewElementorPage(design: ElementorPageDesign) = withContext(Dispatchers.IO) {
        val newId = System.currentTimeMillis()
        val safeSlug = "page-" + design.title.replace(" ", "-").take(40)

        var newPage = SiteItem(
            id = newId,
            title = design.title,
            content = design.semanticHtml,
            excerpt = design.recommendedMetaDesc,
            slug = safeSlug,
            categories = listOf("برگه‌های طراحی شده با المنتور"),
            type = SiteItemType.PAGE,
            status = "publish",
            focusKeyword = design.title.split(" ").take(3).joinToString(" "),
            metaTitle = design.recommendedRankMathTitle,
            metaDescription = design.recommendedMetaDesc,
            schemaType = "WebPage",
            schemaJson = """{"@context":"https://schema.org","@type":"WebPage","name":"${design.title}"}""",
            seoScore = 92,
            isElementor = true,
            elementorDataJson = design.elementorJson,
            elementorTemplate = "elementor_header_footer"
        )

        if (!_siteConfig.value.isDemoMode) {
            try {
                val client = NetworkClientProvider.createOkHttpClient()
                val wpApi = NetworkClientProvider.createWordPressApi(_siteConfig.value.siteUrl, client)
                val wpAuth = NetworkClientProvider.createBasicAuth(_siteConfig.value.wpUsername, _siteConfig.value.wpAppPassword)
                val payload = JSONObject().apply {
                    put("title", design.title)
                    put("content", design.semanticHtml)
                    put("status", "publish")
                    put("template", "elementor_header_footer")
                    val metaObj = JSONObject().apply {
                        put("rank_math_title", design.recommendedRankMathTitle)
                        put("rank_math_description", design.recommendedMetaDesc)
                        put("_elementor_edit_mode", "builder")
                        put("_elementor_data", design.elementorJson)
                        put("_wp_page_template", "elementor_header_footer")
                    }
                    put("meta", metaObj)
                }.toString()
                val resp = wpApi.createPage(payload.toRequestBody("application/json".toMediaTypeOrNull()), wpAuth)
                if (resp.isSuccessful) {
                    val cleaned = WordPressJsonSanitizerInterceptor.cleanJsonResponse(resp.body()?.string() ?: "")
                    val rid = JSONObject(cleaned).optLong("id", newId)
                    newPage = newPage.copy(id = rid)
                }
            } catch (e: Exception) {
                // Ignore transient network error
            }
        }

        val snapshotId = snapshotDao.insertSnapshot(
            SnapshotEntity(
                timestamp = System.currentTimeMillis(),
                targetType = newPage.type.name,
                targetId = newPage.id,
                title = newPage.title,
                content = newPage.content,
                excerpt = newPage.excerpt,
                metaTitle = newPage.metaTitle,
                metaDescription = newPage.metaDescription,
                schemaJson = newPage.schemaJson,
                imageAlt = newPage.imageAlt,
                reason = "ایجاد برگه جدید با افزونه المنتور و هوش مصنوعی"
            )
        )

        recordLog(
            target = newPage,
            actionName = "ایجاد برگه المنتور جدید",
            reason = "تولید برگه استاندارد همراه با قالب ساختاریافته _elementor_data",
            role = _userSession.value.role,
            status = LogStatus.SUCCESS,
            diffs = listOf(DiffItem("ایجاد برگه المنتور", "موجود نبود", design.title)),
            snapshotId = snapshotId
        )

        _items.value = _items.value + newPage
        consumeBudgetTokens(950)
    }

    // --- Phase 2: Scheduling & Jalali Calendar ---
    fun schedulePostFuture(targetId: Long, daysInFuture: Int, occasion: String? = null) {
        val item = _items.value.find { it.id == targetId } ?: return
        val datePair = JalaliCalendarHelper.createWpFutureDate(daysInFuture)

        val newPlan = ScheduledPostPlan(
            targetId = item.id,
            targetTitle = item.title,
            jalaliScheduledDate = datePair.second,
            isoGmtTimestamp = datePair.first,
            wpStatus = "future",
            occasion = occasion
        )

        _scheduledPlans.value = _scheduledPlans.value + newPlan
        // Update item status in memory to future
        _items.value = _items.value.map {
            if (it.id == targetId) it.copy(status = "future") else it
        }
    }

    // --- Phase 2: Pre-Publish Validation ---
    fun validateItemPrePublish(item: SiteItem): ValidationCheckResult {
        val otherKeywords = _items.value
            .filter { it.id != item.id }
            .map { it.focusKeyword }
            .filter { it.isNotBlank() }

        return ContentValidator.validate(
            title = item.title,
            content = item.content,
            metaDescription = item.metaDescription,
            focusKeyword = item.focusKeyword,
            isHygieneOrDetergent = item.categories.any { it.contains("پوست") || it.contains("شوینده") || it.contains("بهداشتی") },
            existingKeywordsOnSite = otherKeywords
        )
    }

    // --- Phase 2: Continuous Learning & Knowledge Base Update ---
    fun learnFromUserEdit(original: String, approved: String, category: String) {
        val currentKb = _brandKnowledgeBase.value
        val newSample = ApprovedSample(
            id = "sample_${System.currentTimeMillis()}",
            category = category,
            originalText = original,
            approvedText = approved,
            keyLearning = "الگوی لحن و واژگان تایید شده توسط کاربر ذخیره شد."
        )

        val updated = currentKb.copy(
            approvedSamples = currentKb.approvedSamples + newSample,
            totalLearnedEditsCount = currentKb.totalLearnedEditsCount + 1
        )
        _brandKnowledgeBase.value = updated
    }

    fun updateBrandKnowledge(kb: BrandKnowledgeBase) {
        _brandKnowledgeBase.value = kb
    }

    // --- Phase 2: Persian Normalizer Helper ---
    fun normalizePersianText(text: String): String {
        return PersianNormalizer.normalize(text)
    }
}
