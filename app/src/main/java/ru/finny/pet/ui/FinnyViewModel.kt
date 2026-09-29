package ru.finny.pet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.finny.pet.content.ContentRepository
import ru.finny.pet.di.AppContainer
import ru.finny.pet.domain.model.ActionFeedback
import ru.finny.pet.domain.model.BudgetPlan
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.ParentDashboard
import ru.finny.pet.domain.model.PetSpecies
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.repository.ProfileRepository
import ru.finny.pet.domain.usecase.GameInteractor

data class FinnyUiState(
    val profile: ChildProfile? = null,
    val feedback: ActionFeedback? = null,
    val parentDashboard: ParentDashboard? = null,
    val season: Season = Season.SUMMER,
    val loading: Boolean = true
)

class FinnyViewModel(
    private val game: GameInteractor,
    private val profiles: ProfileRepository,
    val content: ContentRepository
) : ViewModel() {

    val profileFlow: StateFlow<ChildProfile?> = profiles.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _feedback = MutableStateFlow<ActionFeedback?>(null)
    val feedback: StateFlow<ActionFeedback?> = _feedback.asStateFlow()

    private val _parent = MutableStateFlow<ParentDashboard?>(null)
    val parent: StateFlow<ParentDashboard?> = _parent.asStateFlow()

    private val _season = MutableStateFlow(Season.SUMMER)
    val season: StateFlow<Season> = _season.asStateFlow()

    init {
        viewModelScope.launch {
            refreshSeason()
        }

        viewModelScope.launch {
            while (true) {
                game.tick()
                kotlinx.coroutines.delay(60_000)
            }
        }
    }

    fun dismissFeedback() {
        _feedback.value = null
    }

    private fun show(fb: ActionFeedback) {
        _feedback.value = fb
    }

    fun createPet(name: String, species: PetSpecies, onDone: () -> Unit) {
        viewModelScope.launch {
            game.createChildProfile(name, species)
            refreshSeason()
            onDone()
        }
    }

    fun claimDaily() = act { game.claimDailyLogin() }
    fun setPlan(m: Int, o: Int, s: Int) = act { game.setBudgetPlan(BudgetPlan(m, o, s)) }
    fun work(id: String) = act { game.doWork(id) }
    fun buy(id: String) = act { game.buyShopItem(id) }
    fun buyClothes(id: String) = act { game.buyClothingBase(id) }
    fun buyVariant(itemId: String, variantId: String) = act { game.buyClothingVariant(itemId, variantId) }
    fun equip(itemId: String, variantId: String) = act { game.equipVariant(itemId, variantId) }
    fun sleep() = act { game.sleep() }
    fun saveMoney(amount: Int) = act { game.transferSavings(amount) }
    fun withdraw(amount: Int, confirmed: Boolean) = act { game.withdrawSavings(amount, confirmed) }
    fun completeTask(taskId: String, optionId: String) = act { game.completeTask(taskId, optionId) }
    fun completeLesson(lessonId: String, correct: Int, total: Int) =
        act { game.completeLessonTest(lessonId, correct, total) }

    fun advancePeriod(onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            show(game.advancePeriod())
            refreshSeason()
            onDone?.invoke()
        }
    }

    fun setDemo(enabled: Boolean) {
        viewModelScope.launch {
            game.setDemoMode(enabled)
            show(
                ActionFeedback(
                    if (enabled) "Демо включено" else "Демо выключено",
                    if (enabled) "5 периодов подряд без ожидания. Сезон можно менять вручную. Налог на 5-м периоде."
                    else "Обычный режим.",
                    isPositive = true
                )
            )
        }
    }

    fun setSeason(season: Season) {
        viewModelScope.launch {
            game.setManualSeason(season)
            _season.value = season
        }
    }

    fun resetProfile(onDone: () -> Unit) {
        viewModelScope.launch {
            game.resetToDefaults()
            _parent.value = null
            onDone()
        }
    }

    fun loadParent() {
        viewModelScope.launch {
            _parent.value = game.parentDashboard()
        }
    }

    fun checkParentCode(code: String, onOk: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            val p = game.findByChildCode(code)
            if (p != null) {
                _parent.value = game.parentDashboard()
                onOk()
            } else onFail()
        }
    }

    private fun act(block: suspend () -> ActionFeedback) {
        viewModelScope.launch {
            show(block())
            refreshSeason()
        }
    }

    private suspend fun refreshSeason() {
        _season.value = game.currentSeason()
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FinnyViewModel(
                    AppContainer.game,
                    AppContainer.profiles,
                    AppContainer.content
                ) as T
            }
        }
    }
}
