package com.vaultmind

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class CardDetailUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun createFlowOpensCardDetail() {
        rule.onNodeWithContentDescription("Create").performClick()
        rule.onNodeWithText("Title").performTextInput("UI test card")
        rule.onNodeWithText("Body / content").performTextInput("A saved Compose UI card")
        rule.onNodeWithText("Save card").performClick()
        rule.onNodeWithText("UI test card").assertIsDisplayed()
    }
}
