package com.arygm.quickfix.kaspresso

import android.util.Log
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.width
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arygm.quickfix.MainActivity
import com.arygm.quickfix.kaspresso.screen.WelcomeScreen
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.kaspersky.kaspresso.flakysafety.*
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.compose.node.element.ComposeScreen
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class MainActivityTest : TestCase() {

  private lateinit var navigationActions: NavigationActions
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setup() {
    // Initialize the navigationActions mock
    navigationActions = Mockito.mock(NavigationActions::class.java)
  }

  @Test
  fun shouldNotBeAbleToReg() = run {
    step("Set up the WelcomeScreen and transit to the register") {
      // Retry the action until it works with a timeout of 10 seconds
      ComposeScreen.onComposeScreen<WelcomeScreen>(composeTestRule) {
        registerButton {
          assertIsDisplayed()
          performClick()
          // Log the click action
          Log.d("TestLog", "Register button clicked")
        }
      }
      composeTestRule.mainClock.advanceTimeBy(1500L)
      composeTestRule.onNodeWithTag("firstNameInput").performTextInput("Ramy")
      composeTestRule.onNodeWithTag("lastNameInput").performTextInput("Hatimy")
      composeTestRule.onNodeWithTag("emailInput").performTextInput("hatimyramy@gmail.com")
      composeTestRule.onNodeWithTag("birthDateInput").performTextInput("28/10/2004")
      composeTestRule.onNodeWithTag("passwordInput").performTextInput("246890357Asefthuk")
      composeTestRule.onNodeWithTag("repeatPasswordInput").performTextInput("246890357Asefthuk")
      composeTestRule.onNodeWithTag("checkbox").performClick()
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        val buttonNode = composeTestRule.onAllNodesWithTag("registerButton")
        // Check if any button node is not enabled
        buttonNode.fetchSemanticsNodes().any { semanticsNode ->
          semanticsNode.config.getOrNull(SemanticsProperties.Disabled) != null
        }
      }
      composeTestRule.onNodeWithTag("registerButton").assertIsNotEnabled()
    }
  }

  @Test
  fun shouldBeAbleToLogin() = run {
    step("Set up the WelcomeScreen and transit to the register") {
      // Retry the action until it works with a timeout of 10 seconds
      ComposeScreen.onComposeScreen<WelcomeScreen>(composeTestRule) {
        loginButton {
          assertIsDisplayed()
          performClick()
          // Log the click action
        }
      }
      composeTestRule.mainClock.advanceTimeBy(1500L)
      composeTestRule.onNodeWithTag("inputEmail").performTextInput("main.activity@test.com")
      composeTestRule.onNodeWithTag("inputPassword").performTextInput("246890357Asefthuk")
      composeTestRule.onNodeWithTag("logInButton").assertIsEnabled()
      composeTestRule.onNodeWithTag("logInButton").performClick()
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithTag("BottomNavMenu").fetchSemanticsNodes().isNotEmpty()
      }
      composeTestRule.onRoot().printToLog("TAG")
      // Get the bounds of the node
      // Get the bounds of the BottomNavMenu
      val bounds = composeTestRule.onNodeWithTag("BottomNavMenu").getBoundsInRoot()

      // Extract width, height, and position
      val width = bounds.width
      val height = bounds.height
      var positionX = bounds.left
      var positionY = bounds.top

      // Log the calculated values for debugging
      Log.d(
          "DEBUG",
          "Bounds: width=$width, height=$height, positionX=$positionX, positionY=$positionY")

      onView(withText("Search")) // Match the TextView that has the text "Hello World"
          .perform(click())
      onView(withText("Dashboard")) // Match the TextView that has the text "Hello World"
          .perform(click())
      onView(withText("Profile")) // Match the TextView that has the text "Hello World"
          .perform(click())

      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodesWithTag("AccountconfigurationOption")
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      updateAccountConfigurationAndVerify(
          composeTestRule, "Ramy", "Hatimy", "17/10/2004", "Ramy Hatimy")

      updateAccountConfigurationAndVerify(
          composeTestRule, "Ramo", "Hatimy", "28/10/2004", "Ramo Hatimy")

      composeTestRule.onNodeWithTag("AccountconfigurationOption").performClick()
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithTag("birthDateInput").fetchSemanticsNodes().isNotEmpty()
      }
      composeTestRule.onNodeWithTag("birthDateInput").assertTextEquals("28/10/2004")
      composeTestRule.onNodeWithTag("goBackButton").performClick()

      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule
            .onAllNodesWithTag("SetupyourbusinessaccountOption")
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag("SetupyourbusinessaccountOption").performClick()

      composeTestRule.waitUntil(timeoutMillis = 5000) {
        composeTestRule.onAllNodesWithTag("goBackButton").fetchSemanticsNodes().isNotEmpty()
      }
      composeTestRule.onNodeWithTag("goBackButton").performClick()
    }
  }
}

private fun updateAccountConfigurationAndVerify(
    composeTestRule: ComposeTestRule,
    firstName: String,
    lastName: String,
    birthDate: String,
    expectedProfileName: String
) {
  // Click on account configuration option
  composeTestRule.onNodeWithTag("AccountconfigurationOption").performClick()

  // Wait until the first name input is visible
  composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule.onAllNodesWithTag("firstNameInput").fetchSemanticsNodes().isNotEmpty()
  }

  composeTestRule.onNodeWithTag("firstNameInput").performTextClearance()
  composeTestRule.onNodeWithTag("lastNameInput").performTextClearance()
  composeTestRule.onNodeWithTag("birthDateInput").performTextClearance()

  // Input first name
  composeTestRule.onNodeWithTag("firstNameInput").performTextInput(firstName)

  // Input last name
  composeTestRule.onNodeWithTag("lastNameInput").performTextInput(lastName)

  // Input birthdate
  composeTestRule.onNodeWithTag("birthDateInput").performTextInput(birthDate)

  // Click on save button
  composeTestRule.onNodeWithTag("SaveButton").performClick()

  composeTestRule.waitUntil(timeoutMillis = 5000) {
    composeTestRule
        .onAllNodesWithTag("AccountconfigurationOption")
        .fetchSemanticsNodes()
        .isNotEmpty()
  }

  composeTestRule.waitUntil(5000L) {
    val profileNode = composeTestRule.onAllNodesWithTag("ProfileName")
    // Check if there's at least one node with the expected text
    profileNode.fetchSemanticsNodes().any { semanticsNode ->
      val text = semanticsNode.config.getOrNull(SemanticsProperties.Text)?.joinToString()
      text == expectedProfileName
    }
  }
  // Verify that the profile name has been updated correctly
  composeTestRule.onNodeWithTag("ProfileName").assertTextEquals(expectedProfileName)
}
