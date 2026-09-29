package ru.finny.pet.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ROLE = "role"
    const val ONBOARDING = "onboarding"
    const val CREATE_PET = "create_pet"
    const val PARENT_LOGIN = "parent_login"
    const val HOME = "home"
    const val BUDGET = "budget"
    const val WORK = "work"
    const val SHOP = "shop"
    const val CLOTHING = "clothing"
    const val SAVINGS = "savings"
    const val TASKS = "tasks"
    const val TASK_DETAIL = "task/{taskId}"
    const val LESSONS = "lessons"
    const val LESSON_DETAIL = "lesson/{lessonId}"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val PARENT = "parent"
    const val ADULT_GATE = "adult_gate"

    fun task(id: String) = "task/$id"
    fun lesson(id: String) = "lesson/$id"
}
