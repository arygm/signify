package com.arygm.quickfix.ui.uiMode.appContentUI

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arygm.quickfix.model.account.AccountViewModel
import com.arygm.quickfix.model.category.CategoryViewModel
import com.arygm.quickfix.model.locations.Location
import com.arygm.quickfix.model.locations.LocationViewModel
import com.arygm.quickfix.model.offline.small.PreferencesViewModel
import com.arygm.quickfix.model.profile.ProfileViewModel
import com.arygm.quickfix.model.switchModes.AppMode
import com.arygm.quickfix.model.switchModes.ModeViewModel
import com.arygm.quickfix.ui.elements.QuickFixOfflineBar
import com.arygm.quickfix.ui.navigation.BottomNavigationMenu
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.uiMode.appContentUI.navigation.AppContentRoute
import com.arygm.quickfix.ui.uiMode.workerMode.WorkerModeNavGraph
import com.arygm.quickfix.ui.userModeUI.UserModeNavHost
import com.arygm.quickfix.ui.userModeUI.navigation.UserScreen
import kotlinx.coroutines.delay

@Composable
fun AppContentNavGraph(
    testBitmapPP: Bitmap?,
    testLocation: Location = Location(),
    preferencesViewModel: PreferencesViewModel,
    userViewModel: ProfileViewModel,
    accountViewModel: AccountViewModel,
    isOffline: Boolean,
    rootNavigationActions: NavigationActions,
    modeViewModel: ModeViewModel,
    userPreferencesViewModel: PreferencesViewModel
) {
  val appContentNavController = rememberNavController()
  val appContentNavigationActions = remember { NavigationActions(appContentNavController) }
  val userNavController = rememberNavController()
  val userNavigationActions = remember { NavigationActions(userNavController) }
  val workerNavController = rememberNavController()
  val workerNavigationActions = remember { NavigationActions(workerNavController) }

  val workerViewModel: ProfileViewModel =
      viewModel(key = "workerViewModel", factory = ProfileViewModel.WorkerFactory)
  val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory)
  val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)


     NavHost(
            navController = appContentNavigationActions.navController,
            startDestination = AppContentRoute.USER_MODE, // Apply padding from the Scaffold
            enterTransition = {
              // You can change whatever you want for transitions
              EnterTransition.None
            },
            exitTransition = {
              // You can change whatever you want for transitions
              ExitTransition.None
            }) {
              composable(AppContentRoute.USER_MODE) {
                UserModeNavHost(
                    testBitmapPP,
                    testLocation,
                    modeViewModel,
                    userViewModel,
                    workerViewModel,
                    accountViewModel,
                    categoryViewModel,
                    locationViewModel,
                    preferencesViewModel,
                    rootNavigationActions,
                    userNavigationActions,
                    userPreferencesViewModel,
                    appContentNavigationActions,
                    isOffline)
              }

              composable(AppContentRoute.WORKER_MODE) {
                WorkerModeNavGraph(workerNavigationActions, isOffline = isOffline, modeViewModel = modeViewModel )
              }
            }
      }
