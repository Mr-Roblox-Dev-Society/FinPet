package ru.finny.pet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.finny.pet.data.local.dao.AppMetaDao
import ru.finny.pet.data.local.dao.ProfileDao
import ru.finny.pet.data.local.dao.SpendLogDao
import ru.finny.pet.data.local.entity.AppMetaEntity
import ru.finny.pet.data.local.entity.ProfileEntity
import ru.finny.pet.data.local.entity.SpendLogEntity

@Database(
    entities = [ProfileEntity::class, SpendLogEntity::class, AppMetaEntity::class],
    version = 4,
    exportSchema = false
)
abstract class FinnyDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun spendLogDao(): SpendLogDao
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        @Volatile private var instance: FinnyDatabase? = null

        fun get(context: Context): FinnyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinnyDatabase::class.java,
                    "finny_pet.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
