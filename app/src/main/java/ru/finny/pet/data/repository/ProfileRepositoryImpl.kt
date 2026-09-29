package ru.finny.pet.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.finny.pet.data.local.FinnyDatabase
import ru.finny.pet.data.local.entity.SpendLogEntity
import ru.finny.pet.data.mapper.ProfileMapper
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.ExpenseCategory
import ru.finny.pet.domain.model.SpendRecord
import ru.finny.pet.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val db: FinnyDatabase
) : ProfileRepository {

    private val profileDao = db.profileDao()
    private val spendDao = db.spendLogDao()
    private val metaDao = db.appMetaDao()

    override fun observeProfile(): Flow<ChildProfile?> =
        profileDao.observeProfile().map { it?.let(ProfileMapper::toDomain) }

    override suspend fun getProfile(): ChildProfile? =
        profileDao.getProfile()?.let(ProfileMapper::toDomain)

    override suspend fun saveProfile(profile: ChildProfile) {
        val existing = profileDao.getProfile()
        profileDao.upsert(
            ProfileMapper.toEntity(
                profile,
                onboardingDone = existing?.onboardingDone ?: true,
                roleSelected = existing?.roleSelected ?: true
            )
        )
    }

    override suspend fun resetProfile() {
        profileDao.clear()
        spendDao.clear()
        metaDao.clear()
    }

    override suspend fun isOnboardingDone(): Boolean =
        profileDao.getProfile()?.onboardingDone == true

    override suspend fun setOnboardingDone(done: Boolean) {
        val p = profileDao.getProfile() ?: return
        profileDao.upsert(p.copy(onboardingDone = done, roleSelected = true))
    }

    override suspend fun logSpend(period: Int, title: String, amount: Int, category: ExpenseCategory) {
        spendDao.insert(
            SpendLogEntity(
                period = period,
                title = title,
                amount = amount,
                category = category.name,
                timestampMs = System.currentTimeMillis()
            )
        )
    }

    override fun observeSpends(): Flow<List<SpendRecord>> =
        spendDao.observeRecent().map { list ->
            list.map {
                SpendRecord(
                    period = it.period,
                    title = it.title,
                    amount = it.amount,
                    category = ExpenseCategory.valueOf(it.category)
                )
            }
        }

    override suspend fun getSpends(): List<SpendRecord> =
        spendDao.getRecent().map {
            SpendRecord(it.period, it.title, it.amount, ExpenseCategory.valueOf(it.category))
        }
}
