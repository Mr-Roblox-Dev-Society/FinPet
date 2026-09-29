package ru.finny.pet.data.mapper

import org.json.JSONArray
import org.json.JSONObject
import ru.finny.pet.data.local.entity.ProfileEntity
import ru.finny.pet.domain.model.BudgetPlan
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.OwnedClothingVariant
import ru.finny.pet.domain.model.PeriodFact
import ru.finny.pet.domain.model.PetSpecies
import ru.finny.pet.domain.model.PetStage
import ru.finny.pet.domain.model.PetStats
import ru.finny.pet.domain.model.Season

object ProfileMapper {

    fun toDomain(e: ProfileEntity): ChildProfile = ChildProfile(
        id = e.id,
        childCode = e.childCode,
        petName = e.petName,
        species = PetSpecies.valueOf(e.species),
        stage = PetStage.valueOf(e.stage),
        stageReason = e.stageReason,
        balance = e.balance,
        savingsBalance = e.savingsBalance,
        periodIndex = e.periodIndex,
        weekPeriod = e.weekPeriod,
        stats = PetStats(
            hunger = e.hunger,
            energy = e.energy,
            joy = e.joy,
            isIll = e.isIll,
            illSinceMs = e.illSinceMs,
            hungerZeroSinceMs = e.hungerZeroSinceMs,
            energyZeroSinceMs = e.energyZeroSinceMs,
            joyZeroSinceMs = e.joyZeroSinceMs,
            zeroSinceMs = e.zeroSinceMs,
            lastFedPeriod = e.lastFedPeriod,
            lastEntertainmentPeriod = e.lastEntertainmentPeriod
        ),
        currentPlan = if (e.planMandatory != null && e.planOptional != null && e.planSavings != null) {
            BudgetPlan(e.planMandatory, e.planOptional, e.planSavings)
        } else null,
        lastFact = if (e.factMandatory != null && e.factOptional != null && e.factSaved != null) {
            PeriodFact(e.factMandatory, e.factOptional, e.factSaved)
        } else null,
        activeGoalId = e.activeGoalId,
        wardrobe = parseWardrobe(e.wardrobeJson),
        completedTaskIds = e.completedTaskIdsCsv.split(',').filter { it.isNotBlank() }.toSet(),
        completedLessonIds = e.completedLessonIdsCsv.split(',').filter { it.isNotBlank() }.toSet(),
        ownedShopItemIds = e.ownedShopItemIdsCsv.split(',').filter { it.isNotBlank() }.toSet(),
        dailyLoginClaimedPeriod = e.dailyLoginClaimedPeriod,
        demoMode = e.demoMode,
        manualSeason = e.manualSeason?.let { Season.valueOf(it) },
        soundEnabled = e.soundEnabled,
        animationsEnabled = e.animationsEnabled,
        activeWorkId = e.activeWorkId,
        completedWorkIds = e.completedWorkIdsCsv.split(',').filter { it.isNotBlank() }.toSet(),
        workEndsAtEpochMs = e.workEndsAtEpochMs,
        sleepEndsAtEpochMs = e.sleepEndsAtEpochMs,
        lastTickAtEpochMs = e.lastTickAtEpochMs,
        lastTaxAtEpochMs = e.lastTaxAtEpochMs,
        lastDailyLoginAtEpochMs = e.lastDailyLoginAtEpochMs,
        createdAtEpochMs = e.createdAtEpochMs
    )

    fun toEntity(p: ChildProfile, onboardingDone: Boolean = true, roleSelected: Boolean = true): ProfileEntity =
        ProfileEntity(
            id = p.id,
            childCode = p.childCode,
            petName = p.petName,
            species = p.species.name,
            stage = p.stage.name,
            stageReason = p.stageReason,
            balance = p.balance,
            savingsBalance = p.savingsBalance,
            periodIndex = p.periodIndex,
            weekPeriod = p.weekPeriod,
            hunger = p.stats.hunger,
            energy = p.stats.energy,
            joy = p.stats.joy,
            lastFedPeriod = p.stats.lastFedPeriod,
            lastEntertainmentPeriod = p.stats.lastEntertainmentPeriod,
            planMandatory = p.currentPlan?.mandatory,
            planOptional = p.currentPlan?.optional,
            planSavings = p.currentPlan?.savings,
            factMandatory = p.lastFact?.mandatorySpent,
            factOptional = p.lastFact?.optionalSpent,
            factSaved = p.lastFact?.savedAmount,
            activeGoalId = p.activeGoalId,
            wardrobeJson = wardrobeToJson(p.wardrobe),
            completedTaskIdsCsv = p.completedTaskIds.joinToString(","),
            completedLessonIdsCsv = p.completedLessonIds.joinToString(","),
            ownedShopItemIdsCsv = p.ownedShopItemIds.joinToString(","),
            dailyLoginClaimedPeriod = p.dailyLoginClaimedPeriod,
            demoMode = p.demoMode,
            manualSeason = p.manualSeason?.name,
            soundEnabled = p.soundEnabled,
            animationsEnabled = p.animationsEnabled,
            activeWorkId = p.activeWorkId,
            completedWorkIdsCsv = p.completedWorkIds.joinToString(","),
            workEndsAtEpochMs = p.workEndsAtEpochMs,
            sleepEndsAtEpochMs = p.sleepEndsAtEpochMs,
            lastTickAtEpochMs = p.lastTickAtEpochMs,
            lastTaxAtEpochMs = p.lastTaxAtEpochMs,
            lastDailyLoginAtEpochMs = p.lastDailyLoginAtEpochMs,
            isIll = p.stats.isIll,
            illSinceMs = p.stats.illSinceMs,
            hungerZeroSinceMs = p.stats.hungerZeroSinceMs,
            energyZeroSinceMs = p.stats.energyZeroSinceMs,
            joyZeroSinceMs = p.stats.joyZeroSinceMs,
            zeroSinceMs = p.stats.zeroSinceMs,
            createdAtEpochMs = p.createdAtEpochMs,
            onboardingDone = onboardingDone,
            roleSelected = roleSelected
        )

    private fun parseWardrobe(json: String): List<OwnedClothingVariant> {
        if (json.isBlank()) return emptyList()
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            OwnedClothingVariant(
                itemId = o.getString("itemId"),
                variantId = o.getString("variantId"),
                isEquipped = o.optBoolean("isEquipped", false)
            )
        }
    }

    private fun wardrobeToJson(list: List<OwnedClothingVariant>): String {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("itemId", it.itemId)
                    .put("variantId", it.variantId)
                    .put("isEquipped", it.isEquipped)
            )
        }
        return arr.toString()
    }
}
