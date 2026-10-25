package com.example.data.model

/**
 * Hard lock security exception.
 * Thrown whenever an operation attempts to bypass strict safety invariants:
 * - regular_price
 * - sale_price / price
 * - slug / url / permalink
 * - categories / category_ids
 */
class SecurityHardLockException(
    val blockedField: String,
    val attemptedValue: String?,
    override val message: String = "تغییر فیلد «$blockedField» بر اساس خط مشی قفل سخت امنیتی گلیسئو مسدود است."
) : SecurityException(message)

object HardLockValidator {
    val LOCKED_FIELDS = listOf(
        "regular_price",
        "sale_price",
        "price",
        "slug",
        "link",
        "permalink",
        "categories",
        "category_ids",
        "post_category"
    )

    fun isFieldLocked(fieldName: String): Boolean {
        val normalized = fieldName.trim().lowercase()
        return LOCKED_FIELDS.any { locked ->
            normalized == locked || normalized.contains(locked)
        }
    }

    /**
     * Inspects a key-value delta and throws [SecurityHardLockException]
     * if any locked field is attempted to be changed.
     */
    fun validateModifications(changes: Map<String, Any?>) {
        for ((key, value) in changes) {
            if (isFieldLocked(key)) {
                throw SecurityHardLockException(
                    blockedField = key,
                    attemptedValue = value?.toString()
                )
            }
        }
    }

    /**
     * Inspects a JSON or raw payload string for attempted mutations of locked fields.
     */
    fun inspectRawPayload(payload: String): List<String> {
        val violations = mutableListOf<String>()
        val lower = payload.lowercase()
        for (field in LOCKED_FIELDS) {
            val keyPattern = "\"$field\""
            if (lower.contains(keyPattern)) {
                violations.add(field)
            }
        }
        return violations
    }
}
