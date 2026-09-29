package ru.finny.pet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: Long = 1L,
    val childCode: String,
    val petName: String,
    val species: String,
    val stage: String,
    val stageReason: String,
    val balance: Int,
    val savingsBalance: Int,
    val periodIndex: Int,
    val weekPeriod: Int,
    val hunger: Int,
    val energy: Int,
    val joy: Int,
    val lastFedPeriod: Int,
    val lastEntertainmentPeriod: Int,
    val planMandatory: Int?,
    val planOptional: Int?,
    val planSavings: Int?,
    val factMandatory: Int?,
    val factOptional: Int?,
    val factSaved: Int?,
    val activeGoalId: String?,
    val wardrobeJson: String,
    val completedTaskIdsCsv: String,
    val completedLessonIdsCsv: String,
    val ownedShopItemIdsCsv: String,
    val dailyLoginClaimedPeriod: Int,
    val demoMode: Boolean,
    val manualSeason: String?,
    val soundEnabled: Boolean,
    val animationsEnabled: Boolean,
    val activeWorkId: String?,
    val completedWorkIdsCsv: String,
    val workEndsAtEpochMs: Long,
    val sleepEndsAtEpochMs: Long,
    val lastTickAtEpochMs: Long,
    val lastTaxAtEpochMs: Long,
    val lastDailyLoginAtEpochMs: Long,
    val isIll: Boolean,
    val illSinceMs: Long,
    val hungerZeroSinceMs: Long,
    val energyZeroSinceMs: Long,
    val joyZeroSinceMs: Long,
    val zeroSinceMs: Long,
    val createdAtEpochMs: Long,
    val onboardingDone: Boolean,
    val roleSelected: Boolean
)

@Entity(tableName = "spend_log")
data class SpendLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val period: Int,
    val title: String,
    val amount: Int,
    val category: String,
    val timestampMs: Long
)

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String
)
