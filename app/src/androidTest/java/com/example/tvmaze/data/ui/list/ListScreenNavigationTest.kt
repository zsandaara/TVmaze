package com.example.tvmaze.ui.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tvmaze.MainActivity
import com.example.tvmaze.navigation.NavGraph
import com.example.tvmaze.ui.theme.TvMazeTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ListScreenNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun clickOnShowShouldNavigateToDetailScreen() {
        hiltRule.inject()

        composeTestRule.setContent {
            TvMazeTheme {
                NavGraph()
            }
        }

        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule
                    .onNodeWithTag("shows_list", useUnmergedTree = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onAllNodesWithTag("show_card", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule
                    .onNodeWithTag("detail_screen", useUnmergedTree = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun emptySearchShouldShowEmptyStateMessage() {
        hiltRule.inject()

        composeTestRule.setContent {
            TvMazeTheme {
                NavGraph()
            }
        }

        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule
                    .onNodeWithTag("search_field", useUnmergedTree = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithTag("search_field", useUnmergedTree = true)
            .performTextInput("nonexistent_show_xyz")

        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule
                    .onNodeWithTag("empty_message", useUnmergedTree = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun showsListShouldBeDisplayedAfterLoading() {
        hiltRule.inject()

        composeTestRule.setContent {
            TvMazeTheme {
                NavGraph()
            }
        }

        composeTestRule.waitUntil(10000) {
            try {
                composeTestRule
                    .onNodeWithTag("shows_list", useUnmergedTree = true)
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}