package com.vaultmind

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class DashboardUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun dashboardIsDisplayed() { rule.onNodeWithText("VaultMind").assertIsDisplayed() }
}
