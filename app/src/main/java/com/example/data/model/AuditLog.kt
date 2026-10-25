package com.example.data.model

enum class LogStatus(val persianLabel: String) {
    SUCCESS("موفق"),
    BLOCKED_SECURITY("مسدود شده با قفل سخت"),
    FAILED("خطا"),
    UNDONE("بازگردانی شده (Undo)")
}

data class DiffItem(
    val fieldName: String,
    val beforeValue: String,
    val afterValue: String,
    val isLockedViolation: Boolean = false
)

data class AuditLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val targetType: SiteItemType,
    val targetId: Long,
    val targetTitle: String,
    val actionName: String,
    val reason: String,
    val authorRole: String, // مدیر یا ویرایشگر
    val status: LogStatus,
    val diffs: List<DiffItem>,
    val priorSnapshotId: Long? = null,
    val isUndone: Boolean = false
)

data class ContentSnapshot(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val targetType: SiteItemType,
    val targetId: Long,
    val title: String,
    val content: String,
    val excerpt: String,
    val metaTitle: String,
    val metaDescription: String,
    val schemaJson: String,
    val imageAlt: String,
    val reason: String
)
