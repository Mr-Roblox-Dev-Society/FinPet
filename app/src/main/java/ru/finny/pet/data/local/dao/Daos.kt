package ru.finny.pet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.finny.pet.data.local.entity.AppMetaEntity
import ru.finny.pet.data.local.entity.ProfileEntity
import ru.finny.pet.data.local.entity.SpendLogEntity

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clear()
}

@Dao
interface SpendLogDao {
    @Insert
    suspend fun insert(item: SpendLogEntity)

    @Query("SELECT * FROM spend_log ORDER BY id DESC LIMIT 50")
    fun observeRecent(): Flow<List<SpendLogEntity>>

    @Query("SELECT * FROM spend_log ORDER BY id DESC LIMIT 50")
    suspend fun getRecent(): List<SpendLogEntity>

    @Query("DELETE FROM spend_log")
    suspend fun clear()
}

@Dao
interface AppMetaDao {
    @Query("SELECT value FROM app_meta WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: AppMetaEntity)

    @Query("DELETE FROM app_meta")
    suspend fun clear()
}
