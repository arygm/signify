package com.arygm.quickfix.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.theme.poppinsTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileConfigurationScreen(navigationActions: NavigationActions, isUser: Boolean = true) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Account configuration",
                            modifier = Modifier
                                .testTag("AccountConfigurationTitle")
                                .padding(end = 29.dp),
                            style = poppinsTypography.headlineMedium,
                            color = colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("goBackButton")
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.primary
                        )
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to the Account Configuration Screen",
                    modifier = Modifier
                        .padding(padding)
                )
            }
        }
    )
}