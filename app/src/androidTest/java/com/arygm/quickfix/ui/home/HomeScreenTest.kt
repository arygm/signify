package com.arygm.quickfix.ui.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    navigationActions = mock(NavigationActions::class.java)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
  }

  @Test
  fun homeScreenDisplaysCorrectly() {
    composeTestRule.setContent { HomeScreen(navigationActions) }

    composeTestRule.onNodeWithTag("TopAppBarSurface").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TopAppBarTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HomeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("HomeText").assertTextEquals("HOME")
    composeTestRule.onNodeWithTag("ProfileButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("MessagesButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MessagesButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("HomeContent").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("WelcomeText").assertTextContains("Welcome to the Home Screen")
    composeTestRule.onNodeWithTag("BottomNavMenu").assertIsDisplayed()
  }

  // TODO When we add the screen implementations
  @Test
  fun profileIconClickTest() {
    composeTestRule.setContent { HomeScreen(navigationActions) }
    composeTestRule.onNodeWithTag("ProfileButton").performClick()
    Mockito.verify(navigationActions).navigateTo(Screen.PROFILE)
  }

  // TODO When we add button logic
  @Test
  fun messagesIconClickTest() {
    composeTestRule.setContent { HomeScreen(navigationActions) }
    composeTestRule.onNodeWithTag("MessagesButton").performClick()
    Mockito.verify(navigationActions).navigateTo(Screen.MESSAGES)
  }
}