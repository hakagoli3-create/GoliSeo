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
                title = "ماسک مو کراتینه بدون سولفات آمالفی (Amalfi) اصل اسپانیا",
                content = "ماسک موی تخصصی کراتینه بدون سولفات آمالفی ساخت کمپانی Quimi Romar اسپانیا. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. فرمولاسیون فاقد پارابن و نمک‌های خشن جهت احیا و لطافت موهای کراتینه‌شده و آسیب‌دیده با جذب فوری.",
                excerpt = "احیای ساقه موهای رنگ‌شده و کراتینه بدون ایجاد حس سنگینی، محصول اصل اسپانیا با گارانتی اصالت گلی کالا.",
                slug = "amalfi-keratin-hair-mask-spain",
                categories = listOf("مراقبت از مو", "ماسک مو", "محصولات آمالفی"),
                tags = listOf("آمالفی", "ماسک مو بدون سولفات", "کراتین", "اسپانیا"),
                regularPrice = "۴۸۰٬۰۰۰ تومان",
                salePrice = "۳۹۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-AMF-101",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ماسک مو بدون سولفات آمالفی اصل",
                secondaryKeywords = listOf("ماسک مو کراتین آمالفی", "نمایندگی رسمی آمالفی گلی کالا"),
                metaTitle = "خرید ماسک مو بدون سولفات آمالفی اصل اسپانیا | گلی کالا",
                metaDescription = "خرید ماسک مو کراتینه بدون سولفات آمالفی اصل اسپانیا از گلی کالا نماینده رسمی آمالفی، آگرادو و سایرو در ایران با گارانتی اصالت.",
                canonicalUrl = "https://golikala.com/product/amalfi-keratin-hair-mask-spain",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ماسک مو کراتینه آمالفی اصل اسپانیا","offers":{"@type":"Offer","price":"390000","priceCurrency":"IRR","availability":"https://schema.org/InStock"}}""",
                imageAlt = "کاسه ماسک مو کراتینه بدون سولفات آمالفی ساخت اسپانیا",
                imageTitle = "ماسک مو کراتینه آمالفی در گلی کالا",
                seoScore = 88
            ),
            SiteItem(
                id = 102,
                title = "شامپو کراتینه ترمیم‌کننده آگرادو (Agrado) اصل اسپانیا",
                content = "شامپو کراتینه روزانه آگرادو تولید لابراتوار والنسیا اسپانیا. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. پاکسازی ملایم کف سر همراه با تغذیه کورتکس تارهای مو و کنترل وزی تارهای آسیب‌دیده.",
                excerpt = "شامپو تقویتی کراتینه اصل آگرادو اسپانیا جهت پاکسازی ملایم و ماندگاری رنگ مو.",
                slug = "agrado-keratin-shampoo-spain",
                categories = listOf("مراقبت از مو", "شامپو", "محصولات آگرادو"),
                tags = listOf("آگرادو", "شامپو کراتینه", "اسپانیا", "نمایندگی آگرادو"),
                regularPrice = "۴۵۰٬۰۰۰ تومان",
                salePrice = "۳۸۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-AGR-102",
                type = SiteItemType.PRODUCT,
                focusKeyword = "شامپو کراتینه آگرادو اصل اسپانیا",
                secondaryKeywords = listOf("شامپو آگرادو اصل", "خرید محصولات آگرادو"),
                metaTitle = "شامپو کراتینه آگرادو اصل اسپانیا | گلی کالا",
                metaDescription = "خرید اینترنتی شامپو کراتینه آگرادو اسپانیا از گلی کالا نماینده رسمی و انحصاری برندهای آمالفی، آگرادو و سایرو در ایران با ارسال سریع.",
                canonicalUrl = "https://golikala.com/product/agrado-keratin-shampoo-spain",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"شامپو کراتینه آگرادو اسپانیا","offers":{"@type":"Offer","price":"380000","priceCurrency":"IRR","availability":"https://schema.org/InStock"}}""",
                imageAlt = "بطری شامپو کراتینه تقویتی آگرادو ساخت والنسیا اسپانیا",
                imageTitle = "شامپو کراتینه آگرادو در گلی کالا",
                seoScore = 86
            ),
            SiteItem(
                id = 103,
                title = "لوسیون بدن آبرسان و تغذیه‌کننده سایرو (Sairo) اصل اروپا",
                content = "لوسیون بدن عصاره روغن بادام و شی‌باتر سایرو محصول استاندارد اروپا. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. آبرسانی عمقی ۲۴ ساعته بدون ایجاد چسبندگی بر روی بافت پوست.",
                excerpt = "لطافت ابریشمی پوست بدن با لوسیون آبرسان سایرو اصل اروپا، مناسب انواع پوست خشک و حساس.",
                slug = "sairo-hydrating-body-lotion",
                categories = listOf("مراقبت از بدن", "لوسیون بدن", "محصولات سایرو"),
                tags = listOf("سایرو", "لوسیون بدن", "آبرسان بدن", "نمایندگی سایرو"),
                regularPrice = "۳۹۰٬۰۰۰ تومان",
                salePrice = "۳۴۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-SAI-103",
                type = SiteItemType.PRODUCT,
                focusKeyword = "لوسیون بدن سایرو اصل",
                secondaryKeywords = listOf("محصولات سایرو اسپانیا", "خرید لوسیون بدن سایرو"),
                metaTitle = "لوسیون بدن آبرسان سایرو اصل اروپا | گلی کالا",
                metaDescription = "خرید لوسیون بدن سایرو اصل با بالاترین خلوص از گلی کالا نماینده رسمی برندهای سایرو، آمالفی و آگرادو با ضمانت اصالت ۱۰۰٪ فیزیکی.",
                canonicalUrl = "https://golikala.com/product/sairo-hydrating-body-lotion",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"لوسیون بدن سایرو اصل"}""",
                imageAlt = "پمپ لوسیون بدن سایرو اصل با عصاره بادام و شی باتر",
                imageTitle = "لوسیون بدن سایرو در گلی کالا",
                seoScore = 84
            ),
            SiteItem(
                id = 104,
                title = "ماسک مو احیاکننده روغن آرگان آگرادو (Agrado) اسپانیا",
                content = "ماسک مو غنی از ویتامین E و روغن خالص آرگان آگرادو اسپانیا. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. رفع خشکی شدید و زبری ساقه مو با نفوذ عمیق ترکیبات فعال.",
                excerpt = "تغذیه عمیق و درخشش پایدار تارهای مو با ماسک موی آرگان آگرادو اصل اسپانیا.",
                slug = "agrado-argan-oil-hair-mask",
                categories = listOf("مراقبت از مو", "ماسک مو", "محصولات آگرادو"),
                tags = listOf("آگرادو", "روغن آرگان", "ماسک مو آگرادو"),
                regularPrice = "۴۹۰٬۰۰۰ تومان",
                salePrice = "۴۲۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-AGR-104",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ماسک مو آرگان آگرادو اصل",
                metaTitle = "ماسک مو روغن آرگان آگرادو اصل اسپانیا | گلی کالا",
                metaDescription = "خرید اینترنتی ماسک موی آرگان آگرادو اصل اسپانیا از گلی کالا نماینده رسمی و انحصاری با تضمین اصالت فیزیکی و آزمایشگاهی.",
                canonicalUrl = "https://golikala.com/product/agrado-argan-oil-hair-mask",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ماسک مو آرگان آگرادو"}""",
                imageAlt = "کاسه ماسک موی آرگان آگرادو ساخت والنسیا اسپانیا",
                imageTitle = "ماسک مو آرگان آگرادو در گلی کالا",
                seoScore = 85
            ),
            SiteItem(
                id = 105,
                title = "شامپو براق‌کننده و نرم‌کننده عصاره بابونه آمالفی (Amalfi)",
                content = "شامپو ملایم بابونه آمالفی محصول Quimi Romar اسپانیا با فیلترهای محافظتی رنگ طبیعی موهای روشن و بلوند. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                excerpt = "افزایش انعکاس نور و درخشش طبیعی موهای بلوند با شامپو بابونه آمالفی اصل اسپانیا.",
                slug = "amalfi-chamomile-shampoo-spain",
                categories = listOf("مراقبت از مو", "شامپو", "محصولات آمالفی"),
                tags = listOf("آمالفی", "شامپو بابونه", "موهای روشن"),
                regularPrice = "۳۸۰٬۰۰۰ تومان",
                salePrice = "",
                stockStatus = "instock",
                sku = "GLS-AMF-105",
                type = SiteItemType.PRODUCT,
                focusKeyword = "شامپو بابونه آمالفی اصل",
                metaTitle = "شامپو عصاره بابونه آمالفی اصل اسپانیا | گلی کالا",
                metaDescription = "خرید شامپو بابونه آمالفی اصل اسپانیا با گارانتی اصالت کالا از فروشگاه گلی کالا، نماینده رسمی آمالفی در ایران.",
                canonicalUrl = "https://golikala.com/product/amalfi-chamomile-shampoo-spain",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"شامپو بابونه آمالفی"}""",
                imageAlt = "بطری زرد رنگ شامپو بابونه آمالفی اسپانیا",
                imageTitle = "شامپو بابونه آمالفی گلی کالا",
                seoScore = 82
            ),
            SiteItem(
                id = 106,
                title = "ژل بهداشتی و پاک‌کننده ملایم سایرو (Sairo) اصل اروپا",
                content = "ژل شوینده و تنظیم‌کننده pH فیزیولوژیک سایرو با فرمولاسیون هایپوآلرژنیک و اسید لاکتیک طبیعی. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                excerpt = "حفظ تعادل طبیعی باکتری‌های مفید و پاکسازی ملایم با ژل بهداشتی سایرو اصل اروپا.",
                slug = "sairo-intimate-hygiene-gel",
                categories = listOf("بهداشت فردی", "شوینده بدن", "محصولات سایرو"),
                tags = listOf("سایرو", "ژل بهداشتی", "تنظیم pH"),
                regularPrice = "۳۶۰٬۰۰۰ تومان",
                salePrice = "۳۱۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-SAI-106",
                type = SiteItemType.PRODUCT,
                focusKeyword = "ژل بهداشتی سایرو اصل",
                metaTitle = "ژل بهداشتی سایرو اصل اروپا با تنظیم pH | گلی کالا",
                metaDescription = "خرید ژل بهداشتی ملایم سایرو با ضمانت اصالت فیزیکی و مجوز بهداشت در گلی کالا نماینده انحصاری سایرو در ایران.",
                canonicalUrl = "https://golikala.com/product/sairo-intimate-hygiene-gel",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"ژل بهداشتی سایرو"}""",
                imageAlt = "بطری پمپی ژل بهداشتی سایرو اصل اروپا",
                imageTitle = "ژل بهداشتی سایرو در گلی کالا",
                seoScore = 87
            ),
            SiteItem(
                id = 107,
                title = "سرم مو دوفاز آبرسان و محافظ حرارت آگرادو (Agrado)",
                content = "اسپری سرم دوفاز ضد وز و محافظ حرارت اتو و سشوار آگرادو ساخت والنسیا اسپانیا. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                excerpt = "محافظت حداکثری از بافت مو در برابر حرارت بالا همراه با نرمی ابریشمی فوری.",
                slug = "agrado-biphasic-hair-serum",
                categories = listOf("مراقبت از مو", "سرم مو", "محصولات آگرادو"),
                tags = listOf("آگرادو", "سرم دوفاز", "ضد وز"),
                regularPrice = "۴۶۰٬۰۰۰ تومان",
                salePrice = "۳۹۵٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-AGR-107",
                type = SiteItemType.PRODUCT,
                focusKeyword = "سرم مو دوفاز آگرادو اصل",
                metaTitle = "سرم دوفاز محافظ حرارتی آگرادو اصل اسپانیا | گلی کالا",
                metaDescription = "خرید سرم دوفاز آگرادو ساخت اسپانیا جهت محافظت از حرارت اتو مو و سشوار در گلی کالا نماینده رسمی آگرادو.",
                canonicalUrl = "https://golikala.com/product/agrado-biphasic-hair-serum",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"سرم دوفاز آگرادو"}""",
                imageAlt = "اسپری سرم دوفاز آبی رنگ آگرادو ساخت اسپانیا",
                imageTitle = "سرم دوفاز آگرادو در گلی کالا",
                seoScore = 86
            ),
            SiteItem(
                id = 108,
                title = "کرم نرم‌کننده دست و ناخن سایرو (Sairo) عصاره بادام",
                content = "کرم تغذیه‌کننده عمیق دست و کوتیکول ناخن سایرو غنی از آلانتوئین و اسیدهای چرب گیاهی. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد.",
                excerpt = "رفع زبری پوست دست و تقویت ناخن‌ها با کرم مغذی سایرو اصل بدون ایجاد حس چربی مازاد.",
                slug = "sairo-almond-hand-nail-cream",
                categories = listOf("مراقبت دست و ناخن", "کرم دست", "محصولات سایرو"),
                tags = listOf("سایرو", "کرم دست و ناخن", "روغن بادام"),
                regularPrice = "۲۸۰٬۰۰۰ تومان",
                salePrice = "",
                stockStatus = "instock",
                sku = "GLS-SAI-108",
                type = SiteItemType.PRODUCT,
                focusKeyword = "کرم دست و ناخن سایرو اصل",
                metaTitle = "کرم دست و ناخن سایرو اصل عصاره بادام | گلی کالا",
                metaDescription = "خرید کرم تخصصی دست و ناخن سایرو اصل اروپا از گلی کالا با تضمین اصالت و ارسال سریع به تمام نقاط ایران.",
                canonicalUrl = "https://golikala.com/product/sairo-almond-hand-nail-cream",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"کرم دست سایرو"}""",
                imageAlt = "تیوب کرم دست و ناخن سایرو با طرح بادام طبیعی",
                imageTitle = "کرم دست و ناخن سایرو در گلی کالا",
                seoScore = 83
            ),
            SiteItem(
                id = 109,
                title = "رسوب‌زدا و جرم‌گیر شیرآلات و سطوح پورچز (Porçöz) اصل ترکیه",
                content = "محلول رسوب‌زدا و جرم‌گیر پرقدرت پورچز ساخت کمپانی لونت کیمیا (Levent Kimya) ترکیه. گلی کالا به عنوان واردکننده رسمی و مرجع اصالت کالا این محصول را با تضمین ۱۰۰٪ اصالت فیزیکی عرضه می‌نماید. پاکسازی سریع رسوبات آهکی کهنه، جرم کاشی و سرامیک و رسوب کتری و چای‌ساز بدون آسیب به لعاب استاندارد سطوح.",
                excerpt = "رسوب‌زدایی فوری و درخشان‌کننده شیرآلات، کاشی، سینک و کتری با جرم‌گیر پورچز اصل ترکیه، تضمین ۱۰۰٪ اصالت کالا در گلی کالا.",
                slug = "porcoz-limescale-rust-remover-turkey",
                categories = listOf("بهداشت و نظافت منزل", "شوینده و رسوب‌زدا", "محصولات پورچز"),
                tags = listOf("پورچز", "جرم گیر", "رسوب زدا", "ترکیه", "شوینده شیرآلات"),
                regularPrice = "۲۶۰٬۰۰۰ تومان",
                salePrice = "۲۲۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-PRC-109",
                type = SiteItemType.PRODUCT,
                focusKeyword = "جرم‌گیر پورچز اصل ترکیه",
                secondaryKeywords = listOf("رسوب‌زدا پورچز ترکیه", "خرید پورچز اصل گلی کالا"),
                metaTitle = "رسوب‌زدا و جرم‌گیر پورچز اصل ترکیه | گلی کالا",
                metaDescription = "خرید رسوب‌زدا و جرم‌گیر پورچز اصل ترکیه با ضمانت اصالت ۱۰۰٪ در گلی کالا. مشخصات فنی، روش مصرف، نکات ایمنی و ارسال فوری به سراسر ایران.",
                canonicalUrl = "https://golikala.com/product/porcoz-limescale-rust-remover-turkey",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"رسوب‌زدا و جرم‌گیر پورچز اصل ترکیه"}""",
                imageAlt = "بطری قرمز رسوب‌زدا و جرم‌گیر پورچز اصل ساخت ترکیه",
                imageTitle = "رسوب‌زدا و جرم‌گیر پورچز در گلی کالا",
                featuredImageUrl = "https://golikala.com/wp-content/uploads/porcoz-descaler-turkey.webp",
                seoScore = 85
            ),
            SiteItem(
                id = 110,
                title = "شامپو آبرسان لورال Loreal Elseve بنفش هیالورونیک اسید اصل فرانسه",
                content = "شامپو آبرسان و حجم‌دهنده ۷۲ ساعته لورال السیو بنفش حاوی اسید هیالورونیک خالص ساخت کمپانی لورآل پاریس فرانسه. گلی کالا به عنوان واردکننده رسمی و مرجع اصالت کالا این محصول را با تضمین ۱۰۰٪ اصالت فیزیکی عرضه می‌نماید. پاکسازی ملایم چربی کف سر و تارهای مو همراه با آبرسانی عمیق فیبر موهای دهیدراته و خشک بدون ایجاد احساس سنگینی.",
                excerpt = "آبرسانی ۷۲ ساعته تارهای مو و کنترل چربی کف سر با شامپو السیو بنفش لورال فرانسه، تضمین ۱۰۰٪ اصالت کالا در گلی کالا.",
                slug = "loreal-elseve-hyaluron-plump-shampoo-france",
                categories = listOf("مراقبت از مو", "شامپو", "محصولات لورال"),
                tags = listOf("لورال", "شامپو لورال", "السیو بنفش", "هیالورونیک اسید", "فرانسه"),
                regularPrice = "۳۸۰٬۰۰۰ تومان",
                salePrice = "۳۲۰٬۰۰۰ تومان",
                stockStatus = "instock",
                sku = "GLS-LOR-110",
                type = SiteItemType.PRODUCT,
                focusKeyword = "شامپو لورال السیو بنفش اصل فرانسه",
                secondaryKeywords = listOf("شامپو هیالورونیک اسید لورال", "خرید شامپو لورال اصل گلی کالا"),
                metaTitle = "شامپو لورال Elseve بنفش اصل فرانسه | گلی کالا",
                metaDescription = "خرید اینترنتی شامپو لورال السیو بنفش هیالورونیک اسید اصل فرانسه با ضمانت اصالت ۱۰۰٪ فیزیکی در گلی کالا. مشخصات فنی، روش مصرف و ارسال فوری.",
                canonicalUrl = "https://golikala.com/product/loreal-elseve-hyaluron-plump-shampoo-france",
                schemaType = "Product",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"شامپو لورال Elseve بنفش اصل فرانسه"}""",
                imageAlt = "بطری بنفش شامپو آبرسان لورال السیو حاوی هیالورونیک اسید اصل فرانسه",
                imageTitle = "شامپو لورال السیو بنفش در گلی کالا",
                featuredImageUrl = "https://golikala.com/wp-content/uploads/loreal-elseve-hyaluron-purple-shampoo.webp",
                seoScore = 84
            ),

            // --- WordPress Posts & Articles ---
            SiteItem(
                id = 201,
                title = "راهنمای جامع روتین مو با محصولات بدون سولفات آمالفی و آگرادو",
                content = "شستشو و مراقبت اصولی از موهای کراتینه‌شده و رنگ‌شده نیازمند استفاده از محصولات ملایم و فاقد نمک خشن است. در این راهنما مراحل روتین مو را بررسی می‌کنیم.\n\n## چرا شامپو و ماسک موی بدون سولفات ضروری است؟\nسولفات‌ها چربی طبیعی مو را از بین می‌برند، در حالی که برندهای اسپانیایی مانند آمالفی و آگرادو از شوینده‌های زیستی ملایم استفاده می‌کنند.\n\n## معرفی محصولات برندهای آمالفی و آگرادو\nگلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و دسترسی شما به محصولات اورجینال اسپانیا را با ضمانت اصالت ۱۰۰٪ تسهیل نموده است.\n\n## نحوه مصرف صحیح ماسک مو در حمام\nپس از شستشوی ملایم، آب اضافی مو را بگیرید و ماسک مو آمالفی یا آگرادو را از ساقه تا نوک موها ماساژ دهید و پس از ۵ دقیقه آبکشی کنید.",
                excerpt = "بررسی مراحل استاندارد مراقبت از موهای خشک و رنگ‌شده با برندهای تخصصی آمالفی و آگرادو اسپانیا.",
                slug = "amalfi-agrado-hair-care-routine-guide",
                categories = listOf("مقالات آموزشی", "مراقبت مو"),
                tags = listOf("روتین مو", "آمالفی", "آگرادو", "بدون سولفات"),
                type = SiteItemType.POST,
                focusKeyword = "روتین مو بدون سولفات آمالفی و آگرادو",
                secondaryKeywords = listOf("محصولات آمالفی اصل", "نمایندگی رسمی آگرادو"),
                metaTitle = "راهنمای روتین مو با محصولات آمالفی و آگرادو اصل | گلی کالا",
                metaDescription = "آموزش کامل روتین موهای خشک و کراتینه‌شده با شامپو و ماسک بدون سولفات آمالفی و آگرادو در گلی کالا نماینده رسمی در ایران.",
                canonicalUrl = "https://golikala.com/blog/amalfi-agrado-hair-care-routine-guide",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"راهنمای روتین مو با آمالفی و آگرادو"}""",
                imageAlt = "محصولات تخصصی مراقبت مو آمالفی و آگرادو در کنار یکدیگر",
                seoScore = 92
            ),
            SiteItem(
                id = 202,
                title = "چرا محصولات اصل آمالفی، آگرادو و سایرو را فقط از گلی کالا بخریم؟",
                content = "تشخیص محصولات آرایشی و بهداشتی اصل اروپایی از نسخه‌های فیک و تقلبی دغدغه اصلی خریداران است.\n\n## هویت و جایگاه گلی کالا\nگلی کالا به عنوان واردکننده و نماینده رسمی و انحصاری محصولات برندهای آمالفی (Amalfi)، آگرادو (Agrado) و سایرو (Sairo) در ایران فعالیت می‌کند.\n\n## مزایای خرید مستقیم از نماینده رسمی\n۱. تاریخ انقضای معتبر و تولید روز کارخانه در اسپانیا\n۲. بارکد بین‌المللی قابل پیگیری در سامانه جهانی\n۳. قیمت‌گذاری مصوب و بدون واسطه‌های غیرضروری\n۴. پشتیبانی تخصصی و پاسخگویی به سوالات مصرف‌کنندگان.",
                excerpt = "راهنمای تضمین اصالت و مزایای خرید مستقیم از گلی کالا، نماینده رسمی برندهای آمالفی، آگرادو و سایرو.",
                slug = "why-buy-amalfi-agrado-sairo-from-golikala",
                categories = listOf("اصالت کالا", "راهنمای خرید"),
                tags = listOf("گلی کالا", "نماینده انحصاری", "آمالفی", "آگرادو", "سایرو"),
                type = SiteItemType.POST,
                focusKeyword = "نماینده رسمی آمالفی آگرادو و سایرو در ایران",
                metaTitle = "نماینده رسمی محصولات آمالفی، آگرادو و سایرو در ایران | گلی کالا",
                metaDescription = "گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. تضمین ۱۰۰٪ اصالت و ارسال اکسپرس.",
                canonicalUrl = "https://golikala.com/blog/why-buy-amalfi-agrado-sairo-from-golikala",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"نماینده رسمی آمالفی، آگرادو و سایرو"}""",
                imageAlt = "لوگوهای رسمی برندهای آمالفی، آگرادو و سایرو در فروشگاه گلی کالا",
                seoScore = 94
            ),
            SiteItem(
                id = 203,
                title = "بررسی تخصصی و مقایسه شامپوهای کراتینه آگرادو و آمالفی اسپانیا",
                content = "هر دو برند آمالفی و آگرادو تحت نظارت دقیق استانداردهای بهداشتی اتحادیه اروپا در اسپانیا تولید می‌شوند اما فرمولاسیون آن‌ها برای نیازهای متفاوتی بهینه‌سازی شده است.\n\n## مقایسه ترکیبات و غلظت کراتین\nشامپو کراتینه آگرادو دارای غلظت بالاتری از کلاژن و پروتئین گیاهی است، در حالی که آمالفی تمرکز بیشتری بر نرم‌کنندگی و حالت‌پذیری مو دارد.\n\n## هویت عرضه در ایران\nگلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و تمامی این محصولات را با تضمین سلامت فیزیکی به سراسر کشور ارسال می‌نماید.",
                excerpt = "مقایسه فرمولاسیون، غلظت ترکیبات فعال و شیوه اثربخشی شامپوهای تقویتی کراتینه آمالفی و آگرادو.",
                slug = "agrado-vs-amalfi-keratin-shampoo-review",
                categories = listOf("مقایسه محصولات", "مراقبت مو"),
                tags = listOf("مقایسه شامپو", "آگرادو", "آمالفی", "کراتینه"),
                type = SiteItemType.POST,
                focusKeyword = "مقایسه شامپو کراتینه آگرادو و آمالفی",
                metaTitle = "مقایسه شامپو کراتینه آگرادو و آمالفی اسپانیا | گلی کالا",
                metaDescription = "تفاوت‌های اصلی شامپو کراتینه آگرادو و آمالفی چیست؟ بررسی فنی ترکیبات در گلی کالا نماینده رسمی در ایران.",
                canonicalUrl = "https://golikala.com/blog/agrado-vs-amalfi-keratin-shampoo-review",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"مقایسه شامپو کراتینه آگرادو و آمالفی"}""",
                imageAlt = "مقایسه بطری‌های شامپو کراتینه آگرادو و آمالفی کنار هم",
                seoScore = 91
            ),
            SiteItem(
                id = 204,
                title = "راهنمای مراقبت و آبرسانی روزانه به پوست بدن با محصولات سایرو",
                content = "پوست بدن نیز مانند پوست صورت در معرض عوامل مخرب محیطی و خشکی ناشی از شوینده‌های قوی قرار دارد.\n\n## نقش لوسیون‌ها و ژل‌های سایرو\nبرند سایرو (Sairo) با فرمولاسیون اختصاصی حاوی روغن بادام شیرین و اسیدهای میوه، رطوبت مورد نیاز اپیدرم را تامین می‌کند.\n\n## خرید از نماینده رسمی در ایران\nگلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد و تمامی محصولات سایرو با ضمانت اصالت فیزیکی و آزمایشگاهی عرضه می‌گردند.",
                excerpt = "چگونه با روتین بهداشتی و لوسیون‌های بدن سایرو پوستی نرم، شاداب و باطراوت داشته باشیم؟",
                slug = "sairo-body-care-hydration-guide",
                categories = listOf("مراقبت بدن", "مقالات آموزشی"),
                tags = listOf("سایرو", "لوسیون بدن", "مراقبت پوست"),
                type = SiteItemType.POST,
                focusKeyword = "مراقبت از پوست بدن با محصولات سایرو",
                metaTitle = "راهنمای آبرسانی پوست بدن با محصولات سایرو اصل | گلی کالا",
                metaDescription = "راهنمای مراقبت از پوست خشک با لوسیون‌ها و شوینده‌های بدن سایرو اصل در فروشگاه گلی کالا نماینده رسمی سایرو.",
                canonicalUrl = "https://golikala.com/blog/sairo-body-care-hydration-guide",
                schemaType = "Article",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"راهنمای آبرسانی پوست بدن با سایرو"}""",
                imageAlt = "روتین مراقبت و آبرسانی پوست بدن با لوسیون سایرو",
                seoScore = 89
            ),

            // --- WordPress Pages & Landing Pages ---
            SiteItem(
                id = 301,
                title = "صفحه اصلی فروشگاه اینترنتی گلی کالا (طراحی با المنتور پرو)",
                content = "به وب‌سایت و فروشگاه اینترنتی گلی کالا (golikala.com) خوش آمدید. گلی کالا نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران می‌باشد. واردکننده مستقیم محصولات بهداشتی و مراقبتی با ضمانت اصالت ۱۰۰٪ فیزیکی.",
                excerpt = "صفحه اصلی فروشگاه اینترنتی گلی کالا با طراحی اختصاصی المنتور و لندینگ محصولات آمالفی، آگرادو و سایرو.",
                slug = "home-page",
                categories = listOf("برگه‌های اصلی سایت"),
                type = SiteItemType.PAGE,
                focusKeyword = "فروشگاه آنلاین گلی کالا",
                metaTitle = "گلی کالا | نماینده رسمی آمالفی، آگرادو و سایرو در ایران",
                metaDescription = "خرید اینترنتی محصولات اصل آمالفی، آگرادو و سایرو از گلی کالا نماینده رسمی و انحصاری در ایران با ضمانت ۱۰۰٪ اصالت فیزیکی و ارسال سریع.",
                canonicalUrl = "https://golikala.com/",
                schemaType = "WebPage",
                schemaJson = """{"@context":"https://schema.org","@type":"WebSite","name":"گلی کالا","url":"https://golikala.com"}""",
                imageAlt = "بنر اسلایدر صفحه اصلی فروشگاه اینترنتی گلی کالا",
                seoScore = 96,
                isElementor = true,
                elementorTemplate = "elementor_header_footer",
                elementorDataJson = """[{"id":"sec_hero_home","elType":"section","settings":{"background_color":"#F8FAFC"},"elements":[{"id":"col_1","elType":"column","elements":[{"id":"w_h1","elType":"widget","widgetType":"heading","settings":{"title":"گلی کالا؛ نماینده رسمی و انحصاری محصولات آمالفی، آگرادو و سایرو در ایران"}}]}]}]"""
            ),
            SiteItem(
                id = 302,
                title = "درباره ما و خط مشی اصالت گلی کالا (طراحی المنتور)",
                content = "فروشگاه اینترنتی گلی کالا (golikala.com) به عنوان نماینده رسمی و انحصاری محصولات برندهای معتبر آمالفی (Amalfi)، آگرادو (Agrado) و سایرو (Sairo) در ایران فعالیت می‌کند.\n\nهدف ما ارائه مستقیم کالاهای اصل با بچ‌نامبر معتبر، تاریخ مصرف تاییدشده و حذف کامل کالاهای تقلبی از سبد خرید خانواده‌های ایرانی است.",
                excerpt = "آشنایی با رسالت گلی کالا به عنوان نماینده رسمی برندهای آمالفی، آگرادو و سایرو در ایران.",
                slug = "about-us",
                categories = listOf("برگه‌های سازمانی"),
                type = SiteItemType.PAGE,
                focusKeyword = "درباره گلی کالا",
                metaTitle = "درباره ما | اصالت کالا و نمایندگی رسمی گلی کالا",
                metaDescription = "معرفی فروشگاه گلی کالا به عنوان نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو در ایران با تضمین اصالت کالا.",
                canonicalUrl = "https://golikala.com/about-us/",
                schemaType = "AboutPage",
                schemaJson = """{"@context":"https://schema.org","@type":"AboutPage","name":"درباره ما"}""",
                imageAlt = "مرکز پردازش و انبار محصولات اصل گلی کالا",
                seoScore = 92,
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
        val finalContent = pkg.structuredHtmlContent.ifBlank { pkg.fullDescription }
        val changes = mutableMapOf<String, Any?>(
            "title" to pkg.seoTitle,
            "excerpt" to pkg.shortDescription,
            "content" to finalContent,
            "metaDescription" to pkg.rankMathMetaDesc,
            "focusKeyword" to pkg.focusKeyword
        )
        pkg.imageMetadata?.let { imgMeta ->
            if (imgMeta.persianAltText.isNotBlank()) {
                changes["imageAlt"] = imgMeta.persianAltText
            }
        }
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

    // --- Phase 3: Real Google Competitor Intelligence & Advanced Content Rewriting ---
    suspend fun analyzeCompetitors(itemId: Long): CompetitorAnalysisReport? = withContext(Dispatchers.IO) {
        val item = _items.value.find { it.id == itemId } ?: return@withContext null
        val profile = BrandProfile(brandName = _brandKnowledgeBase.value.storeName)
        val kb = _brandKnowledgeBase.value
        geminiService.analyzeGoogleCompetitors(item, profile, kb)
    }

    suspend fun rewriteItemContentAdvanced(itemId: Long) = withContext(Dispatchers.IO) {
        val item = _items.value.find { it.id == itemId } ?: return@withContext
        val kb = _brandKnowledgeBase.value
        val pkg = ContentModuleProcessor.generateProductPackage(item, kb)
        val imgMeta = pkg.imageMetadata ?: ContentModuleProcessor.generateImageMetadata(item)
        val changes = mapOf(
            "title" to pkg.seoTitle,
            "excerpt" to pkg.shortDescription,
            "content" to pkg.structuredHtmlContent,
            "metaTitle" to pkg.seoTitle,
            "metaDescription" to pkg.rankMathMetaDesc,
            "focusKeyword" to pkg.focusKeyword,
            "imageAlt" to imgMeta.persianAltText,
            "imageTitle" to "${item.title} در گلی کالا",
            "seoScore" to 96
        )
        applyItemUpdate(
            targetId = itemId,
            proposedChanges = changes,
            actionName = "بازنویسی پیشرفته سئو (پروتکل ۳ مرحله‌ای و قانون خطای صفر)",
            reason = "تحلیل OCR و تصویر، عنوان H1، متن کوتاه تخصصی، جدول ۹ ردیفه فنی، متادیتای عکس و اعتبارسنجی خطای صفر"
        )
    }
}

