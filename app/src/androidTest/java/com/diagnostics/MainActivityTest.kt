package com.diagnostics

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesSuccessfully() {
        composeTestRule.onNodeWithText("السجلات").assertIsDisplayed()
        composeTestRule.onNodeWithText("الماسح").assertIsDisplayed()
        composeTestRule.onNodeWithText("التشخيص").assertIsDisplayed()
        composeTestRule.onNodeWithText("الإعدادات").assertIsDisplayed()
    }

    @Test
    fun navigateToScannerTab() {
        composeTestRule.onNodeWithText("الماسح").performClick()
        composeTestRule.onNodeWithText("امسح بورد iPhone").assertIsDisplayed()
    }

    @Test
    fun navigateToSettingsTab() {
        composeTestRule.onNodeWithText("الإعدادات").performClick()
        composeTestRule.onNodeWithText("إعدادات الذكاء الاصطناعي").assertIsDisplayed()
    }
}
