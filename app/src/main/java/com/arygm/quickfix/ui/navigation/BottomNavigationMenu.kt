package com.arygm.quickfix.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.arygm.quickfix.BuildConfig
import com.arygm.quickfix.R
import com.etebarian.meowbottomnavigation.MeowBottomNavigation

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    navigationActions: NavigationActions,
    tabList: List<TopLevelDestination>,
    getBottomBarId: (String) -> Int,
) {
  val selectedTabId = remember { mutableIntStateOf(1) } // default tab id

  val colorScheme = colorScheme
  val currentRoute by navigationActions.currentRoute.collectAsState()

  val bottomNavigation = remember { mutableStateOf<MeowBottomNavigation?>(null) }

  // Use AndroidView to integrate MeowBottomNavigation
  AndroidView(
      factory = { ctx ->
        Log.d("BottomNavigationMenu", "the Android view is recomposed")

        MeowBottomNavigation(ctx).apply {
          bottomNavigation.value = this

          // Add menu items using the tabList
          tabList.forEachIndexed { index, tab ->
            // Get the drawable resource from the ImageVector
            val drawableId = tab.icon?.let { convertImageVectorToDrawableId(it) }
            val model = drawableId?.let { MeowBottomNavigation.Model(index + 1, it) }
            if (drawableId != null) {
              if (model != null) {
                model.icon = drawableId
              }
            } // Set the icon of the model
            if (BuildConfig
                .DEBUG) { // when we'll want to generate the APK since it will be in release mode
              // normally this will not be executed
              if (model != null) {
                model.count = tab.route
              } // Set the count of the model
              countBackgroundColor = 0
            }

            id = index + 1 // Set the ID of the model
            // Add the model to MeowBottomNavigation
            add(model)
          }
          // Set design colors
          circleColor = colorScheme.primary.toArgb() // Central button color
          backgroundBottomColor = colorScheme.surface.toArgb() // Orange background color
          defaultIconColor =
              colorScheme.tertiaryContainer.toArgb() // Default icon color (unselected)
          selectedIconColor = colorScheme.surface.toArgb() // Selected icon color

          setOnShowListener { model ->
            // Handle item show event
            Log.d("MeowBottomNavigation", "Item shown: ${model.id}")
          }

          // Define actions on selecting a menu item
          setOnClickMenuListener { model ->
            val selectedTab = tabList.getOrNull(model.id - 1) // Find the corresponding tab
            if (selectedTab != null) {
              onTabSelect(selectedTab)
            }
          }

          // Handle reselect event to avoid crash
          setOnReselectListener { model ->
            // Handle the event when the user clicks on the currently selected item
            Log.d("MeowBottomNavigation", "Reselected tab id: ${model.id}")
            // You can add a behavior here or just ignore the reselect event
          }

          contentDescription = "MeowBottomNavigation"
        }
      },
      modifier = Modifier.fillMaxWidth().testTag("BottomNavMenu"))

  // LaunchedEffect allowing to update the bottom bar accordingly to navigationActions
  LaunchedEffect(currentRoute) {
    val selectedItemId =
        getBottomBarId(navigationActions.currentRoute()) // Get the ID of the selected item
    selectedTabId.intValue = selectedItemId
    bottomNavigation.value?.show(selectedItemId, true)
  }
}

// Helper function to convert ImageVector to drawable resource ID
fun convertImageVectorToDrawableId(imageVector: ImageVector): Int {
  return when (imageVector) {
    Icons.Default.Home -> R.drawable.icon_home_vector
    Icons.Default.AddCircle -> R.drawable.icon_annoucement
    Icons.Default.AccountCircle -> R.drawable.profile
    Icons.Default.MoreVert -> R.drawable.icon_other
    Icons.Default.Menu -> R.drawable.dashboard
    Icons.Default.Search -> R.drawable.logo
    Icons.Outlined.Home -> R.drawable.icon_home_vector
    Icons.Outlined.Campaign -> R.drawable.cmpaign
    Icons.Filled.MailOutline -> R.drawable.mail
    Icons.Filled.PersonOutline -> R.drawable.profile
    else -> R.drawable.ic_launcher_background // Default fallback icon
  }
}
