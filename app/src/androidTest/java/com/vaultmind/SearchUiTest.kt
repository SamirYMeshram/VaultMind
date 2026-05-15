package com.vaultmind

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class SearchUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun searchScreenIsReachable() { rule.onNodeWithText("Search").performClick(); rule.onNodeWithText("Offline search").assertIsDisplayed() }
}
