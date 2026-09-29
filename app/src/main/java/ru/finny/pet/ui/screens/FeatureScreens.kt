package ru.finny.pet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finny.pet.domain.economy.GameEconomy
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.ClothingItemDefinition
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.LessonDefinition
import ru.finny.pet.domain.model.ParentDashboard
import ru.finny.pet.domain.model.SavingsGoal
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.ShopItemDefinition
import ru.finny.pet.domain.model.TaskDefinition
import ru.finny.pet.domain.model.WorkDefinition
import ru.finny.pet.ui.FinnyViewModel
import ru.finny.pet.ui.components.FeedbackCard
import ru.finny.pet.ui.components.FinnyPrimaryButton
import ru.finny.pet.ui.components.FinnySecondaryButton
import ru.finny.pet.ui.components.ScreenHeader
import ru.finny.pet.ui.components.WorkImage
import ru.finny.pet.ui.components.formatMinutes
import ru.finny.pet.ui.components.rememberCurrentTimeMillis
import ru.finny.pet.ui.theme.FinnyDeep
import ru.finny.pet.ui.theme.FinnyGrass
import ru.finny.pet.ui.theme.FinnySun
import kotlin.math.roundToInt

@Composable
fun BudgetScreen(profile: ChildProfile, vm: FinnyViewModel, onBack: () -> Unit) {
    val balance = profile.balance
    var mandatory by remember { mutableFloatStateOf(profile.currentPlan?.mandatory?.toFloat() ?: (balance * 0.5f)) }
    var optional by remember { mutableFloatStateOf(profile.currentPlan?.optional?.toFloat() ?: (balance * 0.25f)) }
    var savings by remember { mutableFloatStateOf(profile.currentPlan?.savings?.toFloat() ?: (balance * 0.25f)) }
    val m = mandatory.roundToInt()
    val o = optional.roundToInt()
    val s = savings.roundToInt()
    val validation = GameEconomy.validateBudgetPlan(balance, ru.finny.pet.domain.model.BudgetPlan(m, o, s))
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()

    SimpleScaffold("План бюджета", onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader("Распредели монеты", "Баланс: $balance. Сумма плана ≤ бюджет.")
            Text("Обязательные: $m")
            Slider(value = mandatory, onValueChange = { mandatory = it }, valueRange = 0f..balance.toFloat())
            Text("Необязательные: $o")
            Slider(value = optional, onValueChange = { optional = it }, valueRange = 0f..balance.toFloat())
            Text("Накопления: $s")
            Slider(value = savings, onValueChange = { savings = it }, valueRange = 0f..balance.toFloat())
            Text(validation.message, fontWeight = FontWeight.SemiBold)
            profile.lastFact?.let {
                Text("Прошлый факт: обяз. ${it.mandatorySpent}, необяз. ${it.optionalSpent}, накоп. ${it.savedAmount}")
            }
            FeedbackCard(feedback, vm::dismissFeedback)
            FinnyPrimaryButton(
                "Подтвердить план",
                enabled = validation.ok,
                onClick = { vm.setPlan(m, o, s) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WorkScreen(works: List<WorkDefinition>, profile: ChildProfile, vm: FinnyViewModel, onBack: () -> Unit) {
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    val (can, reason) = GameEconomy.canWork(profile)
    val now = rememberCurrentTimeMillis()
    SimpleScaffold("Работа", onBack) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ScreenHeader(
                    "Выбери работу",
                    if (can) "Каждая работа длится 8 реальных часов и является одноразовой." else reason
                )
                FeedbackCard(feedback, vm::dismissFeedback)
            }
            items(works) { w ->

                val done = profile.completedWorkIds.contains(w.id)
                val isActive = profile.activeWorkId == w.id && now < profile.workEndsAtEpochMs
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (done) MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box {

                        WorkImage(
                            fileName = w.imageAssetPath,
                            contentDescription = w.title,
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer { alpha = if (done) 0.35f else 1f }
                        )
                        if (done) {
                            Box(
                                Modifier.size(72.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✔️", fontSize = 28.sp)
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            w.title,
                            fontWeight = FontWeight.Bold,
                            color = if (done) Color.Gray else Color.Unspecified
                        )
                        Text(
                            w.description,
                            color = if (done) Color.Gray else Color.Unspecified
                        )
                        Text(
                            if (done) "Выполнено · оплата была ${w.difficulty.pay} · сложность: ${w.difficulty.displayName}"
                            else "Оплата ${w.difficulty.pay} · сложность: ${w.difficulty.displayName}",
                            color = if (done) Color.Gray else Color.Unspecified
                        )
                        when {
                            done -> Text(
                                "Одноразовая работа — заблокирована",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            isActive -> Text(
                                "Смена идёт: осталось ${formatMinutes(profile.remainingMinutes(now))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinnyDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                            else -> FinnyPrimaryButton(
                                "Работать",
                                enabled = can,
                                onClick = { vm.work(w.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopScreen(items: List<ShopItemDefinition>, vm: FinnyViewModel, onBack: () -> Unit) {
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    SimpleScaffold("Покупки", onBack) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                ScreenHeader("Магазин", "Обязательные и необязательные расходы подписаны.")
                FeedbackCard(feedback, vm::dismissFeedback)
            }
            items(items) { item ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text(item.description)
                    Text("${item.price} монет · ${if (item.category.name == "MANDATORY") "обязательно" else "по желанию"}")
                    FinnyPrimaryButton("Купить", onClick = { vm.buy(item.id) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun ClothingScreen(
    season: Season,
    items: List<ClothingItemDefinition>,
    profile: ChildProfile,
    vm: FinnyViewModel,
    onBack: () -> Unit
) {
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    val seasonal = items.filter { it.season == season }
    SimpleScaffold("Одежда · ${season.displayName}", onBack) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                ScreenHeader(
                    "Гардероб",
                    "База обязательна (комплект ~${GameEconomy.baseOutfitCost(season)}). Цвета — по желанию (+10 радости)."
                )
                FeedbackCard(feedback, vm::dismissFeedback)
            }
            items(seasonal) { item ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(item.title, fontWeight = FontWeight.Bold)
                    Text("База: ${item.basePrice}")
                    FinnyPrimaryButton("Купить базу", onClick = { vm.buyClothes(item.id) }, modifier = Modifier.fillMaxWidth())
                    item.variants.filter { !it.isBase }.forEach { v ->
                        val owned = profile.wardrobe.any { it.variantId == v.id }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${v.title} (${v.price})")
                            if (owned) {
                                FinnySecondaryButton("Надеть", onClick = { vm.equip(item.id, v.id) })
                            } else {
                                FinnySecondaryButton("Купить", onClick = { vm.buyVariant(item.id, v.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsScreen(
    profile: ChildProfile,
    goals: List<SavingsGoal>,
    vm: FinnyViewModel,
    onBack: () -> Unit
) {
    var amount by remember { mutableIntStateOf(EconomyConstants.MIN_SAVINGS_TRANSFER) }
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    val active = goals.find { it.id == profile.activeGoalId } ?: goals.firstOrNull()
    val savedToward = profile.savingsBalance.coerceAtMost(active?.targetAmount ?: 0)
    val eta = GameEconomy.estimatePeriodsToGoal(
        profile.savingsBalance,
        active?.targetAmount ?: 1000,
        EconomyConstants.MIN_SAVINGS_TRANSFER
    )

    SimpleScaffold("Накопления", onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader("Копилка", "В копилке: ${profile.savingsBalance}. Минимум перевода: 50.")
            goals.forEach { g ->
                val progress = (profile.savingsBalance.toFloat() / g.targetAmount).coerceIn(0f, 1f)
                Text("${g.title}: цель ${g.targetAmount}, остаток ${(g.targetAmount - profile.savingsBalance).coerceAtLeast(0)}")
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp))
            }
            Text("Ориентировочный срок активной цели: ~$eta периодов при +50 за раз.")
            Text("Сумма перевода: $amount")
            Slider(
                value = amount.toFloat(),
                onValueChange = { amount = it.roundToInt().coerceAtLeast(EconomyConstants.MIN_SAVINGS_TRANSFER) },
                valueRange = EconomyConstants.MIN_SAVINGS_TRANSFER.toFloat()..profile.balance.coerceAtLeast(EconomyConstants.MIN_SAVINGS_TRANSFER).toFloat()
            )
            FeedbackCard(feedback, vm::dismissFeedback)
            FinnyPrimaryButton("Отложить", onClick = { vm.saveMoney(amount) }, modifier = Modifier.fillMaxWidth())
            FinnySecondaryButton("Снять (сначала показать)", onClick = { vm.withdraw(amount, false) }, modifier = Modifier.fillMaxWidth())
            FinnyPrimaryButton("Подтвердить снятие", onClick = { vm.withdraw(amount, true) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TasksScreen(tasks: List<TaskDefinition>, profile: ChildProfile, onOpen: (String) -> Unit, onBack: () -> Unit) {
    SimpleScaffold("Задания", onBack) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { ScreenHeader("Игровые ситуации", "Выбор с последствиями и объяснением.") }
            items(tasks) { t ->
                val done = profile.completedTaskIds.contains(t.id)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(if (done) FinnyGrass.copy(0.3f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .clickable { onOpen(t.id) }
                        .padding(12.dp)
                ) {
                    Text(t.title, fontWeight = FontWeight.Bold)
                    Text(t.theme.displayName)
                    if (done) Text("Пройдено")
                }
            }
        }
    }
}

@Composable
fun TaskDetailScreen(task: TaskDefinition, vm: FinnyViewModel, onBack: () -> Unit) {
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    SimpleScaffold(task.title, onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(task.situation, style = MaterialTheme.typography.bodyLarge)
            FeedbackCard(feedback, vm::dismissFeedback)
            task.options.forEach { opt ->
                FinnyPrimaryButton(opt.text, onClick = { vm.completeTask(task.id, opt.id) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun LessonsScreen(lessons: List<LessonDefinition>, profile: ChildProfile, onOpen: (String) -> Unit, onBack: () -> Unit) {
    SimpleScaffold("Уроки", onBack) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { ScreenHeader("Теория + тест", "Хороший результат теста: +${EconomyConstants.GOOD_TEST_REWARD} монет.") }
            items(lessons) { l ->
                val done = profile.completedLessonIds.contains(l.id)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(if (done) FinnySun.copy(0.35f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .clickable { onOpen(l.id) }
                        .padding(12.dp)
                ) {
                    Text(l.title, fontWeight = FontWeight.Bold)
                    Text(l.theme.displayName)
                }
            }
        }
    }
}

@Composable
fun LessonDetailScreen(lesson: LessonDefinition, vm: FinnyViewModel, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    SimpleScaffold(lesson.title, onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (step == 0) {
                Text(lesson.theoryText, style = MaterialTheme.typography.bodyLarge)
                FinnyPrimaryButton("К тесту", onClick = { step = 1 }, modifier = Modifier.fillMaxWidth())
            } else {
                val qIndex = step - 1
                if (qIndex < lesson.testQuestions.size) {
                    val q = lesson.testQuestions[qIndex]
                    Text(q.text, fontWeight = FontWeight.Bold)
                    Text("Мини-действие: ${q.miniActionHint}")
                    q.options.forEachIndexed { idx, text ->
                        FinnyPrimaryButton(text, onClick = {
                            if (idx == q.correctIndex) correct++
                            step++
                        }, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    FeedbackCard(feedback, vm::dismissFeedback)
                    FinnyPrimaryButton(
                        "Проверить результат",
                        onClick = { vm.completeLesson(lesson.id, correct, lesson.testQuestions.size) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Правильных ответов: $correct из ${lesson.testQuestions.size}")
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(profile: ChildProfile, onBack: () -> Unit) {
    SimpleScaffold("Прогресс", onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Стадия: ${profile.stage.displayName}", fontWeight = FontWeight.Bold)
            Text(profile.stageReason)
            Text("Заданий пройдено: ${profile.completedTaskIds.size}")
            Text("Уроков пройдено: ${profile.completedLessonIds.size}")
            Text("Период: ${profile.periodIndex}")
            Text("Код для взрослого: ${profile.childCode}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsScreen(
    profile: ChildProfile,
    season: Season,
    vm: FinnyViewModel,
    onBack: () -> Unit,
    onResetDone: () -> Unit
) {
    var confirmReset by remember { mutableStateOf(false) }
    val feedback by vm.feedback.collectAsStateWithLifecycleSafe()
    SimpleScaffold("Настройки", onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Код ребёнка: ${profile.childCode}", fontWeight = FontWeight.Bold)
            Text("Звук: ${if (profile.soundEnabled) "вкл" else "выкл"} (заглушка прототипа)")
            Text("Анимации: ${if (profile.animationsEnabled) "вкл" else "выкл"}")
            FeedbackCard(feedback, vm::dismissFeedback)
            FinnyPrimaryButton(
                if (profile.demoMode) "Выключить демо" else "Включить демо-режим",
                onClick = { vm.setDemo(!profile.demoMode) },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Сезон (демо): ${season.displayName}")
            Season.entries.forEach { s ->
                FinnySecondaryButton(s.displayName, onClick = { vm.setSeason(s) }, modifier = Modifier.fillMaxWidth())
            }
            if (!confirmReset) {
                FinnyPrimaryButton("Сбросить тестовый профиль", onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth())
            } else {
                Text("Точно удалить все данные на устройстве?")
                FinnyPrimaryButton("Да, удалить", onClick = { vm.resetProfile(onResetDone) }, modifier = Modifier.fillMaxWidth())
                FinnySecondaryButton("Отмена", onClick = { confirmReset = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun AdultGateScreen(onPassed: () -> Unit, onBack: () -> Unit) {
    var holdMs by remember { mutableIntStateOf(0) }
    var a by remember { mutableIntStateOf(3) }
    var b by remember { mutableIntStateOf(4) }
    var answer by remember { mutableStateOf("") }
    SimpleScaffold("Раздел для взрослого", onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Барьер: реши пример или удержи кнопку 3 секунды.")
            Text("Сколько будет $a + $b?")
            OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Ответ") }, modifier = Modifier.fillMaxWidth())
            FinnyPrimaryButton("Проверить", onClick = {
                if (answer.toIntOrNull() == a + b) onPassed()
            }, modifier = Modifier.fillMaxWidth())
            FinnyPrimaryButton(
                "Удерживай: $holdMs / 3000 мс",
                onClick = {
                    holdMs += 1000
                    if (holdMs >= 3000) onPassed()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ParentScreen(dashboard: ParentDashboard?, onReset: () -> Unit, onBack: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    SimpleScaffold("Кабинет взрослого", onBack) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Цель приложения: навыки бюджета, обязательных/необязательных расходов, накоплений и оценки решений — в игре, без реальных денег.")
            if (dashboard == null) {
                Text("Профиль ребёнка на этом устройстве не найден.")
            } else {
                Text("Питомец: ${dashboard.petName} ${dashboard.species.emoji}")
                Text("Стадия: ${dashboard.stage.displayName}")
                Text("Баланс: ${dashboard.balance}, копилка: ${dashboard.savingsBalance}")
                Text("Темы: ${dashboard.completedThemes.joinToString { it.displayName }.ifBlank { "пока нет" }}")
                Text("Заданий: ${dashboard.completedTaskCount}, уроков: ${dashboard.completedLessonCount}")
                Text("Траты:", fontWeight = FontWeight.Bold)
                dashboard.recentSpends.take(15).forEach {
                    Text("Период ${it.period}: ${it.title} −${it.amount} (${it.category.name})")
                }
            }
            if (!confirm) {
                FinnyPrimaryButton("Сбросить / удалить профиль", onClick = { confirm = true }, modifier = Modifier.fillMaxWidth())
            } else {
                Text("Подтвердите удаление всех локальных данных.")
                FinnyPrimaryButton("Удалить", onClick = onReset, modifier = Modifier.fillMaxWidth())
                FinnySecondaryButton("Отмена", onClick = { confirm = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
