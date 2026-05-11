package com.diagnostics.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Entity(tableName = "repairs")
data class RepairRecord(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val deviceModel: String,
    val deviceSerial: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val problem: String,
    val diagnosis: String,
    val componentsReplaced: String, // JSON
    val cost: Double = 0.0,
    val status: RepairStatus = RepairStatus.PENDING,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    enum class RepairStatus {
        PENDING, IN_PROGRESS, WAITING_PARTS, COMPLETED, CANCELLED
    }
}

@Dao
interface RepairDao {
    @Query("SELECT * FROM repairs ORDER BY createdAt DESC")
    fun getAllRepairs(): Flow<List<RepairRecord>>

    @Query("SELECT * FROM repairs WHERE status = :status ORDER BY createdAt DESC")
    fun getRepairsByStatus(status: RepairRecord.RepairStatus): Flow<List<RepairRecord>>

    @Query("SELECT * FROM repairs WHERE id = :id LIMIT 1")
    suspend fun getRepairById(id: String): RepairRecord?

    @Insert
    suspend fun insert(repair: RepairRecord)

    @Update
    suspend fun update(repair: RepairRecord)

    @Delete
    suspend fun delete(repair: RepairRecord)

    @Query("SELECT COUNT(*) FROM repairs WHERE status = 'COMPLETED'")
    suspend fun getCompletedCount(): Int

    @Query("SELECT SUM(cost) FROM repairs WHERE status = 'COMPLETED'")
    suspend fun getTotalRevenue(): Double?
}

@Database(entities = [RepairRecord::class], version = 1)
abstract class RepairDatabase : RoomDatabase() {
    abstract fun repairDao(): RepairDao
}
