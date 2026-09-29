package ru.finny.pet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.finny.pet.di.AppContainer
import ru.finny.pet.ui.FinnyViewModel
import ru.finny.pet.ui.screens.AdultGateScreen
import ru.finny.pet.ui.screens.BudgetScreen
import ru.finny.pet.ui.screens.ClothingScreen
import ru.finny.pet.ui.screens.CreatePetScreen
import ru.finny.pet.ui.screens.HomeScreen
import ru.finny.pet.ui.screens.LessonDetailScreen
import ru.finny.pet.ui.screens.LessonsScreen
import ru.finny.pet.ui.screens.OnboardingScreen
import ru.finny.pet.ui.screens.ParentLoginScreen
import ru.finny.pet.ui.screens.ParentScreen
import ru.finny.pet.ui.screens.ProgressScreen
import ru.finny.pet.ui.screens.RoleSelectScreen
import ru.finny.pet.ui.screens.SavingsScreen
import ru.finny.pet.ui.screens.SettingsScreen
import ru.finny.pet.ui.screens.ShopScreen
import ru.finny.pet.ui.screens.TaskDetailScreen
import ru.finny.pet.ui.screens.TasksScreen
import ru.finny.pet.ui.screens.WorkScreen
import ru.finny.pet.ui.screens.collectAsStateWithLifecycleSafe

@Composable
fun FinnyNavHost(
    vm: FinnyViewModel = viewModel(factory = FinnyViewModel.factory())
) {
    var startReady by remember { mutableStateOf(false) }
    var startRoute by remember { mutableStateOf(Routes.ROLE) }

    LaunchedEffect(Unit) {
        val existing = AppContainer.profiles.getProfile()
        startRoute = if (existing != null) Routes.HOME else Routes.ROLE
        startReady = true
    }

    if (!startReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val nav = rememberNavController()
    val profile by vm.profileFlow.collectAsStateWithLifecycleSafe()
    val season by vm.season.collectAsStateWithLifecycleSafe()
    val parent by vm.parent.collectAsStateWithLifecycleSafe()

    androidx.compose.runtime.CompositionLocalProvider(
        ru.finny.pet.ui.components.LocalFinnyContent provides AppContainer.content
    ) {
    NavHost(navController = nav, startDestination = startRoute) {
        composable(Routes.ROLE) {
            RoleSelectScreen(
                onChild = { nav.navigate(Routes.ONBOARDING) },
                onParent = { nav.navigate(Routes.PARENT_LOGIN) }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onContinue = { nav.navigate(Routes.CREATE_PET) })
        }
        composable(Routes.CREATE_PET) {
            CreatePetScreen { name, species ->
                vm.createPet(name, species) {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.ROLE) { inclusive = true }
                    }
                }
            }
        }
        composable(Routes.PARENT_LOGIN) {
            ParentLoginScreen(
                onSuccess = {
                    vm.loadParent()
                    nav.navigate(Routes.PARENT)
                },
                onBack = { nav.popBackStack() },
                onFailMessage = null,
                check = { code, ok, fail -> vm.checkParentCode(code, ok, fail) }
            )
        }
        composable(Routes.HOME) {
            val p = profile
            if (p == null) {
                RoleSelectScreen(
                    onChild = { nav.navigate(Routes.ONBOARDING) },
                    onParent = { nav.navigate(Routes.PARENT_LOGIN) }
                )
            } else {
                val activeTask = vm.content.tasks.firstOrNull { !p.completedTaskIds.contains(it.id) }
                val goal = vm.content.goals.firstOrNull { it.id == p.activeGoalId } ?: vm.content.goals.firstOrNull()
                HomeScreen(
                    profile = p,
                    season = season,
                    activeTaskTitle = activeTask?.title,
                    goalTitle = goal?.title,
                    goalProgress = goal?.let {
                        "Накоплено ${p.savingsBalance} из ${it.targetAmount}"
                    },
                    vm = vm,
                    onNavigate = { nav.navigate(it) }
                )
            }
        }
        composable(Routes.BUDGET) {
            profile?.let { BudgetScreen(it, vm) { nav.popBackStack() } }
        }
        composable(Routes.WORK) {
            profile?.let { WorkScreen(vm.content.works, it, vm) { nav.popBackStack() } }
        }
        composable(Routes.SHOP) {
            ShopScreen(vm.content.shopItems, vm) { nav.popBackStack() }
        }
        composable(Routes.CLOTHING) {
            profile?.let {
                ClothingScreen(season, vm.content.clothing, it, vm) { nav.popBackStack() }
            }
        }
        composable(Routes.SAVINGS) {
            profile?.let { SavingsScreen(it, vm.content.goals, vm) { nav.popBackStack() } }
        }
        composable(Routes.TASKS) {
            profile?.let {
                TasksScreen(vm.content.tasks, it, onOpen = { id -> nav.navigate(Routes.task(id)) }) {
                    nav.popBackStack()
                }
            }
        }
        composable(
            Routes.TASK_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("taskId")
            val task = vm.content.tasks.find { it.id == id }
            if (task != null) TaskDetailScreen(task, vm) { nav.popBackStack() }
        }
        composable(Routes.LESSONS) {
            profile?.let {
                LessonsScreen(vm.content.lessons, it, onOpen = { id -> nav.navigate(Routes.lesson(id)) }) {
                    nav.popBackStack()
                }
            }
        }
        composable(
            Routes.LESSON_DETAIL,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("lessonId")
            val lesson = vm.content.lessons.find { it.id == id }
            if (lesson != null) LessonDetailScreen(lesson, vm) { nav.popBackStack() }
        }
        composable(Routes.PROGRESS) {
            profile?.let { ProgressScreen(it) { nav.popBackStack() } }
        }
        composable(Routes.SETTINGS) {
            profile?.let {
                SettingsScreen(it, season, vm, onBack = { nav.popBackStack() }, onResetDone = {
                    nav.navigate(Routes.ROLE) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
        composable(Routes.ADULT_GATE) {
            AdultGateScreen(
                onPassed = {
                    vm.loadParent()
                    nav.navigate(Routes.PARENT)
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.PARENT) {
            ParentScreen(
                dashboard = parent,
                onReset = {
                    vm.resetProfile {
                        nav.navigate(Routes.ROLE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
    }
    }
}
