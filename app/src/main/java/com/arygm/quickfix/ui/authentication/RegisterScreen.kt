package com.arygm.quickfix.ui.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arygm.quickfix.model.profile.LoggedInProfileViewModel
import com.arygm.quickfix.model.profile.ProfileViewModel
import com.arygm.quickfix.ui.elements.QuickFixAnimatedBox
import com.arygm.quickfix.ui.elements.QuickFixBackButtonTopBar
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.elements.QuickFixCheckBoxRow
import com.arygm.quickfix.ui.elements.QuickFixTextFieldCustom
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.navigation.Screen
import com.arygm.quickfix.ui.navigation.TopLevelDestinations
import com.arygm.quickfix.utils.BOX_COLLAPSE_SPEED
import com.arygm.quickfix.utils.BOX_OFFSET_X_EXPANDED
import com.arygm.quickfix.utils.BOX_OFFSET_X_SHRUNK
import com.arygm.quickfix.utils.createAccountWithEmailAndPassword
import com.arygm.quickfix.utils.isValidDate
import com.arygm.quickfix.utils.isValidEmail
import com.arygm.quickfix.utils.referenceHeight
import com.arygm.quickfix.utils.referenceWidth
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(
    navigationActions: NavigationActions,
    userViewModel: ProfileViewModel,
    loggedInProfileViewModel: LoggedInProfileViewModel,
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(), // Injected dependency
    createAccountFunc:
        (
            firebaseAuth: FirebaseAuth,
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            birthDate: String,
            userViewModel: ProfileViewModel,
            loggedInProfileViewModel: LoggedInProfileViewModel,
            onSuccess: () -> Unit,
            onFailure: () -> Unit) -> Unit =
        ::createAccountWithEmailAndPassword // Default implementation
) {
  val context = LocalContext.current
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var birthDate by remember { mutableStateOf("") }

  var emailError by remember { mutableStateOf(false) }
  var birthDateError by remember { mutableStateOf(false) }

  var acceptTerms by remember { mutableStateOf(false) }

  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var repeatPasswordVisible by remember { mutableStateOf(false) }
  var repeatPassword by remember { mutableStateOf("") }

  val noMatch by remember {
    derivedStateOf { password != repeatPassword && repeatPassword.isNotEmpty() }
  }

  val passwordConditions1 =
      listOf(
          "• At least 8 characters" to (password.length >= 8),
          "• Contains an uppercase letter (A-Z)" to password.any { it.isUpperCase() })

  val passwordConditions2 =
      listOf(
          "• Contains a lowercase letter (a-z)" to password.any { it.isLowerCase() },
          "• Contains a digit (0-9)" to password.any { it.isDigit() })

  var shrinkBox by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { shrinkBox = true }

  val focursManager = LocalFocusManager.current

  val filledForm =
      firstName.isNotEmpty() &&
          lastName.isNotEmpty() &&
          email.isNotEmpty() &&
          birthDate.isNotEmpty() &&
          passwordConditions1.all { it.second } &&
          passwordConditions2.all { it.second } &&
          !noMatch &&
          repeatPassword.isNotEmpty() &&
          acceptTerms
  BoxWithConstraints(
      modifier =
          Modifier.fillMaxSize().testTag("InfoBox").pointerInput(Unit) {
            detectTapGestures(onTap = { focursManager.clearFocus() })
          }) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Log.d("RegisterScreen", "Screen size: $screenWidth x $screenHeight")

        val widthRatio = screenWidth / referenceWidth
        val heightRatio = screenHeight / referenceHeight

        Log.d("RegisterScreen", "Screen ratio: $widthRatio x $heightRatio")

        val boxOffsetX by
            animateDpAsState(
                targetValue =
                    if (shrinkBox) BOX_OFFSET_X_SHRUNK * widthRatio
                    else BOX_OFFSET_X_EXPANDED * widthRatio,
                animationSpec = tween(durationMillis = BOX_COLLAPSE_SPEED),
                label = "shrinkingBox")

        QuickFixAnimatedBox(boxOffsetX, widthRatio = widthRatio, heightRatio = heightRatio)
        Scaffold(
            modifier =
                Modifier.background(colorScheme.background)
                    .fillMaxSize()
                    .testTag("RegisterScaffold"),
            content = { dp ->

              // Background and content are wrapped in a Box to control the layering
              Box(
                  modifier =
                      Modifier.fillMaxSize()
                          .testTag("ContentBox")
                          .background(colorScheme.background)
                          .padding(dp.calculateBottomPadding())) {

                    // TopAppBar below content (layered behind content)
                    Box(
                        modifier = Modifier.zIndex(1f) // Lower zIndex so it's behind the content
                        ) {
                          Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter =
                                    painterResource(
                                        id = com.arygm.quickfix.R.drawable.worker_image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().testTag("topBarBackground"))
                            QuickFixBackButtonTopBar(
                                onBackClick = {
                                  shrinkBox = false
                                  navigationActions.goBack()
                                },
                                color = Color.Transparent)
                          }
                        }

                    // Foreground content (on top of the TopAppBar)
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .zIndex(2f)
                                .background(
                                    colorScheme.background,
                                    shape =
                                        RoundedCornerShape(
                                            12.dp)) // Ensure content is above TopAppBar
                        ) {
                          // Adjusted Box using ratios
                          val relativeOffsetX = (-150).dp * widthRatio
                          val relativeOffsetY = 100.dp * heightRatio

                          Box(
                              modifier =
                                  Modifier.align(Alignment.BottomStart)
                                      .size(180.dp * widthRatio, 180.dp * heightRatio)
                                      .offset(x = relativeOffsetX, y = relativeOffsetY)
                                      .graphicsLayer(rotationZ = 57f)
                                      .background(colorScheme.primary)
                                      .testTag("BoxDecoration"))

                          Column(
                              modifier =
                                  Modifier.align(Alignment.Center)
                                      .padding(16.dp)
                                      .zIndex(100f)
                                      .verticalScroll(rememberScrollState()), // Ensure it's on top
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Register Now",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = colorScheme.primary,
                                    modifier = Modifier.testTag("welcomeText"))

                                Text(
                                    "Join QuickFix to connect with skilled workers!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSecondaryContainer,
                                    modifier = Modifier.testTag("welcomeTextBis"))

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth().padding(start = 8.dp * widthRatio),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically) {
                                      CustomTextField(
                                          value = firstName,
                                          onValueChange = { firstName = it },
                                          placeHolderText = "First Name",
                                          placeHolderColor = colorScheme.onSecondaryContainer,
                                          label = "First Name",
                                          columnModifier = Modifier.weight(1f),
                                          modifier = Modifier.testTag("firstNameInput"))

                                      CustomTextField(
                                          value = lastName,
                                          onValueChange = { lastName = it },
                                          placeHolderText = "Last Name",
                                          placeHolderColor = colorScheme.onSecondaryContainer,
                                          label = "Last Name",
                                          columnModifier = Modifier.weight(1f),
                                          modifier = Modifier.testTag("lastNameInput"))
                                    }

                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth().padding(start = 8.dp * widthRatio),
                                ) {
                                  QuickFixTextFieldCustom(
                                      value = email,
                                      onValueChange = {
                                        email = it
                                        emailError = !isValidEmail(it)
                                        userViewModel.profileExists(email) { exists, _ ->
                                          emailError =
                                              if (exists) {
                                                true
                                              } else {
                                                !isValidEmail(it)
                                              }
                                        }
                                      },
                                      placeHolderText = "Enter your email address",
                                      placeHolderColor = colorScheme.onSecondaryContainer,
                                      shape = RoundedCornerShape(12.dp),
                                      isError = emailError,
                                      showError = emailError,
                                      errorText = "INVALID EMAIL",
                                      moveContentHorizontal = 10.dp,
                                      heightField = 42.dp * heightRatio,
                                      widthField = 360.dp * widthRatio,
                                      modifier = Modifier.testTag("emailInput"),
                                      showLabel = true,
                                      label = {
                                        Text(
                                            "Email",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = colorScheme.onBackground,
                                            modifier =
                                                Modifier.padding(start = 3.dp * heightRatio)
                                                    .testTag("emailText"))
                                      })
                                }

                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth().padding(start = 8.dp * widthRatio),
                                ) {
                                  QuickFixTextFieldCustom(
                                      value = birthDate,
                                      onValueChange = {
                                        birthDate = it
                                        birthDateError = !isValidDate(it)
                                      },
                                      placeHolderText = "Enter your birthdate (DD/MM/YYYY)",
                                      placeHolderColor = colorScheme.onSecondaryContainer,
                                      singleLine = false,
                                      errorText = "INVALID DATE",
                                      isError = birthDateError,
                                      showError = birthDateError,
                                      moveContentHorizontal = 10.dp,
                                      heightField = 42.dp * heightRatio,
                                      widthField = 360.dp * widthRatio,
                                      shape = RoundedCornerShape(12.dp),
                                      modifier = Modifier.testTag("birthDateInput"),
                                      showLabel = true,
                                      label = {
                                        Text(
                                            "Birthdate",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = colorScheme.onBackground,
                                            modifier =
                                                Modifier.padding(start = 3.dp * widthRatio)
                                                    .testTag("birthDateText"))
                                      })
                                }

                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth().padding(start = 8.dp * widthRatio),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                      QuickFixTextFieldCustom(
                                          value = password,
                                          onValueChange = { password = it },
                                          placeHolderText = "Enter your password",
                                          placeHolderColor = colorScheme.onSecondaryContainer,
                                          moveContentHorizontal = 10.dp,
                                          heightField = 42.dp * heightRatio,
                                          widthField = 360.dp * widthRatio,
                                          shape = RoundedCornerShape(12.dp),
                                          trailingIcon = {
                                            val image =
                                                if (passwordVisible) Icons.Filled.VisibilityOff
                                                else Icons.Filled.Visibility
                                            IconButton(
                                                onClick = { passwordVisible = !passwordVisible }) {
                                                  Icon(
                                                      imageVector = image,
                                                      contentDescription = null,
                                                      tint = colorScheme.primary)
                                                }
                                          },
                                          showTrailingIcon = { password.isNotEmpty() },
                                          visualTransformation =
                                              if (passwordVisible) VisualTransformation.None
                                              else PasswordVisualTransformation(),
                                          keyboardOptions =
                                              KeyboardOptions.Default.copy(
                                                  imeAction = ImeAction.Next),
                                          modifier = Modifier.testTag("passwordInput"),
                                          showLabel = true,
                                          label = {
                                            Text(
                                                "Password",
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = colorScheme.onBackground,
                                                modifier =
                                                    Modifier.padding(start = 3.dp * widthRatio)
                                                        .testTag("passwordText"))
                                          })

                                      QuickFixTextFieldCustom(
                                          value = repeatPassword,
                                          onValueChange = { repeatPassword = it },
                                          placeHolderText = "Confirm password",
                                          placeHolderColor = colorScheme.onSecondaryContainer,
                                          moveContentHorizontal = 10.dp,
                                          heightField = 42.dp * heightRatio,
                                          widthField = 360.dp * widthRatio,
                                          shape = RoundedCornerShape(12.dp),
                                          trailingIcon = {
                                            val image =
                                                if (repeatPasswordVisible)
                                                    Icons.Filled.VisibilityOff
                                                else Icons.Filled.Visibility
                                            IconButton(
                                                onClick = {
                                                  repeatPasswordVisible = !repeatPasswordVisible
                                                }) {
                                                  Icon(
                                                      imageVector = image,
                                                      contentDescription = null,
                                                      tint = colorScheme.primary)
                                                }
                                          },
                                          showTrailingIcon = { repeatPassword.isNotEmpty() },
                                          visualTransformation =
                                              if (repeatPasswordVisible) VisualTransformation.None
                                              else PasswordVisualTransformation(),
                                          keyboardOptions =
                                              KeyboardOptions.Default.copy(
                                                  imeAction = ImeAction.Done),
                                          modifier = Modifier.testTag("repeatPasswordInput"))
                                    }

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth()
                                            .padding(horizontal = 8.dp * widthRatio)
                                            .testTag("passwordConditions"),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                      PasswordConditions(
                                          password, passwordConditions1, widthRatio, heightRatio)
                                      PasswordConditions(
                                          password, passwordConditions2, widthRatio, heightRatio)
                                    }

                                // Error message if passwords don't match
                                if (noMatch) {
                                  Text(
                                      "The two password do not match, try again. ",
                                      style = MaterialTheme.typography.bodySmall,
                                      color = colorScheme.error,
                                      modifier =
                                          Modifier.padding(start = 3.dp).testTag("noMatchText"),
                                      textAlign = TextAlign.Start)
                                  Spacer(modifier = Modifier.padding(8.dp))
                                } else {
                                  Spacer(modifier = Modifier.padding(12.dp))
                                }

                                QuickFixCheckBoxRow(
                                    modifier = Modifier.padding(start = 6.dp * widthRatio),
                                    checked = acceptTerms,
                                    onCheckedChange = { acceptTerms = it },
                                    label = "I'm at least 18 and agree to the following",
                                    underlinedText = "Term & Conditions",
                                    onUnderlinedTextClick = { /* TODO: Add click logic */},
                                    labelBis = "and",
                                    underlinedTextBis = "Privacy Policy",
                                    onUnderlinedTextClickBis = { /* TODO: Add click logic */},
                                    colorScheme = colorScheme)

                                QuickFixButton(
                                    buttonText = "Register",
                                    onClickAction = {
                                      shrinkBox = false
                                      createAccountFunc(
                                          firebaseAuth,
                                          firstName,
                                          lastName,
                                          email,
                                          password,
                                          birthDate,
                                          userViewModel,
                                          loggedInProfileViewModel,
                                          {
                                            navigationActions.navigateTo(TopLevelDestinations.HOME)
                                          },
                                          {
                                            Toast.makeText(
                                                    context,
                                                    "Registration Failed.",
                                                    Toast.LENGTH_LONG)
                                                .show()
                                          })
                                    },
                                    buttonColor = colorScheme.primary,
                                    textColor = colorScheme.onPrimary,
                                    textStyle = MaterialTheme.typography.labelLarge,
                                    modifier =
                                        Modifier.width(360.dp * widthRatio)
                                            .height(55.dp * heightRatio)
                                            .testTag("registerButton")
                                            .graphicsLayer(alpha = 1f),
                                    enabled = filledForm && !emailError && !birthDateError)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                  Text(
                                      "Already have an account ?",
                                      style = MaterialTheme.typography.headlineSmall,
                                      color = colorScheme.onSecondaryContainer,
                                      modifier =
                                          Modifier.padding(bottom = 8.dp * heightRatio)
                                              .requiredWidth(225.dp * widthRatio)
                                              .testTag("alreadyAccountText"),
                                      textAlign = TextAlign.End)

                                  QuickFixButton(
                                      buttonText = "Login !",
                                      onClickAction = {
                                        navigationActions.navigateTo(Screen.LOGIN)
                                      },
                                      buttonColor = Color.Transparent,
                                      textColor = colorScheme.primary,
                                      textStyle = MaterialTheme.typography.headlineSmall,
                                      contentPadding = PaddingValues(4.dp),
                                      horizontalArrangement = Arrangement.Start,
                                      modifier = Modifier.testTag("clickableLoginButtonText"))
                                }
                                Spacer(modifier = Modifier.padding(bottom = 30.dp * heightRatio))
                              }
                        }
                  }
            })
      }
}

@Composable
private fun PasswordConditions(
    password: String,
    listConditions: List<Pair<String, Boolean>>,
    widthRatio: Float = 1f,
    heightRatio: Float = 1f
) {
  Column(modifier = Modifier.padding(vertical = 8.dp * heightRatio)) {
    listConditions.forEach { (condition, met) ->
      Text(
          text = condition,
          color =
              if (met || password.isEmpty()) colorScheme.onSecondaryContainer
              else colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 3.dp * widthRatio))
    }
  }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeHolderText: String,
    placeHolderColor: Color,
    label: String,
    columnModifier: Modifier = Modifier,
    isError: Boolean = false,
    showError: Boolean = false,
    errorText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    showTrailingIcon: () -> Boolean = { false },
    modifier: Modifier = Modifier
) {
  Column(modifier = columnModifier.padding(end = 12.dp)) {
    QuickFixTextFieldCustom(
        value = value,
        onValueChange = onValueChange,
        placeHolderText = placeHolderText,
        placeHolderColor = placeHolderColor,
        shape = RoundedCornerShape(12.dp),
        moveContentHorizontal = 10.dp,
        heightField = 42.dp,
        isError = isError,
        showError = showError,
        errorText = errorText,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        showTrailingIcon = showTrailingIcon,
        modifier = modifier,
        showLabel = true,
        label = {
          Text(
              label,
              style = MaterialTheme.typography.headlineSmall,
              color = colorScheme.onBackground,
              modifier = Modifier.padding(start = 3.dp))
        })
  }
}
