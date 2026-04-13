package com.expensetracker

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigation_isDisplayed() {
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Stats").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun fabButton_isDisplayed_onHomeScreen() {
        composeRule.onNodeWithContentDescription("Add transaction").assertIsDisplayed()
    }

    @Test
    fun periodSelector_chips_areDisplayed() {
        composeRule.onNodeWithText("Day").assertIsDisplayed()
        composeRule.onNodeWithText("Week").assertIsDisplayed()
        composeRule.onNodeWithText("Month").assertIsDisplayed()
        composeRule.onNodeWithText("Year").assertIsDisplayed()
    }

    @Test
    fun periodSelector_selectWeek_updates() {
        composeRule.onNodeWithText("Week")
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Week")
            .assertIsSelected()
    }

    @Test
    fun navigateTo_stats_screen() {
        composeRule.onNodeWithText("Stats").performClick()
        composeRule.onNodeWithText("Statistics").assertIsDisplayed()
    }

    @Test
    fun navigateTo_settings_screen() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Display Currency").assertIsDisplayed()
    }

    @Test
    fun fab_click_opens_addTransaction_screen() {
        composeRule.onNodeWithContentDescription("Add transaction").performClick()
        composeRule.onNodeWithText("Add Transaction").assertIsDisplayed()
    }

    @Test
    fun addTransaction_screen_hasIncomeExpenseToggle() {
        composeRule.onNodeWithContentDescription("Add transaction").performClick()
        composeRule.onNodeWithText("Expense").assertIsDisplayed()
        composeRule.onNodeWithText("Income").assertIsDisplayed()
    }

    @Test
    fun addTransaction_screen_hasSaveButton() {
        composeRule.onNodeWithContentDescription("Add transaction").performClick()
        composeRule.onNodeWithText("Save Transaction").assertIsDisplayed()
    }

    @Test
    fun addTransaction_amountField_acceptsInput() {
        composeRule.onNodeWithContentDescription("Add transaction").performClick()
        composeRule.onNodeWithText("Amount").performTextInput("42.50")
        composeRule.onNodeWithText("42.50").assertIsDisplayed()
    }

    @Test
    fun addTransaction_backButton_navigatesBack() {
        composeRule.onNodeWithContentDescription("Add transaction").performClick()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithContentDescription("Add transaction").assertIsDisplayed()
    }

    @Test
    fun settings_currencyRefreshButton_isDisplayed() {
        composeRule.onNodeWithContentDescription("Settings")
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("currency_refresh_button")
            .assertIsDisplayed()
    }

    @Test
    fun settings_notificationsToggle_isDisplayed() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Budget & Rate Alerts").assertIsDisplayed()
    }
}
