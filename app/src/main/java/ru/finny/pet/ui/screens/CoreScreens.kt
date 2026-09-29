package ru.finny.pet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.LessonDefinition
import ru.finny.pet.domain.model.PetSpecies
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.TaskDefinition
import ru.finny.pet.ui.FinnyViewModel
import ru.finny.pet.ui.components.CoinBadge
import ru.finny.pet.ui.components.FeedbackCard
import ru.finny.pet.ui.components.FinnyPrimaryButton
import ru.finny.pet.ui.components.FinnySecondaryButton
import ru.finny.pet.ui.components.LocalFinnyContent
import ru.finny.pet.ui.components.PetAvatar
import ru.finny.pet.ui.components.ScreenHeader
import ru.finny.pet.ui.components.StatBar
import ru.finny.pet.ui.theme.FinnyCloud
import ru.finny.pet.ui.theme.FinnyCoral
import ru.finny.pet.ui.theme.FinnyGrass
import ru.finny.pet.ui.theme.FinnySky
import ru.finny.pet.ui.theme.FinnySun

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    val contentRepo = LocalFinnyContent.current
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(FinnyCloud)
    ) {
        val bg = remember(contentRepo) { contentRepo?.backgroundImage() }
        if (bg != null) {
            Image(
                bitmap = bg,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(FinnySky.copy(alpha = 0.55f), FinnySun.copy(alpha = 0.35f), FinnyGrass.copy(alpha = 0.4f))
                        )
                    )
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.28f))
        )
        content()
    }
}

@Composable
fun GradientBg(content: @Composable () -> Unit) = AppBackground(content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinnySky.copy(alpha = 0.4f))
            )
        },
        content = content
    )
}

@Composable
fun RoleSelectScreen(onChild: () -> Unit, onParent: () -> Unit) {
    GradientBg {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ФинПет", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Играем и учимся считать монеты — без настоящих денег.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            FinnyPrimaryButton("Я ребёнок", onClick = onChild, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            FinnySecondaryButton("Я взрослый", onClick = onParent, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    GradientBg {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Знакомство", style = MaterialTheme.typography.displayLarge)
            Text(
                "Финни помогает понять бюджет. У тебя будут игровые монеты — не настоящие деньги.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("Три типа решений:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("1. Потратить на обязательное (еда, налог, одежда сезона).")
            Text("2. Потратить на желаемое (игрушки, цвета одежды).")
            Text("3. Отложить в копилку на цель.")
            Text("Расходы не должны быть больше доходов. Взрослый может смотреть прогресс по коду.")
            FinnyPrimaryButton("Создать питомца", onClick = onContinue, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun CreatePetScreen(onCreated: (String, PetSpecies) -> Unit) {
    var name by remember { mutableStateOf("Финни") }
    var selected by remember { mutableStateOf(PetSpecies.FOX) }
    GradientBg {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            ScreenHeader("Выбери питомца", "9 разных друзей — выбери своего!")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(12) },
                label = { Text("Имя питомца") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PetSpecies.entries) { sp ->
                    Column(
                        Modifier
                            .background(
                                if (sp == selected) FinnySun else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selected = sp }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(sp.emoji, fontSize = 40.sp)
                        Text(sp.displayName, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            FinnyPrimaryButton(
                "Начать!",
                onClick = { onCreated(name, selected) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ParentLoginScreen(
    onSuccess: (String) -> Unit,
    onBack: () -> Unit,
    onFailMessage: String?,
    check: (String, () -> Unit, () -> Unit) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(onFailMessage) }
    SimpleScaffold("Вход для взрослого", onBack = onBack) { pad ->
        Column(Modifier.padding(pad).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Введи код ребёнка из настроек (например FIN7K2). Данные только на этом устройстве.")
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(12) },
                label = { Text("Код") },
                modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = FinnyCoral) }
            FinnyPrimaryButton("Открыть", onClick = {
                check(code, { onSuccess(code) }, { error = "Код не найден. Проверь буквы." })
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun HomeScreen(
    profile: ChildProfile,
    season: Season,
    activeTaskTitle: String?,
    goalTitle: String?,
    goalProgress: String?,
    vm: FinnyViewModel,
    onNavigate: (String) -> Unit
) {
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()

    val activeWork = profile.activeWorkId?.let { id -> vm.content.works.find { it.id == id } }
    GradientBg {
        LazyColumn(
            Modifier.fillMaxSize(),

            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("ФинПет", style = MaterialTheme.typography.headlineMedium)
                Text("Период ${profile.periodIndex} · ${season.displayName}", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                PetAvatar(
                    profile = profile,
                    animationsEnabled = profile.animationsEnabled,
                    activeWorkImageFile = activeWork?.imageAssetPath,
                    activeWorkTitle = activeWork?.title
                )
            }
            item { CoinBadge(profile.balance, profile.savingsBalance) }
            item {
                Text("Цель: ${goalTitle ?: "—"}", fontWeight = FontWeight.SemiBold)
                goalProgress?.let { Text(it) }
            }
            item {
                StatBar("Голод", profile.stats.hunger, FinnyCoral)
                Spacer(Modifier.height(8.dp))
                StatBar("Энергия", profile.stats.energy, FinnySky)
                Spacer(Modifier.height(8.dp))
                StatBar("Радость", profile.stats.joy, FinnySun)
            }
            item {
                Text(
                    "Задание: ${activeTaskTitle ?: "Выбери в разделе «Задания»"}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            item { FeedbackCard(feedback, onDismiss = vm::dismissFeedback) }
            item {
                val links = listOf(
                    "План бюджета" to "budget",
                    "Работа" to "work",
                    "Покупки" to "shop",
                    "Одежда" to "clothing",
                    "Накопления" to "savings",
                    "Задания" to "tasks",
                    "Уроки" to "lessons",
                    "Прогресс" to "progress",
                    "Настройки" to "settings",
                    "Для взрослого" to "adult_gate"
                )
                links.forEach { (label, route) ->
                    FinnyPrimaryButton(label, onClick = { onNavigate(route) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                }
                FinnySecondaryButton("Забрать вход (+100)", onClick = vm::claimDaily, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                if (!profile.stats.isIll) {

                    val nowMs = System.currentTimeMillis()
                    val canActNow = !profile.isWorking(nowMs) && !profile.isSleeping(nowMs)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FinnyPrimaryButton(
                            "🍎 Покормить (${EconomyConstants.FOOD_PRICE})",
                            enabled = canActNow && profile.balance >= EconomyConstants.FOOD_PRICE,
                            onClick = { vm.buy("food") },
                            modifier = Modifier.weight(1f)
                        )
                        FinnySecondaryButton(
                            text = "🎾 Поиграть (${EconomyConstants.ENTERTAINMENT_PRICE})",
                            onClick = { if (canActNow) vm.buy("entertainment") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (profile.stats.isIll) {

                    val canBuyMedicine = profile.balance >= EconomyConstants.MEDICINE_PRICE
                    FinnyPrimaryButton(
                        text = if (canBuyMedicine) "💊 Купить лекарство (${EconomyConstants.MEDICINE_PRICE})"
                               else "💊 Лекарство: не хватает ${EconomyConstants.MEDICINE_PRICE - profile.balance}",
                        enabled = canBuyMedicine,
                        onClick = { vm.buy("medicine") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    FinnySecondaryButton("Спать (8 часов сна)", onClick = vm::sleep, modifier = Modifier.fillMaxWidth())
                }
                if (profile.demoMode) {
                    Spacer(Modifier.height(8.dp))
                    FinnyPrimaryButton("Завершить период (демо)", onClick = { vm.advancePeriod() }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateWithLifecycleSafe(): androidx.compose.runtime.State<T> {
    return this.collectAsState()
}
