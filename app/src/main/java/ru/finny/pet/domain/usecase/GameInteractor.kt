package ru.finny.pet.domain.usecase

import ru.finny.pet.content.ContentRepository
import ru.finny.pet.domain.economy.GameEconomy
import ru.finny.pet.domain.model.ActionFeedback
import ru.finny.pet.domain.model.BudgetPlan
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.ExpenseCategory
import ru.finny.pet.domain.model.OwnedClothingVariant
import ru.finny.pet.domain.model.ParentDashboard
import ru.finny.pet.domain.model.PeriodFact
import ru.finny.pet.domain.model.PetSpecies
import ru.finny.pet.domain.model.PetStage
import ru.finny.pet.domain.model.PetStats
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.ShopItemKind
import ru.finny.pet.domain.repository.ProfileRepository
import ru.finny.pet.util.ChildCodeGenerator

class GameInteractor(
    private val profiles: ProfileRepository,
    private val content: ContentRepository
) {

    private suspend fun loadSynced(): Pair<ChildProfile?, List<ActionFeedback>> {
        val raw = profiles.getProfile() ?: return null to emptyList()
        val (synced, notes) = GameEconomy.syncRealTime(raw)
        if (synced != raw) profiles.saveProfile(synced)
        return synced to notes
    }

    private suspend fun currentProfile(): ChildProfile? = loadSynced().first

    suspend fun tick() {
        val raw = profiles.getProfile() ?: return
        val synced = GameEconomy.syncSilently(raw)
        if (synced != raw) profiles.saveProfile(synced)
    }

    private suspend fun loadAndReport(block: (ChildProfile, List<ActionFeedback>) -> ActionFeedback): ActionFeedback {
        val (p, notes) = loadSynced()
        if (p == null) return missing()
        val fb = block(p, notes)

        return if (notes.isNotEmpty() && fb.title == "") notes.first() else fb
    }

    suspend fun createChildProfile(petName: String, species: PetSpecies): ChildProfile {
        val now = System.currentTimeMillis()
        val profile = ChildProfile(
            childCode = ChildCodeGenerator.generate(),
            petName = petName.ifBlank { "Финни" },
            species = species,
            stage = PetStage.BABY,
            stageReason = "Ты только начинаешь заботиться о питомце!",
            balance = EconomyConstants.STARTING_BALANCE,
            stats = PetStats(lastFedPeriod = 1, lastEntertainmentPeriod = 1),
            lastTickAtEpochMs = now,
            createdAtEpochMs = now
        )
        profiles.saveProfile(profile)
        profiles.setOnboardingDone(true)
        return profile
    }

    suspend fun findByChildCode(code: String): ChildProfile? {
        val p = profiles.getProfile() ?: return null
        return if (p.childCode.equals(code.trim(), ignoreCase = true)) p else null
    }

    suspend fun parentDashboard(): ParentDashboard? {
        val p = profiles.getProfile() ?: return null
        val spends = profiles.getSpends()
        val themes = content.lessons
            .filter { p.completedLessonIds.contains(it.id) }
            .map { it.theme }
            .distinct()
        return ParentDashboard(
            childCode = p.childCode,
            petName = p.petName,
            species = p.species,
            stage = p.stage,
            balance = p.balance,
            savingsBalance = p.savingsBalance,
            completedThemes = themes,
            completedTaskCount = p.completedTaskIds.size,
            completedLessonCount = p.completedLessonIds.size,
            recentSpends = spends
        )
    }

    suspend fun save(profile: ChildProfile) = profiles.saveProfile(profile)

    suspend fun claimDailyLogin(): ActionFeedback {
        val (p, notes) = loadSynced()
        if (p == null) return missing()
        val (updated, fb) = GameEconomy.applyDailyLogin(p)
        profiles.saveProfile(updated)
        return if (notes.isNotEmpty()) notes.first() else fb
    }

    suspend fun setBudgetPlan(plan: BudgetPlan): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val validation = GameEconomy.validateBudgetPlan(p.balance, plan)
        if (!validation.ok) {
            return ActionFeedback("План не подходит", validation.message, isPositive = false, nextStep = "Уменьши суммы.")
        }
        profiles.saveProfile(p.copy(currentPlan = plan))
        return ActionFeedback(
            "План сохранён",
            validation.message,
            nextStep = "Теперь можно работать, покупать и копить в рамках плана.",
            isPositive = true
        )
    }

    suspend fun doWork(workId: String): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val work = content.works.find { it.id == workId }
            ?: return ActionFeedback("Ошибка", "Работа не найдена", isPositive = false)
        val (updated, fb) = GameEconomy.applyWork(
            profile = p,
            difficulty = work.difficulty,
            workId = work.id,
            workTitle = work.title
        )

        profiles.saveProfile(updated)
        return fb
    }

    suspend fun buyShopItem(itemId: String): ActionFeedback {
        val (loaded, notes) = loadSynced()
        val p = loaded ?: return missing()
        val item = content.shopItems.find { it.id == itemId }
            ?: return ActionFeedback("Ошибка", "Товар не найден", isPositive = false)

        if (item.kind != ShopItemKind.MEDICINE) {
            val (blocked, reason) = GameEconomy.actionsBlocked(p)
            if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        } else if (notes.isNotEmpty()) {
            return notes.first()
        }

        when (item.kind) {
            ShopItemKind.FOOD -> {
                val (u, fb) = GameEconomy.feed(p)
                if (fb.balanceDelta < 0) profiles.logSpend(u.periodIndex, item.title, item.price, ExpenseCategory.MANDATORY)
                profiles.saveProfile(bumpFact(u, mandatory = item.price))
                return fb
            }
            ShopItemKind.MEDICINE -> {
                val (u, fb) = GameEconomy.heal(p)
                if (fb.balanceDelta < 0) profiles.logSpend(u.periodIndex, item.title, item.price, ExpenseCategory.MANDATORY)
                profiles.saveProfile(bumpFact(u, mandatory = if (fb.balanceDelta < 0) item.price else 0))
                return fb
            }
            ShopItemKind.ENTERTAINMENT -> {
                val (u, fb) = GameEconomy.entertain(p)
                if (fb.balanceDelta < 0) profiles.logSpend(u.periodIndex, item.title, item.price, ExpenseCategory.OPTIONAL)
                profiles.saveProfile(bumpFact(u, optional = if (fb.balanceDelta < 0) item.price else 0))
                return fb
            }
            ShopItemKind.TAX -> {
                if (!GameEconomy.canAfford(p.balance, item.price)) {
                    return ActionFeedback("Не хватает", "Налог ${item.price}", isPositive = false)
                }
                val u = p.copy(balance = p.balance - item.price)
                profiles.logSpend(u.periodIndex, item.title, item.price, ExpenseCategory.MANDATORY)
                profiles.saveProfile(bumpFact(u, mandatory = item.price))
                return ActionFeedback("Налог оплачен", "−${item.price}", balanceDelta = -item.price, isPositive = true)
            }
            else -> {
                if (!GameEconomy.canAfford(p.balance, item.price)) {
                    return ActionFeedback(
                        "Не хватает монет",
                        "${item.title} стоит ${item.price}. Это необязательный расход — можно отказаться.",
                        isPositive = false,
                        nextStep = "Скорректируй план или заработай."
                    )
                }
                val stats = p.stats.copy(joy = (p.stats.joy + item.joyGain).coerceAtMost(100))
                val u = p.copy(
                    balance = p.balance - item.price,
                    stats = stats,
                    ownedShopItemIds = p.ownedShopItemIds + item.id
                )
                profiles.logSpend(u.periodIndex, item.title, item.price, ExpenseCategory.OPTIONAL)
                profiles.saveProfile(bumpFact(u, optional = item.price))
                return ActionFeedback(
                    "Куплено!",
                    "${item.title}: −${item.price}. ${item.description}",
                    balanceDelta = -item.price,
                    petStateChange = if (item.joyGain > 0) "Радость +${item.joyGain}" else null,
                    nextStep = "Подумай: это было нужно или хотелось?",
                    isPositive = true
                )
            }
        }
    }

    suspend fun buyClothingBase(itemId: String): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val item = content.clothing.find { it.id == itemId }
            ?: return ActionFeedback("Ошибка", "Вещь не найдена", isPositive = false)
        if (p.wardrobe.any { it.itemId == itemId }) {
            return ActionFeedback("Уже есть", "Базовый предмет уже куплен.", isPositive = true)
        }
        if (!GameEconomy.canAfford(p.balance, item.basePrice)) {
            return ActionFeedback("Не хватает", "Нужно ${item.basePrice}", isPositive = false, nextStep = "Это обязательный расход сезона.")
        }
        val base = item.variants.first { it.isBase }
        val wardrobe = p.wardrobe + OwnedClothingVariant(itemId, base.id, isEquipped = true)
        val u = p.copy(balance = p.balance - item.basePrice, wardrobe = wardrobe)
        profiles.logSpend(u.periodIndex, "Одежда: ${item.title}", item.basePrice, ExpenseCategory.MANDATORY)
        profiles.saveProfile(bumpFact(u, mandatory = item.basePrice))
        return ActionFeedback(
            "Одежда куплена",
            "${item.title} (база) −${item.basePrice}. Теперь питомец готов к сезону!",
            balanceDelta = -item.basePrice,
            isPositive = true,
            nextStep = "Доп. цвета — по желанию."
        )
    }

    suspend fun buyClothingVariant(itemId: String, variantId: String): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val item = content.clothing.find { it.id == itemId } ?: return ActionFeedback("Ошибка", "Не найдено", isPositive = false)
        val variant = item.variants.find { it.id == variantId } ?: return ActionFeedback("Ошибка", "Вариант не найден", isPositive = false)
        if (variant.isBase) return buyClothingBase(itemId)
        if (p.wardrobe.none { it.itemId == itemId }) {
            return ActionFeedback("Сначала база", "Купи базовый ${item.title}.", isPositive = false)
        }
        if (p.wardrobe.any { it.variantId == variantId }) {
            return ActionFeedback("Уже куплено", "Можно надеть из гардероба.", isPositive = true)
        }
        if (!GameEconomy.canAfford(p.balance, variant.price)) {
            return ActionFeedback("Не хватает", "Цена ${variant.price} — необязательный расход.", isPositive = false)
        }
        val wardrobe = p.wardrobe.map { if (it.itemId == itemId) it.copy(isEquipped = false) else it } +
            OwnedClothingVariant(itemId, variantId, isEquipped = true)
        val stats = p.stats.copy(joy = (p.stats.joy + EconomyConstants.DESIGN_JOY_BONUS).coerceAtMost(100))
        val u = p.copy(balance = p.balance - variant.price, wardrobe = wardrobe, stats = stats)
        profiles.logSpend(u.periodIndex, "Дизайн: ${variant.title}", variant.price, ExpenseCategory.OPTIONAL)
        profiles.saveProfile(bumpFact(u, optional = variant.price))
        return ActionFeedback(
            "Новый стиль!",
            "${variant.title}: −${variant.price}, радость +${EconomyConstants.DESIGN_JOY_BONUS}.",
            balanceDelta = -variant.price,
            petStateChange = "Радость ${stats.joy}",
            isPositive = true,
            nextStep = "Это необязательный расход — для настроения."
        )
    }

    suspend fun equipVariant(itemId: String, variantId: String): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        if (p.wardrobe.none { it.variantId == variantId }) {
            return ActionFeedback("Нет в гардеробе", "Сначала купи этот вариант.", isPositive = false)
        }
        val wardrobe = p.wardrobe.map {
            if (it.itemId == itemId) it.copy(isEquipped = it.variantId == variantId) else it
        }
        profiles.saveProfile(p.copy(wardrobe = wardrobe))
        return ActionFeedback("Переодето!", "Питомец в новом виде на главном экране.", isPositive = true)
    }

    suspend fun sleep(): ActionFeedback {
        val (p, _) = loadSynced()
        if (p == null) return missing()
        val (u, fb) = GameEconomy.sleep(p)
        profiles.saveProfile(u)
        return fb
    }

    suspend fun transferSavings(amount: Int): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val (u, fb) = GameEconomy.transferToSavings(p, amount)
        if (fb.savingsDelta > 0) {
            profiles.saveProfile(bumpFact(u, saved = amount))
        }
        return fb
    }

    suspend fun withdrawSavings(amount: Int, confirmed: Boolean): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val (u, fb) = GameEconomy.withdrawFromSavings(p, amount, confirmed)
        if (confirmed && fb.savingsDelta < 0) profiles.saveProfile(u)
        return fb
    }

    suspend fun completeTask(taskId: String, optionId: String): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val task = content.tasks.find { it.id == taskId } ?: return ActionFeedback("Ошибка", "Задание не найдено", isPositive = false)
        val option = task.options.find { it.id == optionId } ?: return ActionFeedback("Ошибка", "Вариант не найден", isPositive = false)
        var u = p.copy(completedTaskIds = p.completedTaskIds + taskId)
        if (option.consequenceBalance != 0 && option.isCorrect) {
            if (option.consequenceBalance < 0 && GameEconomy.canAfford(u.balance, -option.consequenceBalance)) {
                val (saved, _) = GameEconomy.transferToSavings(u, -option.consequenceBalance)
                u = saved
            }
        }
        profiles.saveProfile(u)
        return ActionFeedback(
            title = if (option.isCorrect) "Отлично!" else "Давай разберём",
            message = if (option.isCorrect) task.correctExplanation else task.wrongExplanation,
            isPositive = option.isCorrect,
            nextStep = if (option.isCorrect) "Можешь взять следующий урок." else "Попробуй скорректировать план или отказаться от желания."
        )
    }

    suspend fun completeLessonTest(lessonId: String, correctCount: Int, total: Int): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        val (blocked, reason) = GameEconomy.actionsBlocked(p)
        if (blocked) return ActionFeedback("Не сейчас", reason, isPositive = false)
        val good = correctCount * 2 >= total
        val reward = if (correctCount == total) EconomyConstants.GOOD_TEST_REWARD else 0
        val u = p.copy(
            balance = p.balance + reward,
            completedLessonIds = if (good) p.completedLessonIds + lessonId else p.completedLessonIds
        )
        profiles.saveProfile(u)
        return if (reward > 0) {
            ActionFeedback(
                "Тест сдан!",
                "Все ответы верны. Награда +$reward. ${content.lessons.find { it.id == lessonId }?.title ?: ""}",
                balanceDelta = reward,
                isPositive = true,
                nextStep = "Примени знания в плане бюджета."
            )
        } else {
            ActionFeedback(
                "Ещё чуть-чуть",
                "Правильно $correctCount из $total. Перечитай урок и попробуй снова. Награда ${EconomyConstants.GOOD_TEST_REWARD} — за хороший результат.",
                isPositive = false,
                nextStep = "Открой теорию ещё раз."
            )
        }
    }

    suspend fun advancePeriod(): ActionFeedback {
        val p = profiles.getProfile() ?: return missing()
        var u = GameEconomy.checkHealthRules(p)
        val (afterTax, taxFb) = GameEconomy.applyTaxIfNeeded(u)
        u = afterTax
        val plan = u.currentPlan
        val fact = u.lastFact ?: PeriodFact()
        val compare = if (plan != null) GameEconomy.comparePlanAndFact(plan, fact) else "План на период не был составлен."
        u = GameEconomy.advancePeriod(u)

        u = u.copy(activeWorkId = null)

        val mandatoryOk = if (plan != null && plan.mandatory > 0) {
            (fact.mandatorySpent.toFloat() / plan.mandatory).coerceIn(0f, 1.5f)
        } else 0.3f
        val planMatch = if (plan != null) {
            val diffs = listOf(
                kotlin.math.abs(plan.mandatory - fact.mandatorySpent),
                kotlin.math.abs(plan.optional - fact.optionalSpent),
                kotlin.math.abs(plan.savings - fact.savedAmount)
            ).sum()
            (1f - diffs / (plan.total.coerceAtLeast(1).toFloat() + 1f)).coerceIn(0f, 1f)
        } else 0.2f
        val streak = if (fact.savedAmount >= EconomyConstants.MIN_SAVINGS_TRANSFER) 2 else 0
        val (stage, reason) = GameEconomy.evaluateStage(u, mandatoryOk, planMatch, streak)
        u = u.copy(stage = stage, stageReason = reason, lastFact = PeriodFact())
        profiles.saveProfile(u)
        val taxPart = taxFb?.message?.let { "\n\n$it" }.orEmpty()
        return ActionFeedback(
            title = "Период завершён",
            message = "Сравнение плана и факта:\n$compare$taxPart\n\n${u.stageReason}",
            petStateChange = "Стадия: ${u.stage.displayName}",
            nextStep = if (u.demoMode && u.periodIndex > EconomyConstants.DEMO_PERIODS) {
                "Демо из 5 периодов можно сбросить в настройках."
            } else {
                "Составь новый план и забери ежедневный вход."
            },
            isPositive = true
        )
    }

    suspend fun setDemoMode(enabled: Boolean) {
        val p = profiles.getProfile() ?: return
        profiles.saveProfile(
            p.copy(
                demoMode = enabled,
                periodIndex = if (enabled) 1 else p.periodIndex,
                weekPeriod = 1,
                dailyLoginClaimedPeriod = 0
            )
        )
    }

    suspend fun setManualSeason(season: Season?) {
        val p = profiles.getProfile() ?: return
        profiles.saveProfile(p.copy(manualSeason = season))
    }

    suspend fun resetToDefaults() {
        profiles.resetProfile()
    }

    suspend fun currentSeason(): Season {
        val p = profiles.getProfile()
        return GameEconomy.seasonFor(manual = p?.manualSeason)
    }

    private fun bumpFact(p: ChildProfile, mandatory: Int = 0, optional: Int = 0, saved: Int = 0): ChildProfile {
        val f = p.lastFact ?: PeriodFact()
        return p.copy(
            lastFact = f.copy(
                mandatorySpent = f.mandatorySpent + mandatory,
                optionalSpent = f.optionalSpent + optional,
                savedAmount = f.savedAmount + saved
            )
        )
    }

    private fun missing() = ActionFeedback("Нет профиля", "Создай питомца.", isPositive = false)
}
