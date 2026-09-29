package ru.finny.pet.domain.economy

import ru.finny.pet.domain.model.ActionFeedback
import ru.finny.pet.domain.model.BudgetPlan
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.PetStage
import ru.finny.pet.domain.model.PetStats
import ru.finny.pet.domain.model.PeriodFact
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.WorkDifficulty
import java.util.Calendar

object GameEconomy {

    fun seasonFor(nowMs: Long = System.currentTimeMillis(), manual: Season?): Season {
        if (manual != null) return manual
        val month = Calendar.getInstance().apply { timeInMillis = nowMs }.get(Calendar.MONTH)
        return when (month) {
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> Season.WINTER
            Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> Season.SPRING
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> Season.SUMMER
            else -> Season.AUTUMN
        }
    }

    fun baseOutfitCost(season: Season): Int = when (season) {
        Season.SUMMER -> 240
        Season.AUTUMN -> 380
        Season.WINTER -> 700
        Season.SPRING -> 330
    }

    fun canAfford(balance: Int, price: Int): Boolean = balance >= price

    fun validateBudgetPlan(balance: Int, plan: BudgetPlan): PlanValidation {
        if (plan.mandatory < 0 || plan.optional < 0 || plan.savings < 0) {
            return PlanValidation(false, "Суммы не могут быть отрицательными.", balance)
        }
        if (plan.total > balance) {
            return PlanValidation(
                false,
                "План больше бюджета. Уменьши суммы — остаток должен быть ≥ 0.",
                balance - plan.total
            )
        }
        return PlanValidation(true, "План готов! Остаток: ${balance - plan.total}", balance - plan.total)
    }

    data class PlanValidation(val ok: Boolean, val message: String, val leftover: Int)

    fun syncRealTime(
        profile: ChildProfile,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<ChildProfile, List<ActionFeedback>> {
        val notes = mutableListOf<ActionFeedback>()
        var p = profile

        if (p.activeWorkId != null && nowMs >= p.workEndsAtEpochMs) {
            p = p.copy(activeWorkId = null, workEndsAtEpochMs = 0L)
        }
        val wasSleeping = p.sleepEndsAtEpochMs > 0L && nowMs >= p.sleepEndsAtEpochMs
        if (wasSleeping) {
            p = p.copy(sleepEndsAtEpochMs = 0L)
        }

        p = decayStats(p, nowMs, applySleepBonusOnWake = wasSleeping)

        val (afterTax, taxFb) = applyAutoTaxIfNeeded(p, nowMs)
        p = afterTax
        taxFb?.let { notes.add(it) }

        return p to notes
    }

    fun syncSilently(profile: ChildProfile, nowMs: Long = System.currentTimeMillis()): ChildProfile =
        syncRealTime(profile, nowMs).first

    private fun decayStats(
        profile: ChildProfile,
        nowMs: Long,
        applySleepBonusOnWake: Boolean = false
    ): ChildProfile {
        val elapsedMs = (nowMs - profile.lastTickAtEpochMs).coerceAtLeast(0L)
        if (elapsedMs <= 0L) return checkSickness(profile, nowMs)
        val hours = elapsedMs / EconomyConstants.HOURS_IN_MS
        if (hours <= 0L) return profile.copy(lastTickAtEpochMs = nowMs)

        var hunger = profile.stats.hunger - (hours * EconomyConstants.DECAY_HUNGER_PER_HOUR).toInt()
        var energy = profile.stats.energy - (hours * EconomyConstants.DECAY_ENERGY_PER_HOUR).toInt()
        val joy = profile.stats.joy - (hours * EconomyConstants.DECAY_JOY_PER_HOUR).toInt()

        if (applySleepBonusOnWake) {
            energy += EconomyConstants.SLEEP_DURATION_REAL_HOURS.toInt() * EconomyConstants.DECAY_ENERGY_PER_HOUR
            energy += EconomyConstants.SLEEP_ENERGY_GAIN
        }

        val stats = profile.stats.copy(
            hunger = hunger.coerceIn(0, 100),
            energy = energy.coerceIn(0, 100),
            joy = joy.coerceIn(0, 100)
        )
        val updated = profile.copy(stats = stats, lastTickAtEpochMs = nowMs)
        return checkSickness(updated, nowMs)
    }

    private fun checkSickness(profile: ChildProfile, nowMs: Long): ChildProfile {
        val st = profile.stats
        fun zeroSince(value: Int, prev: Long): Long = when {
            value > 0 -> 0L
            prev == 0L -> nowMs
            else -> prev
        }
        val hz = zeroSince(st.hunger, st.hungerZeroSinceMs)
        val ez = zeroSince(st.energy, st.energyZeroSinceMs)
        val jz = zeroSince(st.joy, st.joyZeroSinceMs)
        val thresholdMs = EconomyConstants.SICK_AFTER_ZERO_HOURS * EconomyConstants.HOURS_IN_MS
        val anyZeroTooLong = listOf(hz, ez, jz).any { it != 0L && nowMs - it >= thresholdMs }
        val allPositive = st.hunger > 0 && st.energy > 0 && st.joy > 0
        val isIll = if (allPositive) false else (profile.stats.isIll || anyZeroTooLong)
        val illSince = when {
            !isIll -> 0L
            profile.stats.isIll && profile.stats.illSinceMs != 0L -> profile.stats.illSinceMs
            else -> nowMs
        }

        val anyZero = listOf(hz, ez, jz).filter { it != 0L }.minOrNull() ?: 0L

        return profile.copy(
            stats = st.copy(
                hungerZeroSinceMs = hz,
                energyZeroSinceMs = ez,
                joyZeroSinceMs = jz,
                zeroSinceMs = anyZero,
                isIll = isIll,
                illSinceMs = illSince
            )
        )
    }

    fun nextTaxDueMs(profile: ChildProfile): Long {
        val first = profile.createdAtEpochMs + EconomyConstants.TAX_ANCHOR_HOUR * EconomyConstants.HOURS_IN_MS
        val last = if (profile.lastTaxAtEpochMs == 0L) first - EconomyConstants.TAX_INTERVAL_HOURS * EconomyConstants.HOURS_IN_MS
                   else profile.lastTaxAtEpochMs
        return last + EconomyConstants.TAX_INTERVAL_HOURS * EconomyConstants.HOURS_IN_MS
    }

    private fun applyAutoTaxIfNeeded(
        profile: ChildProfile,
        nowMs: Long
    ): Pair<ChildProfile, ActionFeedback?> {
        if (profile.demoMode) return profile to null
        val due = nextTaxDueMs(profile)
        if (nowMs < due) return profile to null
        val newLastTax = if (profile.lastTaxAtEpochMs == 0L) due
                         else maxOf(due, profile.lastTaxAtEpochMs + EconomyConstants.TAX_INTERVAL_HOURS * EconomyConstants.HOURS_IN_MS)
        if (!canAfford(profile.balance, EconomyConstants.TAX_AMOUNT)) {

            return profile.copy(lastTaxAtEpochMs = newLastTax) to ActionFeedback(
                title = "Налог не удержан",
                message = "На балансе меньше ${EconomyConstants.TAX_AMOUNT} — налог ${EconomyConstants.TAX_AMOUNT} списать не удалось. В следующий раз запланируй обязательный расход заранее.",
                isPositive = false,
                nextStep = "Заработай и учти налог в плане бюджета."
            )
        }
        val updated = profile.copy(
            balance = profile.balance - EconomyConstants.TAX_AMOUNT,
            lastTaxAtEpochMs = newLastTax
        )
        return updated to ActionFeedback(
            title = "Налог списан автоматически",
            message = "Государство забрало ${EconomyConstants.TAX_AMOUNT} монет. Налог приходит сам каждые ${EconomyConstants.TAX_INTERVAL_HOURS} ч (раз в неделю), начиная с 4-го часа.",
            balanceDelta = -EconomyConstants.TAX_AMOUNT,
            isPositive = false,
            nextStep = "Это обязательный расход — планируй его в бюджете заранее."
        )
    }

    fun applyDailyLogin(
        profile: ChildProfile,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<ChildProfile, ActionFeedback> {
        val dayMs = 24 * EconomyConstants.HOURS_IN_MS
        if (profile.lastDailyLoginAtEpochMs != 0L && nowMs - profile.lastDailyLoginAtEpochMs < dayMs) {
            return profile to ActionFeedback(
                title = "Уже получено",
                message = "Награду за вход сегодня ты уже забрал(а).",
                isPositive = false,
                nextStep = "Можно работать, кормить питомца или копить."
            )
        }
        val updated = profile.copy(
            balance = profile.balance + EconomyConstants.DAILY_LOGIN,
            lastDailyLoginAtEpochMs = nowMs
        )
        return updated to ActionFeedback(
            title = "Добро пожаловать!",
            message = "Ты получил(а) ${EconomyConstants.DAILY_LOGIN} монет за вход.",
            balanceDelta = EconomyConstants.DAILY_LOGIN,
            nextStep = "Составь план бюджета на период.",
            isPositive = true
        )
    }

    fun actionsBlocked(profile: ChildProfile, nowMs: Long = System.currentTimeMillis()): Pair<Boolean, String> {
        if (profile.stats.isIll) {
            return true to "Питомец болеет! Доступен только ежедневный вход — копи на лекарство (${EconomyConstants.MEDICINE_PRICE})."
        }
        if (profile.isSleeping(nowMs)) {
            return true to "Питомец спит ещё ${formatRemain(profile.remainingMinutes(nowMs))}. Во сне другие действия недоступны."
        }
        if (profile.isWorking(nowMs)) {
            return true to "Питомец на работе ещё ${formatRemain(profile.remainingMinutes(nowMs))} — подожди окончания смены."
        }
        return false to ""
    }

    private fun formatRemain(min: Int): String {
        val h = min / 60
        val m = min % 60
        return if (h > 0) "${h} часов ${m} минут" else "$m минут"
    }

    fun canWork(
        profile: ChildProfile,
        workId: String? = null,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<Boolean, String> {
        val (blocked, reason) = actionsBlocked(profile, nowMs)
        if (blocked) return false to reason
        if (profile.stats.energy <= 0) {
            return false to "Энергия на нуле. Сначала покорми и дай питомцу поесть/поиграть — или он заболеет."
        }
        if (workId != null && profile.completedWorkIds.contains(workId)) {
            return false to "Эта работа уже выполнена — каждая работа одноразовая."
        }
        if (workId == null && profile.worksAllDone()) {
            return false to "Все работы выполнены! Питомец освоил все виды труда."
        }
        return true to "Можно работать (смена длится 8 реальных часов)."
    }

    private fun ChildProfile.worksAllDone(): Boolean = completedWorkIds.size >= 21

    fun applyWork(
        profile: ChildProfile,
        difficulty: WorkDifficulty,
        workId: String,
        workTitle: String = ""
    ): Pair<ChildProfile, ActionFeedback> {
        val (ok, reason) = canWork(profile, workId)
        if (!ok) {
            return profile to ActionFeedback("Нельзя работать", reason, isPositive = false, nextStep = "Вылечи или подожди окончания сна/смены.")
        }
        val now = System.currentTimeMillis()
        val durationMs = EconomyConstants.WORK_DURATION_REAL_HOURS * EconomyConstants.HOURS_IN_MS
        val updated = profile.copy(
            balance = profile.balance + difficulty.pay,
            activeWorkId = workId,
            completedWorkIds = profile.completedWorkIds + workId,
            workEndsAtEpochMs = now + durationMs
        )
        val titlePart = if (workTitle.isNotBlank()) "$workTitle — " else ""
        return updated to ActionFeedback(
            title = "Смена началась!",
            message = "${titlePart}заработано ${difficulty.pay} (${difficulty.displayName}). Питомец работает 8 реальных часов — всё это время на главном экране показывается его рабочая картинка.",
            balanceDelta = difficulty.pay,
            nextStep = "Работа одноразовая — она больше недоступна. Вернёшься через 8 часов.",
            isPositive = true
        )
    }

    fun feed(profile: ChildProfile): Pair<ChildProfile, ActionFeedback> {
        if (!canAfford(profile.balance, EconomyConstants.FOOD_PRICE)) {
            return profile to ActionFeedback(
                "Не хватает монет",
                "Еда стоит ${EconomyConstants.FOOD_PRICE}. Заработай или отложи необязательную покупку.",
                isPositive = false,
                nextStep = "Возьми лёгкую работу или скорректируй план."
            )
        }
        val stats = profile.stats.copy(
            hunger = profile.stats.hunger + EconomyConstants.FOOD_HUNGER_GAIN,
            lastFedPeriod = profile.periodIndex
        ).clamped()
        val updated = profile.copy(
            balance = profile.balance - EconomyConstants.FOOD_PRICE,
            stats = stats
        )
        return updated to ActionFeedback(
            title = "Вкусно!",
            message = "Потрачено ${EconomyConstants.FOOD_PRICE} на еду. Голод +${EconomyConstants.FOOD_HUNGER_GAIN}.",
            balanceDelta = -EconomyConstants.FOOD_PRICE,
            petStateChange = "Голод ${stats.hunger}",
            nextStep = "Это обязательный расход — питомец должен есть каждый день.",
            isPositive = true
        )
    }

    fun sleep(
        profile: ChildProfile,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<ChildProfile, ActionFeedback> {
        if (profile.isSleeping(nowMs)) {
            return profile to ActionFeedback(
                "Питомец уже спит",
                "До пробуждения ещё ${profile.remainingMinutes(nowMs)} мин.",
                isPositive = false
            )
        }
        val (blocked, reason) = actionsBlocked(profile, nowMs)
        if (blocked) {
            return profile to ActionFeedback("Не сейчас", reason, isPositive = false)
        }
        val durationMs = EconomyConstants.SLEEP_DURATION_REAL_HOURS * EconomyConstants.HOURS_IN_MS
        val updated = profile.copy(sleepEndsAtEpochMs = nowMs + durationMs)
        return updated to ActionFeedback(
            title = "Спокойной ночи",
            message = "Питомец уснул на 8 реальных часов. Энергия будет восстанавливаться во сне (+${EconomyConstants.SLEEP_ENERGY_GAIN} за цикл), голод и радость тоже медленно убывают.",
            nextStep = "Пока питомец спит, действия недоступны — возвращайся после пробуждения!",
            isPositive = true
        )
    }

    fun entertain(profile: ChildProfile): Pair<ChildProfile, ActionFeedback> {
        if (!canAfford(profile.balance, EconomyConstants.ENTERTAINMENT_PRICE)) {
            return profile to ActionFeedback(
                "Не хватает монет",
                "Развлечение стоит ${EconomyConstants.ENTERTAINMENT_PRICE}.",
                isPositive = false,
                nextStep = "Это необязательный расход — можно отложить."
            )
        }
        val stats = profile.stats.copy(
            joy = profile.stats.joy + EconomyConstants.ENTERTAINMENT_JOY_GAIN,
            lastEntertainmentPeriod = profile.periodIndex
        ).clamped()
        val updated = profile.copy(
            balance = profile.balance - EconomyConstants.ENTERTAINMENT_PRICE,
            stats = stats
        )
        return updated to ActionFeedback(
            title = "Весело!",
            message = "Развлечение: −${EconomyConstants.ENTERTAINMENT_PRICE}, радость +${EconomyConstants.ENTERTAINMENT_JOY_GAIN}.",
            balanceDelta = -EconomyConstants.ENTERTAINMENT_PRICE,
            petStateChange = "Радость ${stats.joy}",
            nextStep = "Покупай развлечение примерно раз в 3 дня.",
            isPositive = true
        )
    }

    fun heal(profile: ChildProfile): Pair<ChildProfile, ActionFeedback> {
        if (!profile.stats.isIll) {
            return profile to ActionFeedback("Питомец здоров", "Лекарство не нужно.", isPositive = true)
        }
        if (!canAfford(profile.balance, EconomyConstants.MEDICINE_PRICE)) {
            return profile to ActionFeedback(
                "Нужно накопить",
                "Лекарство стоит ${EconomyConstants.MEDICINE_PRICE}. Прогресс не пропадёт — вылечишь позже.",
                isPositive = false,
                nextStep = "Бери ежедневный вход и копи на лечение."
            )
        }

        val stats = profile.stats.copy(
            isIll = false,
            illSinceMs = 0L,
            hungerZeroSinceMs = 0L,
            energyZeroSinceMs = 0L,
            joyZeroSinceMs = 0L,
            hunger = maxOf(profile.stats.hunger, 40),
            energy = maxOf(profile.stats.energy, 40),
            joy = maxOf(profile.stats.joy, 40)
        )
        val updated = profile.copy(
            balance = profile.balance - EconomyConstants.MEDICINE_PRICE,
            stats = stats
        )
        return updated to ActionFeedback(
            title = "Выздоровел!",
            message = "Потрачено ${EconomyConstants.MEDICINE_PRICE} на лекарство. Прогресс сохранён.",
            balanceDelta = -EconomyConstants.MEDICINE_PRICE,
            petStateChange = "Здоров",
            nextStep = "Снова можно работать и играть.",
            isPositive = true
        )
    }

    fun transferToSavings(profile: ChildProfile, amount: Int): Pair<ChildProfile, ActionFeedback> {
        if (amount < EconomyConstants.MIN_SAVINGS_TRANSFER) {
            return profile to ActionFeedback(
                "Слишком мало",
                "Минимум перевода — ${EconomyConstants.MIN_SAVINGS_TRANSFER}.",
                isPositive = false
            )
        }
        if (!canAfford(profile.balance, amount)) {
            return profile to ActionFeedback(
                "Не хватает",
                "На балансе только ${profile.balance}.",
                isPositive = false,
                nextStep = "Уменьши сумму или заработай."
            )
        }
        val updated = profile.copy(
            balance = profile.balance - amount,
            savingsBalance = profile.savingsBalance + amount
        )
        return updated to ActionFeedback(
            title = "Отложено!",
            message = "Переведено $amount в накопления.",
            balanceDelta = -amount,
            savingsDelta = amount,
            nextStep = "Регулярные накопления помогают достичь цели быстрее.",
            isPositive = true
        )
    }

    fun withdrawFromSavings(profile: ChildProfile, amount: Int, confirmed: Boolean): Pair<ChildProfile, ActionFeedback> {
        if (!confirmed) {
            val newSavings = profile.savingsBalance - amount
            val etaHint = estimatePeriodsToGoal(newSavings.coerceAtLeast(0), 1000, amount.coerceAtLeast(50))
            return profile to ActionFeedback(
                title = "Подтверди снятие",
                message = "Накопления станут ${profile.savingsBalance - amount}. Срок цели может увеличиться (~$etaHint периодов при среднем пополнении).",
                savingsDelta = -amount,
                isPositive = false,
                nextStep = "Подтверди, если уверен(а)."
            )
        }
        if (amount <= 0 || amount > profile.savingsBalance) {
            return profile to ActionFeedback("Нельзя снять", "Проверь сумму.", isPositive = false)
        }
        val updated = profile.copy(
            balance = profile.balance + amount,
            savingsBalance = profile.savingsBalance - amount
        )
        return updated to ActionFeedback(
            title = "Снято",
            message = "Вернули $amount на баланс. Цель отодвинулась — это нормально, можно снова копить.",
            balanceDelta = amount,
            savingsDelta = -amount,
            isPositive = true,
            nextStep = "Скорректируй план накоплений."
        )
    }

    fun estimatePeriodsToGoal(saved: Int, target: Int, avgTransfer: Int): Int {
        if (saved >= target) return 0
        if (avgTransfer <= 0) return Int.MAX_VALUE / 4
        val need = target - saved
        return (need + avgTransfer - 1) / avgTransfer
    }

    fun applyTaxIfNeeded(profile: ChildProfile): Pair<ChildProfile, ActionFeedback?> =
        profile to null

    fun checkHealthRules(profile: ChildProfile): ChildProfile = profile

    fun comparePlanAndFact(plan: BudgetPlan, fact: PeriodFact): String {
        fun mark(name: String, p: Int, f: Int): String {
            val diff = f - p
            return when {
                diff == 0 -> "$name: как в плане ($f)"
                diff > 0 -> "$name: больше плана на $diff"
                else -> "$name: меньше плана на ${-diff}"
            }
        }
        return buildString {
            appendLine(mark("Обязательные", plan.mandatory, fact.mandatorySpent))
            appendLine(mark("Необязательные", plan.optional, fact.optionalSpent))
            append(mark("Накопления", plan.savings, fact.savedAmount))
        }.trim()
    }

    fun advancePeriod(profile: ChildProfile): ChildProfile {
        val next = profile.periodIndex + 1
        val weekPeriod = ((next - 1) % EconomyConstants.PERIODS_PER_WEEK) + 1
        return profile.copy(
            periodIndex = next,
            weekPeriod = weekPeriod,
            currentPlan = null
        )
    }

    fun evaluateStage(profile: ChildProfile, mandatoryOkRatio: Float, planMatchRatio: Float, savingsStreak: Int): Pair<PetStage, String> {
        val score = mandatoryOkRatio * 0.4f + planMatchRatio * 0.3f + (savingsStreak.coerceAtMost(5) / 5f) * 0.3f
        return when {
            score >= 0.75f -> PetStage.ADULT to "Ты регулярно закрываешь обязательные расходы, следуешь плану и копишь — питомец вырос!"
            score >= 0.45f -> PetStage.TEEN to "Ты уже лучше планируешь и иногда откладываешь — питомец стал подростком."
            else -> PetStage.BABY to "Питомец ещё малыш. Чаще закрывай обязательные расходы и откладывай хотя бы понемногу."
        }
    }

    fun spendBalance(profile: ChildProfile, amount: Int): ChildProfile =
        profile.copy(balance = (profile.balance - amount).coerceAtLeast(0))
}
