package com.arygm.quickfix.ui.search

import android.location.Geocoder
import android.util.Log
import android.widget.RatingBar
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arygm.quickfix.MainActivity
import com.arygm.quickfix.R
import com.arygm.quickfix.model.account.Account
import com.arygm.quickfix.model.account.AccountViewModel
import com.arygm.quickfix.model.offline.small.PreferencesViewModel
import com.arygm.quickfix.model.profile.ProfileViewModel
import com.arygm.quickfix.model.profile.UserProfile
import com.arygm.quickfix.model.profile.WorkerProfile
import com.arygm.quickfix.model.search.SearchViewModel
import com.arygm.quickfix.ui.elements.ChooseServiceTypeSheet
import com.arygm.quickfix.ui.elements.QuickFixAvailabilityBottomSheet
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.elements.QuickFixLocationFilterBottomSheet
import com.arygm.quickfix.ui.elements.QuickFixPriceRangeBottomSheet
import com.arygm.quickfix.ui.elements.QuickFixSlidingWindow
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.theme.poppinsTypography
import com.arygm.quickfix.utils.LocationHelper
import com.arygm.quickfix.utils.loadUserId
import com.example.dynamicstarrating.RatingBar
import java.time.LocalDate
import java.util.Locale

data class SearchFilterButtons(
    val onClick: () -> Unit,
    val text: String,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val applied: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchWorkerResult(
    navigationActions: NavigationActions,
    searchViewModel: SearchViewModel,
    accountViewModel: AccountViewModel,
    userProfileViewModel: ProfileViewModel,
    workerProfileViewModel: ProfileViewModel,
    preferencesViewModel: PreferencesViewModel
) {
  val geocoder = Geocoder(LocalContext.current, Locale.getDefault())
  fun getCityNameFromCoordinates(latitude: Double, longitude: Double): String? {
    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
    if (!addresses.isNullOrEmpty()) {
      val city = addresses[0].locality
      return city ?: addresses[0].subAdminArea ?: addresses[0].adminArea
    } else {
      return null
    }
  }
  val locationHelper = LocationHelper(LocalContext.current, MainActivity())
  var phoneLocation by remember {
    mutableStateOf(com.arygm.quickfix.model.locations.Location(0.0, 0.0, "Default"))
  }
  var baseLocation by remember { mutableStateOf(phoneLocation) }
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    if (locationHelper.checkPermissions()) {
      locationHelper.getCurrentLocation { location ->
        if (location != null) {
          phoneLocation =
              com.arygm.quickfix.model.locations.Location(
                  location.latitude, location.longitude, "Phone Location")
          baseLocation = phoneLocation
        } else {
          Toast.makeText(context, "Unable to fetch location", Toast.LENGTH_SHORT).show()
        }
      }
    } else {
      Toast.makeText(context, "Enable Location In Settings", Toast.LENGTH_SHORT).show()
    }
  }

  var selectedWorker by remember { mutableStateOf(WorkerProfile()) }
  var selectedCityName by remember { mutableStateOf<String?>(null) }
  var showFilterButtons by remember { mutableStateOf(false) }
  var showAvailabilityBottomSheet by remember { mutableStateOf(false) }
  var showServicesBottomSheet by remember { mutableStateOf(false) }
  var showPriceRangeBottomSheet by remember { mutableStateOf(false) }
  var showLocationBottomSheet by remember { mutableStateOf(false) }
  val workerProfiles by searchViewModel.subCategoryWorkerProfiles.collectAsState()
  var filteredWorkerProfiles by remember { mutableStateOf(workerProfiles) }
  val searchSubcategory by searchViewModel.searchSubcategory.collectAsState()

  var availabilityFilterApplied by remember { mutableStateOf(false) }
  var servicesFilterApplied by remember { mutableStateOf(false) }
  var priceFilterApplied by remember { mutableStateOf(false) }
  var locationFilterApplied by remember { mutableStateOf(false) }
  var ratingFilterApplied by remember { mutableStateOf(false) }

  var selectedDays by remember { mutableStateOf(emptyList<LocalDate>()) }
  var selectedHour by remember { mutableStateOf(0) }
  var selectedMinute by remember { mutableStateOf(0) }
  var selectedServices by remember { mutableStateOf(emptyList<String>()) }
  var selectedPriceStart by remember { mutableStateOf(0) }
  var selectedPriceEnd by remember { mutableStateOf(0) }
  var selectedLocation by remember { mutableStateOf(com.arygm.quickfix.model.locations.Location()) }
  var maxDistance by remember { mutableStateOf(0) }

  fun reapplyFilters() {
    var updatedProfiles = workerProfiles

    if (availabilityFilterApplied) {
      updatedProfiles =
          searchViewModel.filterWorkersByAvailability(
              updatedProfiles, selectedDays, selectedHour, selectedMinute)
    }

    if (servicesFilterApplied) {
      updatedProfiles = searchViewModel.filterWorkersByServices(updatedProfiles, selectedServices)
    }

    if (priceFilterApplied) {
      updatedProfiles =
          searchViewModel.filterWorkersByPriceRange(
              updatedProfiles, selectedPriceStart, selectedPriceEnd)
    }

    if (locationFilterApplied) {
      updatedProfiles =
          searchViewModel.filterWorkersByDistance(updatedProfiles, selectedLocation, maxDistance)
    }

    if (ratingFilterApplied) {
      updatedProfiles = searchViewModel.sortWorkersByRating(updatedProfiles)
    }

    filteredWorkerProfiles = updatedProfiles
  }

  val listOfButtons =
      listOf(
          SearchFilterButtons(
              onClick = {
                filteredWorkerProfiles = workerProfiles
                availabilityFilterApplied = false
                priceFilterApplied = false
                locationFilterApplied = false
                ratingFilterApplied = false
                servicesFilterApplied = false
                selectedServices = emptyList()
                baseLocation = phoneLocation
              },
              text = "Clear",
              leadingIcon = Icons.Default.Clear,
              applied = false),
          SearchFilterButtons(
              onClick = { showLocationBottomSheet = true },
              text = "Location",
              leadingIcon = Icons.Default.LocationSearching,
              trailingIcon = Icons.Default.KeyboardArrowDown,
              applied = locationFilterApplied),
          SearchFilterButtons(
              onClick = { showServicesBottomSheet = true },
              text = "Service Type",
              leadingIcon = Icons.Default.Handyman,
              trailingIcon = Icons.Default.KeyboardArrowDown,
              applied = servicesFilterApplied),
          SearchFilterButtons(
              onClick = { showAvailabilityBottomSheet = true },
              text = "Availability",
              leadingIcon = Icons.Default.CalendarMonth,
              trailingIcon = Icons.Default.KeyboardArrowDown,
              applied = availabilityFilterApplied),
          SearchFilterButtons(
              onClick = {
                if (ratingFilterApplied) {
                  ratingFilterApplied = false
                  reapplyFilters()
                } else {
                  filteredWorkerProfiles =
                      searchViewModel.sortWorkersByRating(filteredWorkerProfiles)
                  ratingFilterApplied = true
                }
              },
              text = "Highest Rating",
              leadingIcon = Icons.Default.WorkspacePremium,
              trailingIcon = if (ratingFilterApplied) Icons.Default.Clear else null,
              applied = ratingFilterApplied),
          SearchFilterButtons(
              onClick = { showPriceRangeBottomSheet = true },
              text = "Price Range",
              leadingIcon = Icons.Default.MonetizationOn,
              trailingIcon = Icons.Default.KeyboardArrowDown,
              applied = priceFilterApplied),
      )

  // ==========================================================================//
  // ============ TODO: REMOVE NO-DATA WHEN BACKEND IS IMPLEMENTED ============//
  // ==========================================================================//

  val bannerImage = R.drawable.moroccan_flag
  val profilePicture = R.drawable.placeholder_worker
  val workerName = "Moha Abbes"
  val workerCategory = "Exterior Painter"
  val description =
      "According to all known laws of aviation, there is no way a bee should be able " +
          "to fly. Its wings are too small to get its fat little body off the ground. The bee, of " +
          "course, flies anyway because bees don't care what humans think is impossible. Yellow, " +
          "black. Yellow, black. Yellow, black. Yellow, black. Ooh, black and yellow! Let's shake " +
          "it up a little. Barry! Breakfast is ready! Coming! Hang on a second. Hello? - Barry? - Adam? " +
          "- Can you believe this is happening? - I can't. I'll pick you up. Looking sharp. Use the stairs. " +
          "Your father paid good money for those. Sorry. I'm excited. Here's the graduate. We're " +
          "very proud of you, son. A perfect report card, all B's. Very proud. Ma! I got a thing " +
          "going here. - You got lint on your fuzz. - Ow! That's me! - Wave to us! We'll be in row " +
          "118,000. - Bye! Barry, I told you, stop flying in the house! - Hey, Adam. - Hey, Barry. " +
          "- Is that fuzz gel? - A little. Special day, graduation. Never thought I'd make it. " +
          "Three days grade school, three days high school. Those were awkward. Three days college. " +
          "I'm glad I took a day and hitchhiked around the hive. You did come back different. " +
          "- Hi, Barry. - Artie, growing a mustache? Looks good. - Hear about Frankie? - Yeah. " +
          "- You going to the funeral? - No, I'm not going. Everybody knows, sting someone, you die. " +
          "Don't waste it on a squirrel. Such a hothead. I guess he could have just gotten out of the way. " +
          "I love this incorporating an amusement park into our day. That's why we don't need vacations. " +
          "Boy, quite a bit of pomp... under the circumstances."
  val workerAddress = "Ecublens, VD"
  val workerRating = 3.8
  val includedServices =
      listOf(
          "Initial Consultation",
          "Basic Surface Preparation",
          "Priming of Surfaces",
          "High-Quality Paint Application",
          "Two Coats of Paint",
          "Professional Cleanup")
  val addonServices =
      listOf(
          "Detailed Color Consultation",
          "Premium paint Upgrade",
          "Extensive Surface Preparation",
          "Extra Coats for added Durability",
          "Power Washing and Deep Cleaning")
  val rate = 40
  val tags =
      listOf(
          "Exterior Painting",
          "Interior Painting",
          "Cabinet Painting",
          "Licensed & Insured",
          "Local Worker")
  val reviews =
      listOf(
          "Overall, the work was shit; it’s fair to say that since he just painted the whole mural with " +
              "his shit. I don’t know what to do; I’ll probably sue him.",
          "Moha was very punctual and did a great job painting our living room. Highly recommended!",
          "Moha was very professional and did a fantastic job painting our house",
          "I wanna marry that man,")

  // ==========================================================================//
  // ==========================================================================//
  // ==========================================================================//

  var isWindowVisible by remember { mutableStateOf(false) }
  var saved by remember { mutableStateOf(false) }
  val searchQuery by searchViewModel.searchQuery.collectAsState()

  var userProfile = UserProfile(locations = emptyList(), announcements = emptyList(), uid = "0")
  var uid by remember { mutableStateOf("Loading...") }

  LaunchedEffect(Unit) { uid = loadUserId(preferencesViewModel) }
  userProfileViewModel.fetchUserProfile(uid) { profile ->
    if (profile is UserProfile) {
      userProfile = profile
    } else {
      Log.e("SearchWorkerResult", "Fetched a worker profile from a user profile repo.")
    }
  }

  // Wrap everything in a Box to allow overlay
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val screenHeight = maxHeight
    val screenWidth = maxWidth
    Log.d("Screen Dimensions", "Height: $screenHeight, Width: $screenWidth")
    // Scaffold containing the main UI elements
    Scaffold(
        topBar = {
          CenterAlignedTopAppBar(
              title = {
                Text(text = "Search Results", style = MaterialTheme.typography.titleMedium)
              },
              navigationIcon = {
                IconButton(onClick = { navigationActions.goBack() }) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back")
                }
              },
              actions = {
                IconButton(onClick = { /* Handle search */}) {
                  Icon(
                      imageVector = Icons.Default.Search,
                      contentDescription = "Search",
                      tint = colorScheme.onBackground)
                }
              },
              colors =
                  TopAppBarDefaults.centerAlignedTopAppBarColors(
                      containerColor = colorScheme.background),
          )
        }) { paddingValues ->
          // Main content inside the Scaffold
          Column(
              modifier = Modifier.fillMaxWidth().padding(paddingValues),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top) {
                      Text(
                          text = searchQuery,
                          style = poppinsTypography.labelMedium,
                          fontSize = 24.sp,
                          fontWeight = FontWeight.SemiBold,
                          textAlign = TextAlign.Center,
                      )
                      Text(
                          text = "This is a sample description for the $searchQuery result",
                          style = poppinsTypography.labelSmall,
                          fontWeight = FontWeight.Medium,
                          fontSize = 12.sp,
                          color = colorScheme.onSurface,
                          textAlign = TextAlign.Center,
                      )
                    }

                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = screenHeight * 0.02f, bottom = screenHeight * 0.01f)
                            .padding(horizontal = screenWidth * 0.02f)
                            .wrapContentHeight()
                            .testTag("filter_buttons_row"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                  // Tune Icon - fixed, non-scrollable
                  IconButton(
                      onClick = { showFilterButtons = !showFilterButtons },
                      modifier =
                          Modifier.padding(bottom = screenHeight * 0.01f).testTag("tuneButton"),
                      content = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint =
                                if (showFilterButtons) colorScheme.onPrimary
                                else colorScheme.onBackground,
                        )
                      },
                      colors =
                          IconButtonDefaults.iconButtonColors(
                              containerColor =
                                  if (showFilterButtons) colorScheme.primary
                                  else colorScheme.surface),
                  )

                  Spacer(modifier = Modifier.width(10.dp))

                  AnimatedVisibility(visible = showFilterButtons) {
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("lazy_filter_row")) {
                          items(listOfButtons.size) { index ->
                            QuickFixButton(
                                buttonText = listOfButtons[index].text,
                                onClickAction = listOfButtons[index].onClick,
                                buttonColor =
                                    if (listOfButtons[index].applied) colorScheme.primary
                                    else colorScheme.surface,
                                textColor =
                                    if (listOfButtons[index].applied) colorScheme.onPrimary
                                    else colorScheme.onBackground,
                                textStyle =
                                    poppinsTypography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium),
                                height = screenHeight * 0.05f,
                                leadingIcon = listOfButtons[index].leadingIcon,
                                trailingIcon = listOfButtons[index].trailingIcon,
                                leadingIconTint =
                                    if (listOfButtons[index].applied) colorScheme.onPrimary
                                    else colorScheme.onBackground,
                                trailingIconTint =
                                    if (listOfButtons[index].applied) colorScheme.onPrimary
                                    else colorScheme.onBackground,
                                contentPadding =
                                    PaddingValues(
                                        vertical = 0.dp, horizontal = screenWidth * 0.02f),
                                modifier =
                                    Modifier.testTag("filter_button_${listOfButtons[index].text}"))
                            Spacer(modifier = Modifier.width(screenHeight * 0.01f))
                          }
                        }
                  }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth().testTag("worker_profiles_list")) {
                  items(filteredWorkerProfiles.size) { index ->
                    val profile = filteredWorkerProfiles[index]
                    var account by remember { mutableStateOf<Account?>(null) }
                    var worker by remember { mutableStateOf<WorkerProfile?>(null) }
                    var distance by remember { mutableStateOf<Int?>(null) }
                    var cityName by remember { mutableStateOf<String?>(null) }

                    distance =
                        profile.location
                            ?.let { workerLocation ->
                              searchViewModel.calculateDistance(
                                  workerLocation.latitude,
                                  workerLocation.longitude,
                                  baseLocation!!.latitude,
                                  baseLocation!!.longitude)
                            }
                            ?.toInt()

                    LaunchedEffect(profile.uid) {
                      workerProfileViewModel.fetchUserProfile(
                          profile.uid, onResult = { worker = it as WorkerProfile })
                      accountViewModel.fetchUserAccount(profile.uid) { fetchedAccount: Account? ->
                        account = fetchedAccount
                      }
                    }

                    account?.let { acc ->
                      val locationName =
                          if (profile.location?.name.isNullOrEmpty()) "Unknown"
                          else profile.location?.name

                      locationName?.let {
                        cityName =
                            worker?.location?.let { it1 ->
                              worker!!.location?.let { it2 ->
                                getCityNameFromCoordinates(it1.latitude, it2.longitude)
                              }
                            }
                        cityName?.let { it1 ->
                          SearchWorkerProfileResult(
                              modifier = Modifier.testTag("worker_profile_result$index"),
                              profileImage = R.drawable.placeholder_worker,
                              name = "${acc.firstName} ${acc.lastName}",
                              category = profile.fieldOfWork,
                              rating = profile.reviews.map { review -> review.rating }.average(),
                              reviewCount = profile.reviews.size,
                              location = it1,
                              price = profile.price.toString(),
                              onBookClick = {
                                workerProfileViewModel.fetchUserProfile(
                                    acc.uid,
                                    onResult = {
                                      selectedWorker = it as WorkerProfile
                                      selectedCityName = cityName
                                      isWindowVisible = true
                                    })
                              },
                              distance = distance,
                          )
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(screenHeight * 0.004f))
                  }
                }
              }
        }

    QuickFixAvailabilityBottomSheet(
        showAvailabilityBottomSheet,
        onDismissRequest = { showAvailabilityBottomSheet = false },
        onOkClick = { days, hour, minute ->
          selectedDays = days
          selectedHour = hour
          selectedMinute = minute
          filteredWorkerProfiles =
              searchViewModel.filterWorkersByAvailability(
                  filteredWorkerProfiles, days, hour, minute)
          availabilityFilterApplied = true
        },
        onClearClick = {
          availabilityFilterApplied = false
          selectedDays = emptyList()
          selectedHour = 0
          selectedMinute = 0
          reapplyFilters()
        },
        clearEnabled = availabilityFilterApplied)

    searchSubcategory?.let {
      ChooseServiceTypeSheet(
          showServicesBottomSheet,
          it.tags,
          selectedServices = selectedServices,
          onApplyClick = { services ->
            selectedServices = services
            filteredWorkerProfiles =
                searchViewModel.filterWorkersByServices(filteredWorkerProfiles, selectedServices)
            servicesFilterApplied = true
          },
          onDismissRequest = { showServicesBottomSheet = false },
          onClearClick = {
            selectedServices = emptyList()
            servicesFilterApplied = false
            reapplyFilters()
          },
          clearEnabled = servicesFilterApplied)
    }

    QuickFixPriceRangeBottomSheet(
        showPriceRangeBottomSheet,
        onApplyClick = { start, end ->
          selectedPriceStart = start
          selectedPriceEnd = end
          filteredWorkerProfiles =
              searchViewModel.filterWorkersByPriceRange(filteredWorkerProfiles, start, end)
          priceFilterApplied = true
        },
        onDismissRequest = { showPriceRangeBottomSheet = false },
        onClearClick = {
          selectedPriceStart = 0
          selectedPriceEnd = 0
          priceFilterApplied = false
          reapplyFilters()
        },
        clearEnabled = priceFilterApplied)

    QuickFixLocationFilterBottomSheet(
        showLocationBottomSheet,
        userProfile = userProfile,
        phoneLocation = phoneLocation,
        onApplyClick = { location, max ->
          selectedLocation = location
          if (location == com.arygm.quickfix.model.locations.Location(0.0, 0.0, "Default")) {
            Toast.makeText(context, "Enable Location In Settings", Toast.LENGTH_SHORT).show()
          }
          baseLocation = location
          maxDistance = max
          filteredWorkerProfiles =
              searchViewModel.filterWorkersByDistance(filteredWorkerProfiles, location, max)
          locationFilterApplied = true
        },
        onDismissRequest = { showLocationBottomSheet = false },
        onClearClick = {
          baseLocation = phoneLocation
          selectedLocation = com.arygm.quickfix.model.locations.Location()
          maxDistance = 0
          locationFilterApplied = false
          reapplyFilters()
        },
        clearEnabled = locationFilterApplied)

    if (isWindowVisible) {
      QuickFixSlidingWindow(isVisible = isWindowVisible, onDismiss = { isWindowVisible = false }) {
        // Content of the sliding window
        Column(
            modifier =
                Modifier.clip(RoundedCornerShape(topStart = 25f, bottomStart = 25f))
                    .fillMaxWidth()
                    .background(colorScheme.background)
                    .testTag("sliding_window_content")) {

              // Top Bar
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(
                              screenHeight *
                                  0.23f) // Adjusted height to accommodate profile picture overlap
                          .testTag("sliding_window_top_bar")) {
                    // Banner Image
                    Image(
                        painter = painterResource(id = bannerImage),
                        contentDescription = "Banner",
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(screenHeight * 0.2f)
                                .testTag("sliding_window_banner_image"),
                        contentScale = ContentScale.Crop)

                    QuickFixButton(
                        buttonText = if (saved) "saved" else "save",
                        onClickAction = { saved = !saved },
                        buttonColor = colorScheme.surface,
                        textColor = colorScheme.onBackground,
                        textStyle = MaterialTheme.typography.labelMedium,
                        contentPadding = PaddingValues(horizontal = screenWidth * 0.01f),
                        modifier =
                            Modifier.align(Alignment.BottomEnd)
                                .width(screenWidth * 0.25f)
                                .offset(x = -(screenWidth * 0.04f))
                                .testTag(
                                    "sliding_window_save_button"), // Negative offset to position
                        // correctly,
                        leadingIcon =
                            if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder)

                    // Profile picture overlapping the banner image
                    Image(
                        painter = painterResource(id = profilePicture),
                        contentDescription = "Profile Picture",
                        modifier =
                            Modifier.size(screenHeight * 0.1f)
                                .align(Alignment.BottomStart)
                                .offset(x = screenWidth * 0.04f)
                                .clip(CircleShape)
                                .testTag("sliding_window_profile_picture"),
                        // Negative offset to position correctly
                        contentScale = ContentScale.Crop)
                  }

              // Worker Field and Address under the profile picture
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = screenWidth * 0.04f)
                          .testTag("sliding_window_worker_additional_info")) {
                    Text(
                        text = selectedWorker.fieldOfWork,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorScheme.onBackground,
                        modifier = Modifier.testTag("sliding_window_worker_category"))
                    selectedCityName?.let {
                      Text(
                          text = it,
                          style = MaterialTheme.typography.headlineSmall,
                          color = colorScheme.onBackground,
                          modifier = Modifier.testTag("sliding_window_worker_address"))
                    }
                  }

              // Main content should be scrollable
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .verticalScroll(rememberScrollState())
                          .background(colorScheme.surface)
                          .testTag("sliding_window_scrollable_content")) {
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // Description with "Show more" functionality
                    var showFullDescription by remember { mutableStateOf(false) }
                    val descriptionText =
                        if (showFullDescription || selectedWorker.description.length <= 100) {
                          selectedWorker.description
                        } else {
                          selectedWorker.description.take(100) + "..."
                        }

                    Text(
                        text = descriptionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface,
                        modifier =
                            Modifier.padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_description"))

                    if (selectedWorker.description.length > 100) {
                      Text(
                          text = if (showFullDescription) "Show less" else "Show more",
                          style =
                              MaterialTheme.typography.bodySmall.copy(color = colorScheme.primary),
                          modifier =
                              Modifier.padding(horizontal = screenWidth * 0.04f)
                                  .clickable { showFullDescription = !showFullDescription }
                                  .testTag("sliding_window_description_show_more_button"))
                    }

                    // Delimiter between description and services
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_horizontal_divider_1"),
                        thickness = 1.dp,
                        color = colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // Services Section
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_services_row")) {
                          // Included Services
                          Column(
                              modifier =
                                  Modifier.weight(1f)
                                      .testTag("sliding_window_included_services_column")) {
                                Text(
                                    text = "Included Services",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                                selectedWorker.includedServices.forEach { service ->
                                  val name = service.name
                                  Text(
                                      text = "• $name",
                                      style = MaterialTheme.typography.bodySmall,
                                      color = colorScheme.onSurface,
                                      modifier = Modifier.padding(bottom = screenHeight * 0.005f))
                                }
                              }

                          Spacer(modifier = Modifier.width(screenWidth * 0.02f))

                          // Add-On Services
                          Column(
                              modifier =
                                  Modifier.weight(1f)
                                      .testTag("sliding_window_addon_services_column")) {
                                Text(
                                    text = "Add-On Services",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colorScheme.primary)
                                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                                selectedWorker.addOnServices.forEach { service ->
                                  val name = service.name
                                  Text(
                                      text = "• $name",
                                      style = MaterialTheme.typography.bodySmall,
                                      color = colorScheme.primary,
                                      modifier = Modifier.padding(bottom = screenHeight * 0.005f))
                                }
                              }
                        }

                    Spacer(modifier = Modifier.height(screenHeight * 0.03f))

                    // Continue Button with Rate/HR
                    QuickFixButton(
                        buttonText = "Continue",
                        onClickAction = { /* Handle continue */},
                        buttonColor = colorScheme.primary,
                        textColor = colorScheme.onPrimary,
                        textStyle = MaterialTheme.typography.labelMedium,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_continue_button"))

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_horizontal_divider_2"),
                        thickness = 1.dp,
                        color = colorScheme.onSurface.copy(alpha = 0.2f),
                    )
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    // Tags Section
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = screenWidth * 0.04f))
                    Spacer(modifier = Modifier.height(screenHeight * 0.01f))

                    // Display tags using FlowRow for wrapping
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(screenWidth * 0.02f),
                        verticalArrangement = Arrangement.spacedBy(screenHeight * 0.01f),
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_tags_flow_row"),
                    ) {
                      selectedWorker.tags.forEach { tag ->
                        Text(
                            text = tag,
                            color = colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier =
                                Modifier.border(
                                        width = 1.dp,
                                        color = colorScheme.primary,
                                        shape = MaterialTheme.shapes.small)
                                    .padding(
                                        horizontal = screenWidth * 0.02f,
                                        vertical = screenHeight * 0.005f))
                      }
                    }

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_horizontal_divider_3"),
                        thickness = 1.dp,
                        color = colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))

                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = screenWidth * 0.04f))
                    Spacer(modifier = Modifier.height(screenHeight * 0.01f))

                    // Star Rating Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier.padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_star_rating_row")) {
                          RatingBar(
                              selectedWorker.rating.toFloat(), modifier = Modifier.height(20.dp))
                        }
                    Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                    LazyRow(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = screenWidth * 0.04f)
                                .testTag("sliding_window_reviews_row")) {
                          itemsIndexed(selectedWorker.reviews) { index, review ->
                            var isExpanded by remember { mutableStateOf(false) }
                            val displayText =
                                if (isExpanded || review.review.length <= 100) {
                                  review.review
                                } else {
                                  review.review.take(100) + "..."
                                }

                            Box(
                                modifier =
                                    Modifier.padding(end = screenWidth * 0.02f)
                                        .width(screenWidth * 0.6f)
                                        .clip(RoundedCornerShape(25f))
                                        .background(colorScheme.background)) {
                                  Column(modifier = Modifier.padding(screenWidth * 0.02f)) {
                                    Text(
                                        text = displayText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurface)
                                    if (review.review.length > 100) {
                                      Text(
                                          text = if (isExpanded) "See less" else "See more",
                                          style =
                                              MaterialTheme.typography.bodySmall.copy(
                                                  color = colorScheme.primary),
                                          modifier =
                                              Modifier.clickable { isExpanded = !isExpanded }
                                                  .padding(top = screenHeight * 0.01f))
                                    }
                                  }
                                }
                          }
                        }

                    Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                  }
            }
      }
    }
  }
}
