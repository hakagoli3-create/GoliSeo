package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val targetType: String,
    val targetId: Long,
    val targetTitle: String,
    val actionName: String,
    val reason: String,
    val authorRole: String,
    val status: String,
    val diffsJson: String, // Stored as serialized JSON string
    val priorSnapshotId: Long?,
    val isUndone: Boolean = false
)

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val targetType: String,
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

@Entity(tableName = "queue_tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetId: Long,
    val targetType: String,
    val targetTitle: String,
    val taskType: String,
    val status: String,
    val isDryRun: Boolean,
    val needsHumanReview: Boolean,
    val reviewApproved: Boolean,
    val retryCount: Int,
    val maxRetries: Int,
    val errorMessage: String?,
    val estimatedTokens: Int,
    val diffPreview: String?,
    val timestamp: Long
)

@Entity(tableName = "brand_memory")
data class BrandEntity(
    @PrimaryKey
    val id: Int = 1,
    val brandName: String,
    val toneOfVoice: String,
    val targetAudience: String,
    val forbiddenKeywords: String,
    val competitiveAdvantage: String
)
