package com.diagnostics.data.database

import android.content.Context
import androidx.room.*
import com.diagnostics.model.ComponentInfo
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val partNumber: String,
    val compatibleModels: String, // JSON array
    val description: String,
    val commonFaults: String, // JSON array
    val replacementDifficulty: String, // EASY, MEDIUM, HARD
    val averagePrice: Double,
    val alternatives: String, // JSON array
    val datasheetUrl: String,
    val imageUrl: String,
    val isActive: Boolean = true
)

@Dao
interface ComponentDao {
    @Query("SELECT * FROM components WHERE isActive = 1")
    fun getAllComponents(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE partNumber = :partNumber LIMIT 1")
    suspend fun getComponentByPartNumber(partNumber: String): ComponentEntity?

    @Query("SELECT * FROM components WHERE type = :type")
    fun getComponentsByType(type: String): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE compatibleModels LIKE '%' || :model || '%'")
    fun getComponentsForModel(model: String): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components WHERE name LIKE '%' || :query || '%' OR partNumber LIKE '%' || :query || '%'")
    fun searchComponents(query: String): Flow<List<ComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(components: List<ComponentEntity>)

    @Query("SELECT COUNT(*) FROM components")
    suspend fun getCount(): Int
}

@Database(entities = [ComponentEntity::class], version = 1)
abstract class ComponentDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao

    companion object {
        @Volatile
        private var INSTANCE: ComponentDatabase? = null

        fun getInstance(context: Context): ComponentDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ComponentDatabase::class.java,
                    "components.db"
                )
                .createFromAsset("database/components.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
