package ru.finny.pet.domain.model

data class WorkDefinition(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: WorkDifficulty,
    val isPhysical: Boolean,

    val imageAssetPath: String = ""
)

data class ShopItemDefinition(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val category: ExpenseCategory,
    val kind: ShopItemKind,
    val joyGain: Int = 0,
    val hungerGain: Int = 0
)

enum class ShopItemKind {
    FOOD,
    MEDICINE,
    ENTERTAINMENT,
    TOY,
    SWEET,
    TAX,
    CLOTHING_BASE,
    CLOTHING_VARIANT
}

data class ClothingItemDefinition(
    val id: String,
    val season: Season,
    val title: String,
    val basePrice: Int,
    val isMandatory: Boolean,
    val variants: List<ClothingVariantDefinition>
)

data class ClothingVariantDefinition(
    val id: String,
    val title: String,
    val colorHex: String,
    val price: Int,
    val isBase: Boolean
)

data class TaskDefinition(
    val id: String,
    val theme: LessonTheme,
    val title: String,
    val situation: String,
    val type: TaskType,
    val options: List<TaskOption>,
    val distributeTotal: Int? = null,
    val correctExplanation: String,
    val wrongExplanation: String
)

enum class TaskType {
    CHOICE,
    DISTRIBUTE,
    PICK_PURCHASE,
    SAVE_AMOUNT
}

data class TaskOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val consequenceBalance: Int = 0
)

data class LessonDefinition(
    val id: String,
    val theme: LessonTheme,
    val title: String,
    val theoryText: String,
    val testQuestions: List<TestQuestion>
)

data class TestQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val miniActionHint: String,
    val explanation: String
)

data class ActionFeedback(
    val title: String,
    val message: String,
    val balanceDelta: Int = 0,
    val savingsDelta: Int = 0,
    val petStateChange: String? = null,
    val nextStep: String? = null,
    val isPositive: Boolean = true
)

data class ParentDashboard(
    val childCode: String,
    val petName: String,
    val species: PetSpecies,
    val stage: PetStage,
    val balance: Int,
    val savingsBalance: Int,
    val completedThemes: List<LessonTheme>,
    val completedTaskCount: Int,
    val completedLessonCount: Int,
    val recentSpends: List<SpendRecord>
)

data class SpendRecord(
    val period: Int,
    val title: String,
    val amount: Int,
    val category: ExpenseCategory
)
