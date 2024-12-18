package com.arygm.quickfix.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arygm.quickfix.model.account.AccountRepository
import com.arygm.quickfix.model.account.AccountViewModel
import com.arygm.quickfix.model.bill.BillField
import com.arygm.quickfix.model.bill.Units
import com.arygm.quickfix.model.locations.Location
import com.arygm.quickfix.model.messaging.*
import com.arygm.quickfix.model.offline.small.PreferencesRepositoryDataStore
import com.arygm.quickfix.model.offline.small.PreferencesViewModel
import com.arygm.quickfix.model.profile.dataFields.Service
import com.arygm.quickfix.model.quickfix.QuickFix
import com.arygm.quickfix.model.quickfix.QuickFixRepository
import com.arygm.quickfix.model.quickfix.QuickFixViewModel
import com.arygm.quickfix.model.quickfix.Status
import com.arygm.quickfix.model.switchModes.ModeViewModel
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.theme.QuickFixTheme
import com.arygm.quickfix.ui.uiMode.appContentUI.userModeUI.home.MessageScreen
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MessageUserNoModeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var chatRepository: ChatRepository
  private lateinit var quickFixRepository: QuickFixRepository
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var quickFixViewModel: QuickFixViewModel
  private lateinit var modeViewModel: ModeViewModel
  private lateinit var preferencesViewModel: PreferencesViewModel
  private lateinit var preferencesRepositoryDataStore: PreferencesRepositoryDataStore
  private lateinit var accountRepository: AccountRepository
  private lateinit var accountViewModel: AccountViewModel

  // Implémentation de test pour Service
  data class TestService(override val name: String) : Service

  private val appModeFlow = MutableStateFlow("USER")
  private val userIdFlow = MutableStateFlow("testUserId")

  private val yesterday = Timestamp(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
  private val today = Timestamp(Date(System.currentTimeMillis()))

  private val includedServices = listOf(TestService("Included Service 1"))
  private val addOnServices = listOf(TestService("Add-on Service 1"))

  private val billFields =
      listOf(
          BillField(
              description = "Labor", unit = Units.H, amount = 2.0, unitPrice = 40.0, total = 80.0))

  private val fakeLocation =
      Location(latitude = 40.7128, longitude = -74.0060, name = "Test Location")

  private val fakeQuickFixUid = "qf_123"
  private val fakeQuickFix =
      QuickFix(
          uid = fakeQuickFixUid,
          status = Status.PENDING,
          imageUrl = listOf("https://example.com/image1.jpg"),
          date = listOf(yesterday, today),
          time = today,
          includedServices = includedServices,
          addOnServices = addOnServices,
          workerId = "John the Worker",
          userId = "Jane the User",
          chatUid = "chat_123",
          title = "Fixing the Kitchen Sink",
          description = "The kitchen sink is clogged and needs fixing.",
          bill = billFields,
          location = fakeLocation)

  private val testUserId = "testUserId"
  private val fakeChat =
      Chat(
          chatId = "chat_123",
          chatStatus = ChatStatus.ACCEPTED,
          quickFixUid = fakeQuickFixUid,
          messages =
              listOf(
                  Message("msg_1", testUserId, "Hello!", Timestamp.now()),
                  Message("msg_2", "otherUserId", "Hi, how can I help you?", Timestamp.now())),
          useruid = "Jane the User",
          workeruid = "John the Worker")

  @Before
  fun setup() {
    navigationActions = mock(NavigationActions::class.java)
    chatRepository = mock(ChatRepository::class.java)
    quickFixRepository = mock(QuickFixRepository::class.java)
    modeViewModel = mock(ModeViewModel::class.java)
    preferencesRepositoryDataStore = mock(PreferencesRepositoryDataStore::class.java)
    preferencesViewModel = PreferencesViewModel(preferencesRepositoryDataStore)
    accountRepository = mock(AccountRepository::class.java)
    accountViewModel = AccountViewModel(accountRepository)

    // Mock repository init
    whenever(quickFixRepository.init(any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(0)
      onSuccess()
      null
    }

    // Mock userId and appMode preferences
    val userIdKey = stringPreferencesKey("user_id")
    whenever(preferencesRepositoryDataStore.getPreferenceByKey(userIdKey)).thenReturn(userIdFlow)

    val appModeKey = stringPreferencesKey("app_mode")
    whenever(preferencesRepositoryDataStore.getPreferenceByKey(appModeKey)).thenReturn(appModeFlow)

    runBlocking {
      // Mock getChats to return fakeChat
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(fakeChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      // Mock getQuickFixByUid to return fakeQuickFix
      doAnswer { invocation ->
            val uid = invocation.getArgument<String>(0)
            val onResult = invocation.getArgument<(QuickFix?) -> Unit>(1)
            if (uid == fakeQuickFixUid) {
              onResult(fakeQuickFix)
            } else {
              onResult(null)
            }
            null
          }
          .whenever(quickFixRepository)
          .getQuickFixById(any(), any(), any())
    }

    // Initialize ViewModels
    chatViewModel = ChatViewModel(chatRepository)
    quickFixViewModel = QuickFixViewModel(quickFixRepository)

    runBlocking {
      chatViewModel.getChats()
      quickFixViewModel.getQuickFixes()
    }

    chatViewModel.selectChat(fakeChat)
  }

  @Test
  fun testQuickFixDetailsAreDisplayed() {
    composeTestRule.setContent {
      QuickFixTheme {
        MessageScreen(
            chatViewModel = chatViewModel,
            navigationActions = navigationActions,
            quickFixViewModel = quickFixViewModel,
            preferencesViewModel = preferencesViewModel,
            accountViewModel = accountViewModel)
      }
    }

    composeTestRule.onNodeWithTag("quickFixDetails").assertIsDisplayed()
  }

  @Test
  fun testMessagesAreDisplayed() {
    composeTestRule.setContent {
      QuickFixTheme {
        MessageScreen(
            chatViewModel = chatViewModel,
            navigationActions = navigationActions,
            quickFixViewModel = quickFixViewModel,
            preferencesViewModel = preferencesViewModel,
            accountViewModel = accountViewModel)
      }
    }

    composeTestRule.onNodeWithText("Hello!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hi, how can I help you?").assertIsDisplayed()
  }

  @Test
  fun testAcceptedStatusShowsActiveConversationText() {
    composeTestRule.setContent {
      QuickFixTheme {
        MessageScreen(
            chatViewModel = chatViewModel,
            navigationActions = navigationActions,
            quickFixViewModel = quickFixViewModel,
            preferencesViewModel = preferencesViewModel,
            accountViewModel = accountViewModel)
      }
    }

    composeTestRule.onNodeWithText("Conversation is active. Start messaging!").assertIsDisplayed()
  }

  @Test
  fun testGettingSuggestionsShowsSuggestions() {
    runBlocking {
      val gettingSuggestionsChat = fakeChat.copy(chatStatus = ChatStatus.GETTING_SUGGESTIONS)
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(gettingSuggestionsChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      runBlocking {
        chatViewModel.getChats()
        chatViewModel.selectChat(gettingSuggestionsChat)
        quickFixViewModel.getQuickFixes()
      }

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      composeTestRule.onNodeWithText("How is it going?").assertIsDisplayed()
      composeTestRule.onNodeWithText("Is the time and day okay for you?").assertIsDisplayed()
      composeTestRule.onNodeWithText("I can’t wait to work with you!").assertIsDisplayed()
    }
  }

  @Test
  fun testWorkerRefusedStatusShowsRefusalMessage() {
    runBlocking {
      val refusedChat = fakeChat.copy(chatStatus = ChatStatus.WORKER_REFUSED)
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(refusedChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      runBlocking {
        chatViewModel.getChats()
        chatViewModel.selectChat(refusedChat)
        quickFixViewModel.getQuickFixes()
      }

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      composeTestRule
          .onNodeWithText(
              "John the Worker has rejected the QuickFix. No big deal! Contact another worker from the search screen! 😊")
          .assertIsDisplayed()
    }
  }

  @Test
  fun testSendingMessageWhenAcceptedWorks() {
    runBlocking {
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
          }
          .whenever(chatRepository)
          .sendMessage(any(), any(), any(), any())

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      composeTestRule.onNodeWithTag("messageTextField").performTextInput("Hello from test!")
      composeTestRule.onNodeWithTag("sendButton").performClick()

      verify(chatRepository)
          .sendMessage(eq(fakeChat), argThat { content == "Hello from test!" }, any(), any())
    }
  }

  @Test
  fun testWaitingForResponseStatusAsUserShowsAwaitingConfirmation() {
    runBlocking {
      val waitingChat = fakeChat.copy(chatStatus = ChatStatus.WAITING_FOR_RESPONSE)
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(waitingChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      runBlocking {
        chatViewModel.getChats()
        chatViewModel.selectChat(waitingChat)
        quickFixViewModel.getQuickFixes()
      }

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      composeTestRule
          .onNodeWithText("Awaiting confirmation from John the Worker...")
          .assertIsDisplayed()
    }
  }

  @Test
  fun testWaitingForResponseStatusAsWorkerShowsAcceptRejectButtons() {
    runBlocking {
      val waitingChat = fakeChat.copy(chatStatus = ChatStatus.WAITING_FOR_RESPONSE)
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(waitingChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      // Set AppMode to WORKER
      appModeFlow.value = "WORKER"

      runBlocking {
        chatViewModel.getChats()
        chatViewModel.selectChat(waitingChat)
        quickFixViewModel.getQuickFixes()
      }

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      composeTestRule
          .onNodeWithText("Would you like to accept this QuickFix request?")
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag("acceptButton").assertIsDisplayed()
      composeTestRule.onNodeWithTag("refuseButton").assertIsDisplayed()
    }
  }

  @Test
  fun testClickingOnSuggestionSendsMessageAndUpdatesChat() {
    runBlocking {
      val gettingSuggestionsChat = fakeChat.copy(chatStatus = ChatStatus.GETTING_SUGGESTIONS)
      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<Chat>) -> Unit>(0)
            onSuccess(listOf(gettingSuggestionsChat))
            null
          }
          .whenever(chatRepository)
          .getChats(any(), any())

      runBlocking {
        chatViewModel.getChats()
        chatViewModel.selectChat(gettingSuggestionsChat)
        quickFixViewModel.getQuickFixes()
      }

      val suggestions =
          listOf(
              "How is it going?",
              "Is the time and day okay for you?",
              "I can’t wait to work with you!")

      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(1)
            onSuccess()
            null
          }
          .whenever(chatRepository)
          .updateChat(any(), any(), any())

      doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
          }
          .whenever(chatRepository)
          .sendMessage(any(), any(), any(), any())

      composeTestRule.setContent {
        QuickFixTheme {
          MessageScreen(
              chatViewModel = chatViewModel,
              navigationActions = navigationActions,
              quickFixViewModel = quickFixViewModel,
              preferencesViewModel = preferencesViewModel,
              accountViewModel = accountViewModel)
        }
      }

      suggestions.forEach { suggestion ->
        composeTestRule.onNodeWithText(suggestion).assertIsDisplayed()
      }

      val chosenSuggestion = suggestions.first()
      composeTestRule.onNodeWithText(chosenSuggestion).performClick()

      verify(chatRepository).updateChat(argThat { chatStatus == ChatStatus.ACCEPTED }, any(), any())

      verify(chatRepository)
          .sendMessage(
              argThat { chatId == gettingSuggestionsChat.chatId },
              argThat { content == chosenSuggestion },
              any(),
              any())
    }
  }
}
