package ru.finny.pet.domain.economy

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import ru.finny.pet.domain.model.BudgetPlan
import ru.finny.pet.domain.model.ChildProfile
import ru.finny.pet.domain.model.EconomyConstants
import ru.finny.pet.domain.model.PetSpecies
import ru.finny.pet.domain.model.PetStats
import ru.finny.pet.domain.model.Season
import ru.finny.pet.domain.model.WorkDifficulty

class GameEconomyTest {

    private fun profile(
        balance: Int = 500,
        energy: Int = 80,
        sick: Boolean = false
    ) = ChildProfile(
        childCode = "FINTEST",
        petName = "Тест",
        species = PetSpecies.FOX,
        balance = balance,
        stats = PetStats(energy = energy, isIll = sick, lastFedPeriod = 1, lastEntertainmentPeriod = 1)
    )

    @Test
    fun budgetPlan_rejectsOverspend() {
        val v = GameEconomy.validateBudgetPlan(400, BudgetPlan(200, 150, 100))
        assertThat(v.ok).isFalse()
        assertThat(v.leftover).isLessThan(0)
    }

    @Test
    fun budgetPlan_acceptsExact() {
        val v = GameEconomy.validateBudgetPlan(400, BudgetPlan(200, 100, 100))
        assertThat(v.ok).isTrue()
        assertThat(v.leftover).isEqualTo(0)
    }

    @Test
    fun dailyLogin_adds100Once() {
        val (u, fb) = GameEconomy.applyDailyLogin(profile())
        assertThat(u.balance).isEqualTo(600)
        assertThat(fb.balanceDelta).isEqualTo(EconomyConstants.DAILY_LOGIN)
        val (_, fb2) = GameEconomy.applyDailyLogin(u)
        assertThat(fb2.isPositive).isFalse()
    }

    @Test
    fun work_blockedWhenSick() {
        val (ok, _) = GameEconomy.canWork(profile(sick = true))
        assertThat(ok).isFalse()
    }

    @Test
    fun work_easyPays100() {
        val (u, fb) = GameEconomy.applyWork(profile(), WorkDifficulty.EASY)
        assertThat(u.balance).isEqualTo(600)
        assertThat(fb.balanceDelta).isEqualTo(100)
        assertThat(u.stats.energy).isEqualTo(60)
    }

    @Test
    fun feed_costs100_gainsHunger() {
        val (u, _) = GameEconomy.feed(profile(balance = 200))
        assertThat(u.balance).isEqualTo(100)
        assertThat(u.stats.hunger).isEqualTo(100)
    }

    @Test
    fun savings_minTransfer50() {
        val (_, fb) = GameEconomy.transferToSavings(profile(), 40)
        assertThat(fb.isPositive).isFalse()
        val (u, fb2) = GameEconomy.transferToSavings(profile(), 50)
        assertThat(fb2.isPositive).isTrue()
        assertThat(u.savingsBalance).isEqualTo(50)
    }

    @Test
    fun tax_onDemoPeriod5() {
        val p = profile(balance = 200).copy(demoMode = true, periodIndex = 5)
        val (u, fb) = GameEconomy.applyTaxIfNeeded(p)
        assertThat(fb).isNotNull()
        assertThat(u.balance).isEqualTo(50)
    }

    @Test
    fun baseOutfitCosts() {
        assertThat(GameEconomy.baseOutfitCost(Season.SUMMER)).isEqualTo(240)
        assertThat(GameEconomy.baseOutfitCost(Season.WINTER)).isEqualTo(700)
    }

    @Test
    fun estimatePeriodsToGoal() {
        assertThat(GameEconomy.estimatePeriodsToGoal(0, 1000, 50)).isEqualTo(20)
        assertThat(GameEconomy.estimatePeriodsToGoal(1000, 1000, 50)).isEqualTo(0)
    }
}
