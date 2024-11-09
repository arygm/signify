package com.arygm.quickfix.ui.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AnnouncementScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    navigationActions = mock(NavigationActions::class.java)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SEARCH)
  }

  @Test
  fun announcementScreenUserDisplaysCorrectly() {
    composeTestRule.setContent { AnnouncementScreen(navigationActions, true) }

    composeTestRule.onNodeWithTag("AnnouncementContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AnnouncementText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AnnouncementText")
        .assertTextContains("Welcome to the Announcement Screen")
  }

  // Can Remove this tbh
  @Test
  fun announcementScreenWorkerDisplaysCorrectly() {
    composeTestRule.setContent { AnnouncementScreen(navigationActions, false) }

    composeTestRule.onNodeWithTag("AnnouncementContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AnnouncementText").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("AnnouncementText")
        .assertTextContains("Welcome to the Announcement Screen")
  }
}