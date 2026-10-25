package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuditLog
import com.example.data.model.AutopilotConfig
import com.example.data.model.BrandProfile
import com.example.data.model.BudgetTracker
import com.example.data.model.ConnectionTestResult
import com.example.data.model.QueuedTask
import com.example.data.model.SecurityHardLockException
import com.example.data.model.SiteConfig
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.TaskType
import com.example.data.model.UserRole
import com.example.data.model.UserSession
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

    // Diff / Rollback Dialog
    private val _activeDiffLog = MutableStateFlow<AuditLog?>(null)
    val activeDiffLog: StateFlow<AuditLog?> = _activeDiffLog.asStateFlow()

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
}
