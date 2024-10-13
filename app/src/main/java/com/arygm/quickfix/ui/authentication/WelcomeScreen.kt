package com.arygm.quickfix.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arygm.quickfix.model.profile.Profile
import com.arygm.quickfix.model.profile.ProfileViewModel
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WelcomeScreen(navigationActions: NavigationActions, profileViewModel: ProfileViewModel) {
  val colorScheme = MaterialTheme.colorScheme

  val context = LocalContext.current

  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
            navigationActions.navigateTo(Screen.PASSWORD)
          },
          onAuthError = {
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          },
          profileViewModel)

  val token = stringResource(com.arygm.quickfix.R.string.default_web_client_id)

  var fadeOut by remember { mutableStateOf(false) }
  var expandBox by remember { mutableStateOf(false) }
  var startAnimation by remember { mutableStateOf(false) }
  var targetScreen by remember { mutableStateOf("") }

  val elementsAlpha by
      animateFloatAsState(targetValue = if (fadeOut) 0f else 1f, label = "elementsFade")

  val boxOffsetX by
      animateDpAsState(targetValue = if (expandBox) 0.dp else (-890).dp, label = "moveBoxX")

  // Animation sequence when the Register button is clicked
  @Composable
  if (startAnimation) {
    LaunchedEffect(Unit) {
      fadeOut = true // Start fade-out animation
      delay(300) // Wait for fade-out to complete
      expandBox = true // Start expanding the box
      delay(500) // Wait for box to fully shrink
      navigationActions.navigateTo(targetScreen) // Navigate to RegistrationScreen
    }
  }

  Box(modifier = Modifier.fillMaxSize().testTag("welcomeBox")) {
    Image(
        painter = painterResource(id = com.arygm.quickfix.R.drawable.worker_image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopStart,
        modifier = Modifier.fillMaxSize().testTag("workerBackground"))

    Box(
        modifier =
            Modifier.align(Alignment.BottomStart)
                .requiredSize(1700.dp)
                .offset(x = boxOffsetX, y = 30.dp)
                .graphicsLayer(rotationZ = -28f)
                .background(colorScheme.primary)
                .testTag("boxDecoration1"))
    Box(
        modifier =
            Modifier.align(Alignment.BottomStart)
                .size(425.dp, 150.dp)
                .background(colorScheme.primary)
                .testTag("boxDecoration2"))

    Image(
        painter = painterResource(id = com.arygm.quickfix.R.drawable.quickfix),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(colorScheme.background),
        modifier =
            Modifier.align(Alignment.Center)
                .offset(x = 0.dp, y = (-30).dp)
                .size(width = 283.dp, height = 332.7.dp)
                .graphicsLayer(rotationZ = 4.57f, alpha = elementsAlpha)
                .testTag("quickFixLogo"))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier.fillMaxSize()
                .padding(top = 420.dp) // Adjust this to fine-tune the position below the logo
        ) {
          Spacer(modifier = Modifier.padding(60.dp))
          // QuickFix Text
          Text(
              text = "QuickFix",
              style = MaterialTheme.typography.titleLarge,
              color = colorScheme.background,
              modifier =
                  Modifier.padding(bottom = 24.dp) // Space between text and buttons
                      .graphicsLayer(alpha = elementsAlpha)
                      .testTag("quickFixText"))

          QuickFixButton(
              buttonText = "LOG IN TO QUICKFIX",
              onClickAction = {
                targetScreen = Screen.LOGIN
                startAnimation = true
              },
              buttonColor = colorScheme.secondary,
              modifier = Modifier.graphicsLayer(alpha = elementsAlpha).testTag("logInButton"),
              textColor = colorScheme.background)

          QuickFixButton(
              buttonText = "REGISTER TO QUICKFIX",
              onClickAction = {
                targetScreen = Screen.INFO
                startAnimation = true
              },
              buttonColor = colorScheme.background,
              modifier =
                  Modifier.graphicsLayer(alpha = elementsAlpha).testTag("RegistrationButton"),
              textColor = colorScheme.secondary)

          Button(
              onClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
              border = BorderStroke(2.dp, colorScheme.background),
              modifier =
                  Modifier.fillMaxWidth(0.8f)
                      .height(50.dp)
                      .graphicsLayer(alpha = elementsAlpha)
                      .testTag("googleButton"),
              shape = RoundedCornerShape(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp)) {
                      Image(
                          painter =
                              painterResource(
                                  id = com.arygm.quickfix.R.drawable.google,
                              ),
                          contentDescription = "Google Logo",
                          colorFilter = ColorFilter.tint(colorScheme.background),
                          modifier = Modifier.size(30.dp).offset(x = (-3).dp).testTag("googleLogo"))

                      Spacer(modifier = Modifier.width(16.dp))

                      // Button Text
                      Text(
                          text = "CONTINUE WITH GOOGLE",
                          color = colorScheme.background,
                          style = MaterialTheme.typography.labelMedium,
                      )
                    }
              }
        }
  }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit,
    profileViewModel: ProfileViewModel
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        val user = Firebase.auth.currentUser

        user?.let {
          profileViewModel.fetchUserProfile(it.uid) { existingProfile ->
            if (existingProfile != null) {
              profileViewModel.setLoggedInProfile(existingProfile)
              onAuthComplete(authResult)
            } else {
              // Extract user information from Google account
              val firstName = account.givenName ?: ""
              val lastName = account.familyName ?: ""
              val email = account.email ?: ""
              val uid = user.uid

              // Create a new Profile object
              val profile =
                  Profile(
                      uid = uid,
                      firstName = firstName,
                      lastName = lastName,
                      email = email,
                      password = "",
                      birthDate = Timestamp.now())

              // Save the profile to Firestore
              profileViewModel.addProfile(
                  profile,
                  onSuccess = {
                    profileViewModel.setLoggedInProfile(profile)
                    onAuthComplete(authResult)
                  },
                  onFailure = { exception ->
                    Log.e("Google SignIn", "Failed to save new profile", exception)
                  })
            }
          }
        } ?: run { Log.e("Google SignIn", "User Null After Sign In") }
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}
