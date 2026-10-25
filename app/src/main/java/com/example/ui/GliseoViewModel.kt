package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.remote.SeoAiResult
import com.example.data.repository.GliseoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavDestination(val persianLabel: String) {
    DASHBOARD("داشبورد"),
    CONTENT_STUDIO("محتوا و سئو"),
    QUEUE_AUTOPILOT("صف و خلبان خودکار"),
    LOGS_SNAPSHOTS("لاگ و نسخه‌ها"),
    SETTINGS_SECURITY("تنظیمات و اتصال")
}

data class UiMessage(
    val text: String,
    val isError: Boolean = false,
    val isHardLockWarning: Boolean = false
)

class GliseoViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GliseoRepository(application)

    // Navigation
    private val _currentNav = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentNav: StateFlow<AppNavDestination> = _currentNav.asStateFlow()

    // Site Data
    val items: StateFlow<List<SiteItem>> = repository.items
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val queueTasks: StateFlow<List<QueuedTask>> = repository.queueTasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val brandProfile: StateFlow<BrandProfile> = repository.brandProfile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BrandProfile()
    )

    val siteConfig: StateFlow<SiteConfig> = repository.siteConfig
    val userSession: StateFlow<UserSession> = repository.userSession
    val autopilotConfig: StateFlow<AutopilotConfig> = repository.autopilotConfig
    val budgetTracker: StateFlow<BudgetTracker> = repository.budgetTracker
    val circuitBreakerTriggered: StateFlow<Boolean> = repository.circuitBreakerTriggered

    // UI State for Editor / Optimizer
    private val _selectedItem = MutableStateFlow<SiteItem?>(null)
    val selectedItem: StateFlow<SiteItem?> = _selectedItem.asStateFlow()

    private val _aiOptimizing = MutableStateFlow(false)
    val aiOptimizing: StateFlow<Boolean> = _aiOptimizing.asStateFlow()

    private val _aiResult = MutableStateFlow<SeoAiResult?>(null)
    val aiResult: StateFlow<SeoAiResult?> = _aiResult.asStateFlow()

    // Dry Run Preview
    private val _dryRunPreviewItem = MutableStateFlow<SiteItem?>(null)
    val dryRunPreviewItem: StateFlow<SiteItem?> = _dryRunPreviewItem.asStateFlow()

    // Connection Test State
    private val _testingConnection = MutableStateFlow(false)
    val testingConnection: StateFlow<Boolean> = _testingConnection.asStateFlow()

    private val _connectionResult = MutableStateFlow<ConnectionTestResult?>(null)
    val connectionResult: StateFlow<ConnectionTestResult?> = _connectionResult.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    val syncStatusMessage: StateFlow<String> = repository.syncStatusMessage

    // Diff / Rollback Dialog
    private val _activeDiffLog = MutableStateFlow<AuditLog?>(null)
    val activeDiffLog: StateFlow<AuditLog?> = _activeDiffLog.asStateFlow()

    // Phase 2 StateFlows
    val brandKnowledgeBase: StateFlow<BrandKnowledgeBase> = repository.brandKnowledgeBase
    val scheduledPlans: StateFlow<List<ScheduledPostPlan>> = repository.scheduledPlans

    private val _freeformPlan = MutableStateFlow<FreeformCommandPlan?>(null)
    val freeformPlan: StateFlow<FreeformCommandPlan?> = _freeformPlan.asStateFlow()

    private val _activeValidationResult = MutableStateFlow<ValidationCheckResult?>(null)
    val activeValidationResult: StateFlow<ValidationCheckResult?> = _activeValidationResult.asStateFlow()

    private val _productStudioPackage = MutableStateFlow<ProductOptimizationPackage?>(null)
    val productStudioPackage: StateFlow<ProductOptimizationPackage?> = _productStudioPackage.asStateFlow()

    private val _topicSuggestions = MutableStateFlow<List<ArticleTopicSuggestion>>(repository.getArticleTopicSuggestions())
    val topicSuggestions: StateFlow<List<ArticleTopicSuggestion>> = _topicSuggestions.asStateFlow()

    private val _generatedArticlePreview = MutableStateFlow<GeneratedArticle?>(null)
    val generatedArticlePreview: StateFlow<GeneratedArticle?> = _generatedArticlePreview.asStateFlow()

    // Competitor Analysis State
    private val _activeCompetitorReport = MutableStateFlow<CompetitorAnalysisReport?>(null)
    val activeCompetitorReport: StateFlow<CompetitorAnalysisReport?> = _activeCompetitorReport.asStateFlow()

    private val _analyzingCompetitors = MutableStateFlow(false)
    val analyzingCompetitors: StateFlow<Boolean> = _analyzingCompetitors.asStateFlow()

    // Transient UI Messages
    private val _uiMessages = MutableSharedFlow<UiMessage>()
    val uiMessages = _uiMessages.asSharedFlow()


    fun navigateTo(destination: AppNavDestination) {
        _currentNav.value = destination
    }

    fun selectItem(item: SiteItem?) {
        _selectedItem.value = item
        _aiResult.value = null
        _dryRunPreviewItem.value = null
    }

    // --- Connection Testing ---
    fun runConnectionTest() {
        viewModelScope.launch {
            _testingConnection.value = true
            val result = repository.testConnection(siteConfig.value)
            _connectionResult.value = result
            _testingConnection.value = false
            if (result.isSuccess) {
                _uiMessages.emit(UiMessage("اتصال به وردپرس و ووکامرس با موفقیت برقرار شد."))
            } else {
                _uiMessages.emit(UiMessage("خطا در برقراری اتصال با سایت. گزارش را بررسی نمایید.", isError = true))
            }
        }
    }

    // --- AI SEO Generation ---
    fun generateSeoWithGemini(customPrompt: String? = null) {
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            _aiOptimizing.value = true
            val result = repository.generateSeoForCurrentItem(item.id, customPrompt)
            _aiResult.value = result
            _aiOptimizing.value = false
            if (result != null) {
                _uiMessages.emit(UiMessage("پیشنهادات هوشمند سئو با موفقیت بر اساس ترندهای گوگل تولید شد."))
            } else {
                _uiMessages.emit(UiMessage("خطا در ارتباط با هوش مصنوعی سئو.", isError = true))
            }
        }
    }

    // --- Dry Run (اجرای آزمایشی) ---
    fun runDryRunSimulation(changes: Map<String, Any?>) {
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            val result = repository.applyItemUpdate(
                targetId = item.id,
                proposedChanges = changes,
                actionName = "اجرای آزمایشی (Dry Run)",
                reason = "شبیه‌سازی اثر تغییرات بدون دستکاری داده‌های زنده",
                isDryRun = true
            )
            result.onSuccess { preview ->
                _dryRunPreviewItem.value = preview
                _uiMessages.emit(UiMessage("شبیه‌سازی با موفقیت انجام شد. پیش‌نمایش در دسترس است."))
            }.onFailure { ex ->
                if (ex is SecurityHardLockException) {
                    _uiMessages.emit(
                        UiMessage(
                            text = "🛡️ قفل سخت فعال شد: ${ex.message}",
                            isError = true,
                            isHardLockWarning = true
                        )
                    )
                } else {
                    _uiMessages.emit(UiMessage(ex.message ?: "خطا در شبیه‌سازی", isError = true))
                }
            }
        }
    }

    fun dismissDryRun() {
        _dryRunPreviewItem.value = null
    }

    // --- Direct Update (with Inviolable Hard Lock) ---
    fun commitItemUpdate(changes: Map<String, Any?>, reason: String) {
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            val result = repository.applyItemUpdate(
                targetId = item.id,
                proposedChanges = changes,
                actionName = "ویرایش و بهینه‌سازی محتوا",
                reason = reason
            )
            result.onSuccess { updated ->
                _selectedItem.value = updated
                _dryRunPreviewItem.value = null
                _aiResult.value = null
                _uiMessages.emit(UiMessage("تغییرات با موفقیت ذخیره و نسخه پشتیبان برای بازگردانی ثبت شد."))
            }.onFailure { ex ->
                if (ex is SecurityHardLockException) {
                    _uiMessages.emit(
                        UiMessage(
                            text = "⛔ قفل سخت گلیسئو: ${ex.message}",
                            isError = true,
                            isHardLockWarning = true
                        )
                    )
                } else {
                    _uiMessages.emit(UiMessage("خطا در ذخیره تغییرات: ${ex.message}", isError = true))
                }
            }
        }
    }

    // --- Intentional Hard Lock Test (Demonstrating Inviolability) ---
    fun testAttemptHardLockViolation() {
        val item = _selectedItem.value ?: items.value.firstOrNull() ?: return
        viewModelScope.launch {
            // Deliberately attempt to change price & slug to verify inviolable lock
            val forbiddenPayload = mapOf(
                "regular_price" to "120000",
                "slug" to "hacked-slug-url"
            )
            val result = repository.applyItemUpdate(
                targetId = item.id,
                proposedChanges = forbiddenPayload,
                actionName = "آزمون نفوذ قفل سخت",
                reason = "تست خط‌مشی امنیتی مسدودسازی قیمت و اسلاگ"
            )
            result.onFailure { ex ->
                _uiMessages.emit(
                    UiMessage(
                        text = "🔒 قفل سخت غیرقابل دورزدن با موفقیت مانع تغییر قیمت و اسلاگ شد!",
                        isError = false,
                        isHardLockWarning = true
                    )
                )
            }
        }
    }

    // --- Snapshots & Undo ---
    fun viewDiffLog(log: AuditLog?) {
        _activeDiffLog.value = log
    }

    fun undoSnapshot(log: AuditLog) {
        val snapId = log.priorSnapshotId ?: return
        viewModelScope.launch {
            val success = repository.undoLog(log.id, snapId)
            if (success) {
                _activeDiffLog.value = null
                _uiMessages.emit(UiMessage("نسخه با موفقیت به حالت قبل بازگردانی شد (Undo)."))
            } else {
                _uiMessages.emit(UiMessage("خطا در بازگردانی نسخه.", isError = true))
            }
        }
    }

    // --- Task Queue & 10% Review ---
    fun enqueueTask(targetId: Long, taskType: TaskType) {
        viewModelScope.launch {
            repository.queueTask(targetId, taskType)
            _uiMessages.emit(UiMessage("کار با موفقیت در صف پردازش قرار گرفت."))
        }
    }

    fun approveHumanReview(taskId: Long) {
        viewModelScope.launch {
            repository.approveAndRunTask(taskId)
            _uiMessages.emit(UiMessage("کار بازبینی تایید و جهت اجرای نهایی ارسال شد."))
        }
    }

    fun rejectHumanReview(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _uiMessages.emit(UiMessage("کار نمونه‌برداری ۱۰٪ رد و از صف خارج گردید."))
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
            _uiMessages.emit(UiMessage("کارهای تکمیل‌شده پاکسازی شدند."))
        }
    }

    // --- Autopilot & Circuit Breaker ---
    fun updateAutopilot(config: AutopilotConfig) {
        repository.updateAutopilotConfig(config)
    }

    fun emergencyStop() {
        repository.emergencyStopAutopilot()
        viewModelScope.launch {
            _uiMessages.emit(
                UiMessage(
                    "توقف اضطراری فعال شد! خلبان خودکار متوقف و تمام فرآیندها قفل شدند.",
                    isError = true
                )
            )
        }
    }

    fun resetCircuitBreaker() {
        repository.resetCircuitBreaker()
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("سیستم رفع توقف شد و به وضعیت آماده به کار بازگشت."))
        }
    }

    // --- Settings & Brand Memory ---
    fun saveBrandProfile(brand: BrandProfile) {
        viewModelScope.launch {
            repository.saveBrandProfile(brand)
            _uiMessages.emit(UiMessage("حافظه و مشخصات برند با موفقیت ذخیره شد."))
        }
    }

    fun updateSiteConfig(config: SiteConfig) {
        repository.updateSiteConfig(config)
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("تنظیمات اتصال به‌روزرسانی شد."))
        }
    }

    fun switchRole(role: UserRole) {
        repository.switchUserRole(role)
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("سطح دسترسی به «${role.persianLabel}» تغییر یافت."))
        }
    }

    fun verifyTwoFactorCode(code: String): Boolean {
        return repository.verifyTwoFactor(code)
    }

    // --- Phase 2: Natural Language Freeform Command ---
    fun executeFreeform(prompt: String, isDryRun: Boolean) {
        viewModelScope.launch {
            val plan = repository.executeFreeformCommand(prompt, isDryRun)
            _freeformPlan.value = plan
            if (!plan.isSafeFromHardLock) {
                _uiMessages.emit(
                    UiMessage(
                        text = "🛡️ قفل سخت امنیتی: ${plan.blockedReason}",
                        isError = true,
                        isHardLockWarning = true
                    )
                )
            } else {
                _uiMessages.emit(UiMessage("برنامه اجرایی دستور آزاد با موفقیت ایجاد شد."))
            }
        }
    }

    fun dismissFreeformPlan() {
        _freeformPlan.value = null
    }

    // --- Phase 2: Product Studio & Rank Math ---
    fun prepareProductPackage(productId: Long) {
        val pkg = repository.generateProductStudioPackage(productId)
        _productStudioPackage.value = pkg
        if (pkg != null) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage("پکیج سئو Rank Math، توضیحات و FAQ محصول آماده شد."))
            }
        }
    }

    fun applyProductPackage(productId: Long) {
        val pkg = _productStudioPackage.value ?: return
        viewModelScope.launch {
            repository.applyProductStudioPackage(productId, pkg)
            _productStudioPackage.value = null
            _uiMessages.emit(UiMessage("پکیج بهینه‌سازی محصول با موفقیت ذخیره و در نسخه ثبت شد."))
        }
    }

    fun dismissProductPackage() {
        _productStudioPackage.value = null
    }

    fun batchOptimizeProducts() {
        viewModelScope.launch {
            repository.batchOptimizeAllProducts()
            _uiMessages.emit(UiMessage("تمام محصولات برای بهینه‌سازی دسته‌ای در صف قرار گرفتند."))
        }
    }

    // --- Phase 2: Article Studio & E-E-A-T ---
    fun generateArticleFromTopic(topic: ArticleTopicSuggestion) {
        viewModelScope.launch {
            val article = repository.generateFullArticleFromTopic(topic)
            _generatedArticlePreview.value = article
            _uiMessages.emit(UiMessage("مقاله کامل بر اساس اصول E-E-A-T و ساختار H1-H3 آماده شد."))
        }
    }

    fun publishGeneratedArticle(article: GeneratedArticle) {
        viewModelScope.launch {
            repository.publishNewArticle(article)
            _generatedArticlePreview.value = null
            _uiMessages.emit(UiMessage("مقاله با موفقیت ایجاد، اعتبارسنجی و منتشر شد."))
        }
    }

    fun dismissArticlePreview() {
        _generatedArticlePreview.value = null
    }

    // --- Phase 2: Scheduling & Jalali Calendar ---
    fun schedulePostFuture(targetId: Long, daysInFuture: Int, occasion: String? = null) {
        repository.schedulePostFuture(targetId, daysInFuture, occasion)
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("محتوا با موفقیت با وضعیت future در وردپرس زمان‌بندی شد."))
        }
    }

    // --- Phase 2: Pre-Publish Validation ---
    fun validatePrePublish(item: SiteItem) {
        val result = repository.validateItemPrePublish(item)
        _activeValidationResult.value = result
    }

    fun dismissValidationResult() {
        _activeValidationResult.value = null
    }

    // --- Phase 2: Continuous Learning from User Edits ---
    fun learnUserEdit(original: String, approved: String, category: String) {
        repository.learnFromUserEdit(original, approved, category)
        viewModelScope.launch {
            _uiMessages.emit(UiMessage("الگوی ویرایش شما ذخیره شد و در حافظه یادگیری گلی کالا قرار گرفت."))
        }
    }

    fun syncWithSite() {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.syncSiteContent(siteConfig.value)
            _isSyncing.value = false
            res.onSuccess { count ->
                _uiMessages.emit(UiMessage("همگام‌سازی کامل شد: $count آیتم از سایت بارگذاری شدند."))
            }.onFailure { ex ->
                _uiMessages.emit(UiMessage("خطا در همگام‌سازی با سایت: ${ex.message}", isError = true))
            }
        }
    }

    fun generateCustomArticle(title: String, focusKeyword: String, instructions: String? = null) {
        viewModelScope.launch {
            _aiOptimizing.value = true
            val article = repository.generateCustomArticle(title, focusKeyword, instructions)
            _generatedArticlePreview.value = article
            _aiOptimizing.value = false
            _uiMessages.emit(UiMessage("مقاله تخصصی با جمینای تولید شد و آماده انتشار/زمان‌بندی است."))
        }
    }

    fun publishGeneratedArticleWithStatus(article: GeneratedArticle, targetStatus: String = "publish") {
        viewModelScope.launch {
            repository.publishNewArticle(article, targetStatus)
            _generatedArticlePreview.value = null
            val statusMsg = if (targetStatus == "future") "زمان‌بندی شده در وردپرس" else "منتشر شد"
            _uiMessages.emit(UiMessage("مقاله با موفقیت ایجاد، اعتبارسنجی و $statusMsg."))
        }
    }

    fun addNewContent(title: String, content: String, type: SiteItemType, focusKeyword: String, excerpt: String = "", regularPrice: String = "") {
        viewModelScope.launch {
            repository.addNewSiteItem(title, content, type, focusKeyword, excerpt, regularPrice)
            _uiMessages.emit(UiMessage("${type.persianLabel} جدید با موفقیت ایجاد و اعتبارسنجی شد."))
        }
    }

    fun applyElementorDesign(item: SiteItem, design: ElementorPageDesign) {
        viewModelScope.launch {
            repository.applyElementorDesignToPage(item.id, design)
            _uiMessages.emit(UiMessage("طراحی قالب المنتور بر روی «${item.title}» با موفقیت اعمال شد."))
        }
    }

    fun createNewElementorPage(design: ElementorPageDesign) {
        viewModelScope.launch {
            repository.createNewElementorPage(design)
            _uiMessages.emit(UiMessage("برگه جدید المنتور با عنوان «${design.title}» در وردپرس ایجاد شد."))
        }
    }

    fun autoOptimizeFullItem(item: SiteItem) {
        viewModelScope.launch {
            _aiOptimizing.value = true
            val aiResult = repository.generateSeoForCurrentItem(item.id)
            _aiOptimizing.value = false
            if (aiResult != null) {
                val changes = mutableMapOf<String, Any?>(
                    "metaTitle" to aiResult.suggestedTitle,
                    "metaDescription" to aiResult.suggestedMetaDescription,
                    "focusKeyword" to aiResult.suggestedFocusKeyword,
                    "schemaJson" to aiResult.suggestedSchemaJson,
                    "imageAlt" to aiResult.suggestedImageAlt,
                    "seoScore" to aiResult.seoScoreImprovement
                )
                if (aiResult.suggestedTitle.isNotBlank()) {
                    changes["title"] = aiResult.suggestedTitle
                }
                if (aiResult.suggestedShortDescription.isNotBlank()) {
                    changes["excerpt"] = aiResult.suggestedShortDescription
                }
                if (aiResult.suggestedStructuredContent.isNotBlank()) {
                    changes["content"] = aiResult.suggestedStructuredContent
                }
                commitItemUpdate(changes, "بهینه‌سازی کامل خودکار با هوش مصنوعی و سئو Rank Math")
                _uiMessages.emit(UiMessage("کل اطلاعات، محتوا و سئوی «${item.title}» هوشمندانه بازنویسی و بهینه‌سازی شد."))
            } else {
                _uiMessages.emit(UiMessage("خطا در بهینه‌سازی با هوش مصنوعی.", isError = true))
            }
        }
    }

    fun batchOptimizeTabItems(type: SiteItemType?) {
        viewModelScope.launch {
            val targets = if (type == null) items.value else items.value.filter { it.type == type }
            for (it in targets) {
                repository.queueTask(it.id, TaskType.SEO_META)
            }
            val label = type?.persianLabel ?: "تمام آیتم‌ها"
            _uiMessages.emit(UiMessage("تمام $label (${targets.size} مورد) برای بهینه‌سازی خودکار در صف قرار گرفتند."))
        }
    }

    fun analyzeCompetitors(item: SiteItem) {
        viewModelScope.launch {
            _analyzingCompetitors.value = true
            val report = repository.analyzeCompetitors(item.id)
            _analyzingCompetitors.value = false
            _activeCompetitorReport.value = report
            if (report != null) {
                _uiMessages.emit(UiMessage("تحلیل عمیق رقبا در گوگل برای «${item.title}» استخراج شد."))
            } else {
                _uiMessages.emit(UiMessage("خطا در تحلیل رقبا در گوگل.", isError = true))
            }
        }
    }

    fun dismissCompetitorReport() {
        _activeCompetitorReport.value = null
    }

    fun rewriteItemContentAdvanced(item: SiteItem) {
        viewModelScope.launch {
            repository.rewriteItemContentAdvanced(item.id)
            _uiMessages.emit(UiMessage("محتوای «${item.title}» با حذف دیوار متنی، افزودن جدول، بولت‌پوینت‌ها و لینک‌های داخلی بازنویسی شد."))
        }
    }

    fun applyWinningCompetitorStrategy(item: SiteItem, report: CompetitorAnalysisReport) {
        viewModelScope.launch {
            repository.rewriteItemContentAdvanced(item.id)
            val changes = mapOf(
                "focusKeyword" to report.targetKeyword,
                "metaTitle" to "${report.targetKeyword} اصل | گارانتی اصالت گلی کالا".take(60),
                "seoScore" to 95
            )
            repository.applyItemUpdate(
                targetId = item.id,
                proposedChanges = changes,
                actionName = "اعمال استراتژی رقبا",
                reason = "اعمال استراتژی پیروزی بر رقبای گوگل"
            )
            _activeCompetitorReport.value = null
            _uiMessages.emit(UiMessage("استراتژی پیروزی بر رقبای گوگل بر روی «${item.title}» اعمال گردید."))
        }
    }

    fun normalizePersianText(text: String): String {
        return repository.normalizePersianText(text)
    }
}

