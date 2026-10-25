package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BrandEntity
import com.example.data.local.LogEntity
import com.example.data.local.SnapshotEntity
import com.example.data.local.TaskEntity
import com.example.data.model.AuditLog
import com.example.data.model.AutopilotConfig
import com.example.data.model.BrandProfile
import com.example.data.model.BudgetTracker
import com.example.data.model.ConnectionTestResult
import com.example.data.model.DiffItem
import com.example.data.model.HardLockValidator
import com.example.data.model.LogStatus
import com.example.data.model.QueuedTask
import com.example.data.model.SecurityHardLockException
import com.example.data.model.SiteConfig
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.remote.GeminiSeoService
import com.example.data.remote.NetworkClientProvider
import com.example.data.remote.SeoAiResult
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

    init {
        loadInitialMockData()
    }

    private fun loadInitialMockData() {
        val initialItems = listOf(
            SiteItem(
                id = 101,
                title = "کرم آبرسان و مرطوب‌کننده هیالورونیک اسید",
                content = "کرم آبرسان عمقی پوست حاوی هیالورونیک اسید خالص و ویتامین B5 جهت حفظ شادابی و جوانی پوست صورت.",
                excerpt = "آبرسانی قوی و تغذیه‌کننده پوست با جذب فوق‌العاده سریع.",
                slug = "hyaluronic-acid-moisturizer",
                categories = listOf("مراقبت از پوست", "مرطوب‌کننده"),
                regularPrice = "۴۸۰٬۰۰۰ تومان",
                salePrice = "۳۹۰٬۰۰۰ تومان",
                type = SiteItemType.PRODUCT,
                focusKeyword = "کرم آبرسان هیالورونیک اسید",
                metaTitle = "خرید کرم آبرسان قوی هیالورونیک اسید | گلیسئو",
                metaDescription = "خرید اینترنتی کرم آبرسان و جوانساز هیالورونیک اسید با بالاترین کیفیت، ارسال فوری و ضمانت اصالت کالا در فروشگاه گلیسئو.",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"کرم آبرسان هیالورونیک اسید"}""",
                imageAlt = "بسته‌بندی کرم آبرسان هیالورونیک اسید",
                seoScore = 74
            ),
            SiteItem(
                id = 102,
                title = "سرم ویتامین C روشن‌کننده و ضد لک قوی",
                content = "سرم غلیظ حاوی ۲۰ درصد ویتامین C فعال برای رفع لک‌های پوستی، یکنواخت‌سازی رنگ چهره و افزایش کلاژن‌سازی طبیعی.",
                excerpt = "سرم غنی‌شده با آنتی‌اکسیدان قوی برای درخشش و شفافیت پوست.",
                slug = "vitamin-c-brightening-serum",
                categories = listOf("سرم پوست", "ضد لک"),
                regularPrice = "۶۵۰٬۰۰۰ تومان",
                salePrice = "",
                type = SiteItemType.PRODUCT,
                focusKeyword = "سرم ویتامین C ضد لک",
                metaTitle = "سرم ویتامین C ضد لک و روشن‌کننده پوست",
                metaDescription = "بهترین سرم ویتامین C خالص برای از بین بردن لک‌ها و روشن شدن پوست با مشاوره رایگان.",
                schemaJson = """{"@context":"https://schema.org","@type":"Product","name":"سرم ویتامین C"}""",
                imageAlt = "قطره‌چکان سرم ویتامین C روی پس‌زمینه شفاف",
                seoScore = 62
            ),
            SiteItem(
                id = 201,
                title = "راهنمای جامع روتین پوستی روز و شب برای انواع پوست",
                content = "داشتن یک روتین پوستی منظم اولین گام در سلامت چهره است. در این مقاله گام‌های پاکسازی، تونر، آبرسانی و ضد آفتاب را تشریح می‌کنیم.",
                excerpt = "چگونه بهترین روتین پوستی مناسب پوست خشک یا چرب داشته باشیم؟",
                slug = "daily-skincare-routine-guide",
                categories = listOf("مقالات آموزشی", "مراقبت پوست"),
                type = SiteItemType.POST,
                focusKeyword = "روتین پوستی روز و شب",
                metaTitle = "روتین پوستی اصولی چیست؟ راهنمای قدم به قدم",
                metaDescription = "آموزش کامل مراحل روتین پوستی روز و شب برای پوست‌های چرب، خشک و مختلط به همراه نکات مهم کارشناسان.",
                schemaJson = """{"@context":"https://schema.org","@type":"Article","headline":"راهنمای روتین پوستی"}""",
                imageAlt = "اینفوگرافیک مراحل روتین مراقبت پوستی",
                seoScore = 58
            ),
            SiteItem(
                id = 301,
                title = "درباره فروشگاه و خط مشی اصالت گلیسئو",
                content = "فروشگاه گلیسئو با هدف ارائه محصولات با اصالت صد در صدی و دسترسی مستقیم مصرف‌کنندگان ایرانی به برندهای معتبر جهانی پایه‌گذاری شده است.",
                excerpt = "آشنایی با گلیسئو و مأموریت ما در ارائه محصولات با اصالت.",
                slug = "about-us",
                categories = listOf("برگه‌های اصلی"),
                type = SiteItemType.PAGE,
                focusKeyword = "درباره گلیسئو",
                metaTitle = "درباره ما | اصالت کالا و تعهد فروشگاه گلیسئو",
                metaDescription = "داستان شکل‌گیری گلیسئو و تعهد ما به ارائه محصولات تضمین‌شده و پشتیبانی تخصصی.",
                schemaJson = """{"@context":"https://schema.org","@type":"AboutPage","name":"درباره ما"}""",
                imageAlt = "تیم گلیسئو و مرکز پردازش سفارش‌ها",
                seoScore = 80
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
            kotlinx.coroutines.delay(650)
            logs.add("پینگ سرور: ۴۲ میلی‌ثانیه")
            logs.add("پاسخ وردپرس REST API: وضعیت 200 OK")
            logs.add("احراز هویت رمز برنامه (Application Password): تایید شد")
            logs.add("پاسخ ووکامرس REST API v3: وضعیت 200 OK (Consumer Key معتبر)")
            logs.add("قفل سخت: لایه امنیتی گلیسئو فعال و فیلترهای قیمت، اسلاگ و دسته‌بندی استقرار یافتند")
            return@withContext ConnectionTestResult(
                isSuccess = true,
                wpConnected = true,
                wcConnected = true,
                latencyMs = 42,
                wpVersion = "WordPress 6.6.2",
                wcVersion = "WooCommerce 9.3.1",
                siteName = "فروشگاه آنلاین گلیسئو (اتصال آزمایشی فعال)",
                logs = logs
            )
        }

        try {
            val client = NetworkClientProvider.createOkHttpClient()
            val wpApi = NetworkClientProvider.createWordPressApi(config.siteUrl, client)
            val authHeader = NetworkClientProvider.createBasicAuth(config.wpUsername, config.wpAppPassword)

            logs.add("ارسال درخواست به WP REST API...")
            val wpResponse = wpApi.getSiteInfo(authHeader)
            val latency = System.currentTimeMillis() - startTime

            val wpOk = wpResponse.isSuccessful
            var siteTitle = config.siteUrl
            var wpVersion = "WordPress Core"

            if (wpOk) {
                val bodyStr = wpResponse.body()?.string() ?: ""
                try {
                    val root = JSONObject(bodyStr)
                    siteTitle = root.optString("name", siteTitle)
                    wpVersion = "WP ${root.optString("description", "Active")}"
                } catch (e: Exception) {
                    // Ignore parse error
                }
                logs.add("اتصال به وردپرس موفق بود (کد ${wpResponse.code()})")
            } else {
                logs.add("خطا در پاسخ وردپرس: کد ${wpResponse.code()}")
            }

            // Test WooCommerce
            var wcOk = false
            var wcVersion = "WooCommerce 9.x"
            try {
                val wcApi = NetworkClientProvider.createWooCommerceApi(config.siteUrl, client)
                val wcAuth = NetworkClientProvider.createBasicAuth(config.wcConsumerKey, config.wcConsumerSecret)
                val wcResponse = wcApi.getProducts(perPage = 1, authHeader = wcAuth)
                wcOk = wcResponse.isSuccessful
                logs.add("اتصال به ووکامرس: ${if (wcOk) "موفق" else "کد ${wcResponse.code()}"}")
            } catch (e: Exception) {
                logs.add("ووکامرس در دسترس نیست یا کلید اشتباه است: ${e.message}")
            }

            ConnectionTestResult(
                isSuccess = wpOk,
                wpConnected = wpOk,
                wcConnected = wcOk,
                latencyMs = latency,
                wpVersion = wpVersion,
                wcVersion = wcVersion,
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
}
