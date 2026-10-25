package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity): Long

    @Query("UPDATE audit_logs SET isUndone = 1, status = 'UNDONE' WHERE id = :logId")
    suspend fun markLogUndone(logId: Long)

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}

@Dao
interface SnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: SnapshotEntity): Long

    @Query("SELECT * FROM snapshots WHERE id = :snapshotId LIMIT 1")
    suspend fun getSnapshotById(snapshotId: Long): SnapshotEntity?

    @Query("SELECT * FROM snapshots WHERE targetType = :type AND targetId = :targetId ORDER BY timestamp DESC")
    suspend fun getSnapshotsForTarget(type: String, targetId: Long): List<SnapshotEntity>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM queue_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE queue_tasks SET status = :newStatus WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, newStatus: String)

    @Query("UPDATE queue_tasks SET reviewApproved = 1, status = 'QUEUED' WHERE id = :taskId")
    suspend fun approveTask(taskId: Long)

    @Query("DELETE FROM queue_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("DELETE FROM queue_tasks WHERE status = 'COMPLETED'")
    suspend fun clearCompletedTasks()
}

@Dao
interface BrandDao {
    @Query("SELECT * FROM brand_memory WHERE id = 1 LIMIT 1")
    fun getBrandMemory(): Flow<BrandEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBrandMemory(brand: BrandEntity)
}
