package com.example.data.model

enum class TaskStatus(val persianLabel: String) {
    QUEUED("در صف انتظار"),
    RUNNING("در حال پردازش"),
    COMPLETED("تکمیل شده"),
    FAILED("خطا در اجرا"),
    PAUSED("متوقف شده"),
    NEEDS_REVIEW("نیازمند بازبینی انسانی (نمونه‌برداری ۱۰٪)")
}

enum class TaskType(val persianLabel: String) {
    SEO_META("بهینه‌سازی متادیتا و عنوان"),
    CONTENT_POLISH("بهبود و غناسازی متن"),
    SCHEMA_GENERATION("تولید اسکیما استاندارده"),
    ALT_TAGS("نگارش تگ آلت تصاویر"),
    BATCH_AUDIT("ممیزی جامع سئو")
}

data class QueuedTask(
    val id: Long = 0,
    val targetId: Long,
    val targetType: SiteItemType,
    val targetTitle: String,
    val taskType: TaskType,
    val status: TaskStatus = TaskStatus.QUEUED,
    val isDryRun: Boolean = false,
    val needsHumanReview: Boolean = false, // 10% sampling flag
    val reviewApproved: Boolean = false,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val errorMessage: String? = null,
    val estimatedTokens: Int = 850,
    val diffPreview: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutopilotConfig(
    val isMasterActive: Boolean = true,
    val isSeoMetaActive: Boolean = true,
    val isContentPolishActive: Boolean = false,
    val isSchemaActive: Boolean = true,
    val isAltTagsActive: Boolean = true,
    val samplingRate: Float = 0.10f // 10% human-in-the-loop review
)
