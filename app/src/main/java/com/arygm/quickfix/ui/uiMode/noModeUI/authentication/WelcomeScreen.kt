package com.arygm.quickfix.ui.uiMode.noModeUI.authentication

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.arygm.quickfix.R
import com.arygm.quickfix.model.account.AccountViewModel
import com.arygm.quickfix.model.offline.small.PreferencesViewModel
import com.arygm.quickfix.model.offline.small.PreferencesViewModelUserProfile
import com.arygm.quickfix.model.profile.ProfileViewModel
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.navigation.RootRoute
import com.arygm.quickfix.ui.uiMode.noModeUI.navigation.NoModeRoute
import com.arygm.quickfix.ui.uiMode.noModeUI.navigation.NoModeScreen
import com.arygm.quickfix.utils.loadIsSignIn
import com.arygm.quickfix.utils.rememberFirebaseAuthLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UseOfNonLambdaOffsetOverload")
@Composable
fun WelcomeScreen(
    navigationActions: NavigationActions,
    accountViewModel: AccountViewModel,
    userViewModel: ProfileViewModel,
    preferencesViewModel: PreferencesViewModel,
    rootNavigationActions: NavigationActions,
    userPreferencesViewModel: PreferencesViewModelUserProfile
) {
  val colorScheme = MaterialTheme.colorScheme

  var fadeOut by remember { mutableStateOf(true) }
  var expandBox by remember { mutableStateOf(true) }
  var startAnimation by remember { mutableStateOf(false) }
  var targetScreen by remember { mutableStateOf("") }

  val elementsAlpha by
      animateFloatAsState(targetValue = if (fadeOut) 0f else 1f, label = "elementsFade")

  val boxOffsetX by
      animateDpAsState(targetValue = if (expandBox) 0.dp else (-890).dp, label = "moveBoxX")

  val context = LocalContext.current
  var isSignIn by remember { mutableStateOf(false) }
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthCompleteOne = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            rootNavigationActions.navigateTo(RootRoute.APP_CONTENT)
          },
          onAuthCompleteTwo = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            navigationActions.navigateTo(NoModeRoute.GOOGLE_INFO)
          },
          onAuthError = { Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}") },
          accountViewModel,
          userViewModel = userViewModel,
          preferencesViewModel = preferencesViewModel,
          userPreferencesViewModel = userPreferencesViewModel)

  val token = stringResource(com.arygm.quickfix.R.string.default_web_client_id)

  // fast forward to home screen if user is already signed in
  LaunchedEffect(Unit) {
    isSignIn = loadIsSignIn(preferencesViewModel)
    if (isSignIn &&
        navigationActions.currentScreen ==
            NoModeScreen.WELCOME) { // Ensure the value is `true` before navigating
      Log.i("SignInScreen", "User is signed in, fast forwarding to home screen")
      rootNavigationActions.navigateTo(RootRoute.APP_CONTENT)
    } else {
      Log.d("SignInScreen", "User is not signed in or preference not set")
    }
  }
  LaunchedEffect(Unit) {
    expandBox = false // Start expanding the box
    delay(200) // Wait for box to fully shrink
    fadeOut = false // Start fade-out animation
    delay(300) // Wait for fade-out to complete
    // Navigate to RegistrationScreen
  }
  // Animation sequence when the Register button is clicked
  if (startAnimation) {
    LaunchedEffect(Unit) {
      fadeOut = true // Start fade-out animation
      delay(300) // Wait for fade-out to complete
      expandBox = true // Start expanding the box
      delay(500) // Wait for box to fully shrink
      navigationActions.navigateTo(targetScreen) // Navigate to RegistrationScreen
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize().testTag("welcomeBox")) {
    val screenHeight = maxHeight
    val screenWidth = maxWidth
    Image(
        painter = painterResource(id = com.arygm.quickfix.R.drawable.worker_image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopStart,
        modifier =
            Modifier.requiredSize(screenWidth * 1.10f, screenHeight).testTag("workerBackground"))

    Box(
        // Leaving as is as animations are tricky to fine-tune without breaking
        modifier =
            Modifier.align(Alignment.BottomStart)
                .requiredSize(1700.dp)
                .offset(x = boxOffsetX, y = 70.dp)
                .graphicsLayer(rotationZ = -28f)
                .background(colorScheme.primary)
                .testTag("boxDecoration1"))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize().padding(bottom = screenHeight * 0.05f)) {
          Image(
              painter = painterResource(id = com.arygm.quickfix.R.drawable.quickfix),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              colorFilter = ColorFilter.tint(Color.White),
              modifier =
                  Modifier.fillMaxWidth(0.7f)
                      .graphicsLayer(rotationZ = 4.57f, alpha = elementsAlpha)
                      .testTag("quickFixLogo"))

          // QuickFix Text
          Text(
              text = "QuickFix",
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              modifier =
                  Modifier.padding(bottom = screenHeight * 0.01f) // Space between text and buttons
                      .graphicsLayer(alpha = elementsAlpha)
                      .testTag("quickFixText"))

          QuickFixButton(
              buttonText = "LOG IN TO QUICKFIX",
              onClickAction = {
                targetScreen = NoModeRoute.LOGIN
                startAnimation = true
              },
              buttonColor = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.graphicsLayer(alpha = elementsAlpha).testTag("logInButton"),
              textColor = colorScheme.onPrimary)

          QuickFixButton(
              buttonText = "REGISTER TO QUICKFIX",
              onClickAction = {
                targetScreen = NoModeRoute.REGISTER
                startAnimation = true
              },
              buttonColor = colorScheme.surfaceDim,
              modifier =
                  Modifier.graphicsLayer(alpha = elementsAlpha).testTag("RegistrationButton"),
              textColor = colorScheme.scrim)

          QuickFixButton(
              onClickAction = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              },
              buttonColor = Color.Transparent,
              border = BorderStroke(2.dp, colorScheme.surfaceContainerLowest),
              modifier =
                  Modifier.fillMaxWidth(0.8f)
                      .graphicsLayer(alpha = elementsAlpha)
                      .testTag("googleButton"),
              buttonText = "CONTINUE WITH GOOGLE",
              textColor = colorScheme.surfaceContainerLowest,
              leadingIcon = ImageVector.vectorResource(id = R.drawable.google),
              leadingIconTint = colorScheme.surfaceContainerLowest)
        }
  }
}
