package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {
    @Query("SELECT * FROM farms ORDER BY createdAt DESC")
    fun getAllFarms(): Flow<List<FarmEntity>>

    @Query("SELECT * FROM farms WHERE id = :farmId LIMIT 1")
    suspend fun getFarmById(farmId: String): FarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: FarmEntity)

    @Query("SELECT * FROM log_entries WHERE farmId = :farmId ORDER BY recordedAt DESC")
    fun getLogsForFarm(farmId: String): Flow<List<LogEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(entry: LogEntryEntity)

    @Query("SELECT * FROM observations WHERE farmId = :farmId ORDER BY recordedAt DESC LIMIT 10")
    fun getRecentObservations(farmId: String): Flow<List<ObservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: ObservationEntity)
}

@Database(
    entities = [FarmEntity::class, LogEntryEntity::class, ObservationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HarvestDatabase : RoomDatabase() {
    abstract fun harvestDao(): HarvestDao
}
