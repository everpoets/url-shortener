package com.everpoets.urlshortener.view.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class InputTextButtonComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun inputTextButton_initialState() {
        composeTestRule.setContent {
            InputTextButton(
                modifier = Modifier,
                onClick = { }
            )
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).assert(hasText(""))
        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun inputTextButton_enabledButton() {
        composeTestRule.setContent {
            InputTextButton(
                modifier = Modifier,
                onClick = { }
            )
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).performTextInput("test")
        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun inputTextButton_enabledButton_clearInputText() {
        composeTestRule.setContent {
            InputTextButton(
                modifier = Modifier,
                onClick = { }
            )
        }

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).performTextInput("test")
        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsEnabled()
        composeTestRule.onNodeWithTag(BUTTON_TAG).performClick()
        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).assert(hasText(""))
    }
}