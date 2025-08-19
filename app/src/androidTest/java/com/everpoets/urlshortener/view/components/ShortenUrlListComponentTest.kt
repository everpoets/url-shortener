package com.everpoets.urlshortener.view.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import com.everpoets.urlshortener.ShortenUiState
import com.everpoets.urlshortener.data.ShortenUrlModel
import org.junit.Rule
import org.junit.Test

class ShortenUrlListComponentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shortenUrlList_initialState() {
        val uiState = ShortenUiState()

        composeTestRule.setContent {
            ShortenUrlList(uiState = uiState)
        }

        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsNotDisplayed()
    }

    @Test
    fun shortenUrlList_loadingState() {
        val uiState = ShortenUiState(
            isLoading = true
        )

        composeTestRule.setContent {
            ShortenUrlList(uiState = uiState)
        }

        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsNotDisplayed()
    }

    @Test
    fun shortenUrlList_listState() {
        val uiState = ShortenUiState(
            list = listOf(
                ShortenUrlModel(
                    alias = "abc",
                    shortLink = "short",
                    selfLink = "self"
                )
            )
        )

        composeTestRule.setContent {
            ShortenUrlList(uiState = uiState)
        }

        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsNotDisplayed()

        composeTestRule.onNodeWithTag(LIST_STATE_TAG).performScrollToNode(hasText("short"))
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).performScrollToNode(hasText("self"))
    }

    @Test
    fun shortenUrlList_loadingState_with_listState() {
        val uiState = ShortenUiState(
            isLoading = true,
            list = listOf(
                ShortenUrlModel(
                    alias = "abc",
                    shortLink = "short",
                    selfLink = "self"
                )
            )
        )

        composeTestRule.setContent {
            ShortenUrlList(uiState = uiState)
        }

        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsNotDisplayed()
    }

    @Test
    fun shortenUrlList_listState_multipleItems_scroll() {
        val uiState = ShortenUiState(
            list = listOf(
                ShortenUrlModel(
                    alias = "abc1",
                    shortLink = "short1",
                    selfLink = "self1"
                ),ShortenUrlModel(
                    alias = "abc2",
                    shortLink = "short2",
                    selfLink = "self2"
                ),ShortenUrlModel(
                    alias = "abc3",
                    shortLink = "short3",
                    selfLink = "self3"
                ),ShortenUrlModel(
                    alias = "abc4",
                    shortLink = "short4",
                    selfLink = "self4"
                ),ShortenUrlModel(
                    alias = "abc5",
                    shortLink = "short5",
                    selfLink = "self5"
                ),ShortenUrlModel(
                    alias = "abc6",
                    shortLink = "short6",
                    selfLink = "self6"
                ),ShortenUrlModel(
                    alias = "abc7",
                    shortLink = "short7",
                    selfLink = "self7"
                ),ShortenUrlModel(
                    alias = "abc8",
                    shortLink = "short8",
                    selfLink = "self8"
                ),ShortenUrlModel(
                    alias = "abc9",
                    shortLink = "short9",
                    selfLink = "self9"
                ),ShortenUrlModel(
                    alias = "abc10",
                    shortLink = "short10",
                    selfLink = "self10"
                ),
            )
        )

        composeTestRule.setContent {
            ShortenUrlList(uiState = uiState)
        }

        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsNotDisplayed()

        composeTestRule.onNodeWithTag(LIST_STATE_TAG).performScrollToNode(hasText("self10"))
    }
}