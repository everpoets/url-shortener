package com.everpoets.urlshortener.view.components


import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.everpoets.urlshortener.R
import com.everpoets.urlshortener.ShortenUiState
import com.everpoets.urlshortener.data.ShortenUrlModel
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_initialState_showsEmptyState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessageSuccess = context.getString(R.string.url_shortened_successfully)
        val expectedMessageError = context.getString(R.string.error_shortening_url)
        val expectedMessageAlreadyExists = context.getString(R.string.url_already_exists)
        val expectedMessageNoInternet = context.getString(R.string.error_no_internet_connection)

        composeTestRule.setContent {
            HomeScreen(
                uiState = ShortenUiState(),
                dispatchAction = { }
            )
        }

        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsNotDisplayed()

        composeTestRule.onNodeWithText(expectedMessageSuccess).assertDoesNotExist()
        composeTestRule.onNodeWithText(expectedMessageError).assertDoesNotExist()
        composeTestRule.onNodeWithText(expectedMessageAlreadyExists).assertDoesNotExist()
        composeTestRule.onNodeWithText(expectedMessageNoInternet)
            .assertDoesNotExist()
    }

    @Test
    fun homeScreen_loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            HomeScreen(
                uiState = ShortenUiState(isLoading = true),
                dispatchAction = { }
            )
        }

        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsNotDisplayed()
    }

    @Test
    fun homeScreen_withUrlList_showsListAndHidesEmptyState() {
        val testUrls = listOf(
            ShortenUrlModel(
                alias = "abc123",
                shortLink = "https://short.ly/abc123",
                selfLink = "https://example.com/long-url"
            ),
            ShortenUrlModel(
                alias = "def456",
                shortLink = "https://short.ly/def456",
                selfLink = "https://another-example.com/another-long-url"
            )
        )

        composeTestRule.setContent {
            HomeScreen(

                uiState = ShortenUiState(list = testUrls),
                dispatchAction = { }
            )
        }

        composeTestRule.onNodeWithTag(LIST_STATE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EMPTY_STATE_TAG).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(LOADING_STATE_TAG).assertIsNotDisplayed()

        // Verify list content
        composeTestRule.onNodeWithText("https://short.ly/abc123").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://example.com/long-url").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://short.ly/def456").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://another-example.com/another-long-url").assertIsDisplayed()
    }

    @Test
    fun homeScreen_userInputInteraction_enablesButtonWhenTextEntered() {
        composeTestRule.setContent {
            HomeScreen(

                uiState = ShortenUiState(),
                dispatchAction = { }
            )
        }

        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsNotEnabled()

        composeTestRule.onNodeWithTag(TEXT_FIELD_TAG).performTextInput("https://example.com")

        composeTestRule.onNodeWithTag(BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun homeScreen_changesUserMessage_showsSnackbar() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.url_shortened_successfully)
        composeTestRule.setContent {
            HomeScreen(
                uiState = ShortenUiState(userMessage = R.string.url_shortened_successfully),
                dispatchAction = { }
            )
        }

        composeTestRule.onNodeWithText(expectedMessage).assertIsDisplayed()
    }
}