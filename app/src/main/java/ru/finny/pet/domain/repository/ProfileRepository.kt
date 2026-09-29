package ru.finny.pet.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.ExpenseCategory
import ru.finny.pet.domain.model.SpendRecord

interface ProfileRepository {
    fun observeProfile(): Flow<ChildProfile?>
    suspend fun getProfile(): ChildProfile?
    suspend fun saveProfile(profile: ChildProfile)
    suspend fun resetProfile()
    suspend fun isOnboardingDone(): Boolean
    suspend fun setOnboardingDone(done: Boolean)
    suspend fun logSpend(period: Int, title: String, amount: Int, category: ExpenseCategory)
    fun observeSpends(): Flow<List<SpendRecord>>
    suspend fun getSpends(): List<SpendRecord>
}
