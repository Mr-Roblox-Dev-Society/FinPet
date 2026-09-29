package ru.finny.pet.domain.model

enum class UserRole {
    CHILD,
    PARENT
}

enum class PetSpecies(
    val displayName: String,
    val emoji: String
) {
    GIRAFFE("Жираф", "🦒"),
    ZEBRA("Зебра", "🦓"),
    DOG("Пёсик", "🐶"),
    CAT("Кошечка", "🐱"),
    ELEPHANT("Слоник", "🐘"),
    TIGER("Тигр", "🐯"),
    PARROT("Попугай", "🦜"),
    KOALA("Коала", "🐨"),
    FOX("Лисёнок", "🦊")
}

enum class PetStage(val displayName: String) {
    BABY("Малыш"),
    TEEN("Подросток"),
    ADULT("Взрослый")
}

enum class Season(val displayName: String) {
    SUMMER("Лето"),
    AUTUMN("Осень"),
    WINTER("Зима"),
    SPRING("Весна")
}

enum class ExpenseCategory {
    MANDATORY,
    OPTIONAL,
    SAVINGS
}

enum class WorkDifficulty(val pay: Int, val displayName: String) {
    EASY(100, "Лёгкая"),
    MEDIUM(200, "Средняя"),
    HARD(300, "Сложная")
}

enum class LessonTheme(val displayName: String) {
    BUDGET("Планирование бюджета"),
    SAVINGS("Сбережения"),
    PAYMENTS("Платежи и покупки")
}

data class PetStats(
    val hunger: Int = 80,
    val energy: Int = 80,
    val joy: Int = 80,

    val isIll: Boolean = false,

    val illSinceMs: Long = 0L,

    val hungerZeroSinceMs: Long = 0L,

    val energyZeroSinceMs: Long = 0L,

    val joyZeroSinceMs: Long = 0L,

    val zeroSinceMs: Long = 0L,
    val lastFedPeriod: Int = 0,
    val lastEntertainmentPeriod: Int = 0
) {
    fun clamped() = copy(
        hunger = hunger.coerceIn(0, 100),
        energy = energy.coerceIn(0, 100),
        joy = joy.coerceIn(0, 100)
    )
}

data class BudgetPlan(
    val mandatory: Int = 0,
    val optional: Int = 0,
    val savings: Int = 0
) {
    val total: Int get() = mandatory + optional + savings
}

data class PeriodFact(
    val mandatorySpent: Int = 0,
    val optionalSpent: Int = 0,
    val savedAmount: Int = 0
)

data class SavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Int,
    val savedAmount: Int = 0,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false
) {
    val remaining: Int get() = (targetAmount - savedAmount).coerceAtLeast(0)
    val progressFraction: Float
        get() = if (targetAmount <= 0) 0f else (savedAmount.toFloat() / targetAmount).coerceIn(0f, 1f)
}

data class OwnedClothingVariant(
    val itemId: String,
    val variantId: String,
    val isEquipped: Boolean = false
)

data class ChildProfile(
    val id: Long = 1L,
    val childCode: String,
    val petName: String,
    val species: PetSpecies,
    val stage: PetStage = PetStage.BABY,
    val stageReason: String = "Ты только начинаешь заботиться о питомце!",
    val balance: Int = EconomyConstants.STARTING_BALANCE,
    val savingsBalance: Int = 0,
    val periodIndex: Int = 1,
    val weekPeriod: Int = 1,
    val stats: PetStats = PetStats(),
    val currentPlan: BudgetPlan? = null,
    val lastFact: PeriodFact? = null,
    val activeGoalId: String? = "goal_1",
    val wardrobe: List<OwnedClothingVariant> = emptyList(),
    val completedTaskIds: Set<String> = emptySet(),
    val completedLessonIds: Set<String> = emptySet(),
    val ownedShopItemIds: Set<String> = emptySet(),
    val dailyLoginClaimedPeriod: Int = 0,
    val demoMode: Boolean = false,
    val manualSeason: Season? = null,
    val soundEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,

    val activeWorkId: String? = null,

    val completedWorkIds: Set<String> = emptySet(),

    val workEndsAtEpochMs: Long = 0L,

    val sleepEndsAtEpochMs: Long = 0L,

    val lastTickAtEpochMs: Long = System.currentTimeMillis(),

    val lastTaxAtEpochMs: Long = 0L,

    val lastDailyLoginAtEpochMs: Long = 0L,
    val createdAtEpochMs: Long = System.currentTimeMillis()
) {

    fun isWorking(nowMs: Long = System.currentTimeMillis()): Boolean =
        activeWorkId != null && nowMs < workEndsAtEpochMs

    fun isSleeping(nowMs: Long = System.currentTimeMillis()): Boolean =
        nowMs < sleepEndsAtEpochMs

    fun remainingMinutes(nowMs: Long = System.currentTimeMillis()): Int {
        val end = when {
            isWorking(nowMs) -> workEndsAtEpochMs
            isSleeping(nowMs) -> sleepEndsAtEpochMs
            else -> return 0
        }
        return (((end - nowMs) + EconomyConstants.MINUTE_IN_MS - 1) / EconomyConstants.MINUTE_IN_MS)
            .coerceAtLeast(0L).toInt()
    }
}

object EconomyConstants {
    const val STARTING_BALANCE = 500
    const val DAILY_LOGIN = 100
    const val GOOD_TEST_REWARD = 25
    const val FOOD_PRICE = 100
    const val FOOD_HUNGER_GAIN = 50
    const val MEDICINE_PRICE = 1000
    const val TAX_AMOUNT = 150
    const val ENTERTAINMENT_PRICE = 100
    const val ENTERTAINMENT_JOY_GAIN = 50
    const val MIN_SAVINGS_TRANSFER = 50
    const val SLEEP_ENERGY_GAIN = 60
    const val DESIGN_JOY_BONUS = 10
    const val WORK_DURATION_HOURS = 8
    const val PERIODS_PER_WEEK = 7
    const val DEMO_PERIODS = 5

    const val WORK_DURATION_REAL_HOURS = 8L

    const val SLEEP_DURATION_REAL_HOURS = 8L

    const val DECAY_HUNGER_PER_HOUR = 3

    const val DECAY_ENERGY_PER_HOUR = 3

    const val DECAY_JOY_PER_HOUR = 1

    const val TAX_INTERVAL_HOURS = 168L
    const val TAX_ANCHOR_HOUR = 4

    const val SICK_AFTER_ZERO_HOURS = 48L
    const val HOURS_IN_MS = 3_600_000L
    const val MINUTE_IN_MS = 60_000L
}
