package com.arygm.quickfix.ui.uiMode.appContentUI.userModeUI.profile.becomeWorker.views.professional

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arygm.quickfix.model.category.Category
import com.arygm.quickfix.model.category.Subcategory
import com.arygm.quickfix.model.profile.dataFields.AddOnService
import com.arygm.quickfix.model.profile.dataFields.IncludedService
import com.arygm.quickfix.ressources.C
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.elements.QuickFixTextFieldCustom
import com.arygm.quickfix.ui.navigation.NavigationActions
import com.arygm.quickfix.ui.theme.poppinsTypography
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalInfoScreen(
    pagerState: PagerState,
    price: MutableDoubleState,
    fieldOfWork: MutableState<String>,
    includedServices: MutableState<List<IncludedService>>,
    addOnServices: MutableState<List<AddOnService>>,
    tags: MutableState<List<String>>,
    categories: List<Category>,
    formValidatedTest: Boolean = false,
    navigationActions: NavigationActions,
    workingHours: MutableState<Pair<LocalTime?, LocalTime?>>
) {
  val coroutineScope = rememberCoroutineScope()
  val formValidatedIncludedServices = remember { mutableStateOf(false) }
  val formValidatedAddOnServices = remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf(Category()) }
  var selectedSubcategory by remember { mutableStateOf(Subcategory()) }
  var expandedDropDownCategory by remember { mutableStateOf(false) }
  var expandedDropDownSubcategory by remember { mutableStateOf(false) }
  val startTime = remember { mutableStateOf<LocalTime?>(null) }
  val endTime = remember { mutableStateOf<LocalTime?>(null) }
  workingHours.value = Pair(startTime.value, endTime.value)
  val (listServices, checkedStatesIncludedServices) =
      remember(selectedSubcategory) {
        val services = selectedSubcategory.setServices
        val checkedStates =
            mutableStateListOf<Boolean>().apply { repeat(services.size) { add(false) } }
        services to checkedStates
      }
  val listAddOnServicesFromSet by
      remember(listServices, checkedStatesIncludedServices) {
        derivedStateOf {
          Log.d(
              "listAddOnServicesFromSet",
              listServices
                  .filterIndexed { index, _ ->
                    index < checkedStatesIncludedServices.size &&
                        !checkedStatesIncludedServices[index]
                  }
                  .toString())
          listServices.filterIndexed { index, _ ->
            index < checkedStatesIncludedServices.size && !checkedStatesIncludedServices[index]
          }
        }
      }

  // Initialize checkedStatesAddOnServices when listAddOnServices changes
  val checkedStatesAddOnServices =
      remember(listAddOnServicesFromSet) {
        mutableStateListOf<Boolean>().apply { repeat(listAddOnServicesFromSet.size) { add(false) } }
      }

  val textFieldList = remember { mutableStateListOf<MutableState<String>>() }

  val canAddTextField = remember { mutableStateOf(true) }

  val (listTags, checkedStatesTags) =
      remember(selectedSubcategory) {
        val services = selectedSubcategory.tags
        val checkedStates =
            mutableStateListOf<Boolean>().apply { repeat(services.size) { add(false) } }
        services to checkedStates
      }
  val formValidatedTags = remember { mutableStateOf(false) }
  val priceString = remember { mutableStateOf("") }
  val priceError = remember { mutableStateOf(false) } // Tracks if there's an error
  val rotationAngleCategory by
      animateFloatAsState(targetValue = if (expandedDropDownCategory) 180f else 0f, label = "")
  val rotationAngleSubcategory by
      animateFloatAsState(targetValue = if (expandedDropDownSubcategory) 180f else 0f, label = "")
  val items =
      listOf(
          "Professional Info",
          "Working hours",
          "Category",
          "Reference Price",
          "Tags",
          "Included Services",
          "Add-on Services",
          "Continue")
  val conditions =
      listOf(
          true,
          true,
          true,
          selectedCategory.name.isNotEmpty() && selectedSubcategory.name.isNotEmpty(),
          selectedCategory.name.isNotEmpty() && selectedSubcategory.name.isNotEmpty(),
          selectedCategory.name.isNotEmpty() && selectedSubcategory.name.isNotEmpty(),
          formValidatedIncludedServices.value ||
              formValidatedTest ||
              (!formValidatedIncludedServices.value && checkedStatesAddOnServices.any { it }),
          formValidatedAddOnServices.value || formValidatedTest)
  val listState = rememberLazyListState() // State for LazyColumn
  val visibleItems = items.zip(conditions).filter { it.second }.map { it.first } // Filtered items
  val categoryTextStyle =
      MaterialTheme.typography.labelMedium.copy(
          fontSize = 10.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)

  // Scroll to the last visible item when the visible items list changes
  LaunchedEffect(visibleItems) {
    if (visibleItems.isNotEmpty()) {
      smoothScrollToItem(listState, visibleItems.size - 1)
    }
  }

  BoxWithConstraints {
    val widthRatio = maxWidth / 411
    val heightRatio = maxHeight / 860
    val sizeRatio = minOf(widthRatio, heightRatio)
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
      item(key = "Professional Info") {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
          Text(
              "Professional Info",
              style =
                  poppinsTypography.headlineMedium.copy(
                      fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
              color = colorScheme.onBackground,
              modifier = Modifier.semantics { testTag = C.Tag.professionalInfoScreenSectionTitle })
          Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "This is your time to shine. Let potential buyers know what you do best and how you gained your skills, certifications and experience.",
                style =
                    poppinsTypography.headlineMedium.copy(
                        fontSize = 9.sp, fontWeight = FontWeight.Medium),
                color = colorScheme.onSurface,
                modifier =
                    Modifier.weight(0.8f).semantics {
                      testTag = C.Tag.professionalInfoScreenSectionDescription
                    })
            Spacer(modifier = Modifier.weight(0.2f))
          }
          Spacer(modifier = Modifier.height(16.dp * heightRatio.value))
        }
      }
      item(key = "Working hours") {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
          Row() {
            Box(
                modifier = Modifier.weight(0.25f).align(Alignment.Bottom),
            ) {
              TimeDropdownMenu(
                  selectedTime = startTime,
                  dependentTime = endTime, // Pass the dependent time (endTime)
                  isStartTime = true, // This is the Start Time dropdown
                  widthRatio = widthRatio,
                  heightRatio = heightRatio,
                  tag = "StartTime")
            }
            HorizontalDivider(
                modifier =
                    Modifier.weight(0.1f)
                        .align(Alignment.CenterVertically)
                        .padding(top = 20.dp, end = 12.dp))
            Box(
                modifier = Modifier.weight(0.65f).align(Alignment.Bottom),
            ) {
              TimeDropdownMenu(
                  showLabel = false,
                  selectedTime = endTime,
                  dependentTime = startTime, // Pass the Start Time as dependency
                  isStartTime = false, // This is the End Time dropdown
                  widthRatio = widthRatio,
                  heightRatio = heightRatio,
                  startTime = startTime.value ?: LocalTime.of(0, 0), // Default start time
                  tag = "EndTime")
            }
          }
        }
      }

      item(key = "Category") {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
          Row(modifier = Modifier.fillMaxWidth()) {

            // Calculate maximum text width for categories
            val maxCategoryTextWidth =
                calculateMaxTextWidth(
                    texts = categories.map { it.name }, textStyle = categoryTextStyle)

            // Add padding or extra space if needed
            val dropdownMenuWidth = maxCategoryTextWidth + 40.dp
            Box(
                modifier = Modifier.weight(0.465f).align(Alignment.Bottom),
            ) {
              QuickFixTextFieldCustom(
                  modifier =
                      Modifier.semantics { testTag = C.Tag.professionalInfoScreenCategoryField },
                  widthField = 380.dp * widthRatio.value,
                  value = selectedCategory.name,
                  onValueChange = {},
                  shape = RoundedCornerShape(8.dp),
                  showLabel = true,
                  label =
                      @Composable {
                        Text(
                            text =
                                buildAnnotatedString {
                                  append("Your Occupation")
                                  withStyle(
                                      style =
                                          SpanStyle(
                                              color = colorScheme.primary,
                                              fontSize = 12.sp,
                                              fontWeight = FontWeight.Medium)) {
                                        append("*")
                                      }
                                },
                            style =
                                poppinsTypography.headlineMedium.copy(
                                    fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            color = colorScheme.onBackground)
                      },
                  hasShadow = false,
                  borderColor = colorScheme.tertiaryContainer,
                  placeHolderText = "Select Occupation",
                  isTextField = false,
                  alwaysShowTrailingIcon = true,
                  trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "dropdown",
                        tint = colorScheme.onSecondaryContainer,
                        modifier =
                            Modifier.size(30.dp).graphicsLayer(rotationZ = rotationAngleCategory),
                    )
                  },
                  iconButtonOnClickable = { expandedDropDownCategory = !expandedDropDownCategory },
                  moveTrailingIconLeft = 2.dp,
                  onTextFieldClick = { expandedDropDownCategory = !expandedDropDownCategory },
                  singleLine = false,
                  heightInEnabled = true,
                  minHeight = 27.dp * heightRatio.value, // Set default height
                  maxHeight =
                      54.dp * heightRatio.value, // Allow expansion up to double the default height
                  maxLines = 2,
              )
              DropdownMenu(
                  expanded = expandedDropDownCategory,
                  onDismissRequest = { expandedDropDownCategory = false },
                  modifier =
                      Modifier.width(dropdownMenuWidth * widthRatio.value).semantics {
                        testTag = C.Tag.professionalInfoScreenCategoryDropdownMenu
                      },
                  containerColor = colorScheme.surface) {
                    categories.forEachIndexed { index, category ->
                      DropdownMenuItem(
                          text = { Text(text = category.name, style = categoryTextStyle) },
                          onClick = {
                            selectedCategory = category
                            selectedSubcategory = Subcategory()
                            expandedDropDownCategory = false
                            formValidatedIncludedServices.value = false
                            formValidatedAddOnServices.value = false
                            textFieldList.clear()
                            addOnServices.value = emptyList()
                            includedServices.value = emptyList()
                            fieldOfWork.value = ""
                            price.doubleValue = 0.0
                            canAddTextField.value = true
                            priceString.value = ""
                            priceError.value = false
                          },
                          modifier =
                              Modifier.height(30.dp * heightRatio.value).semantics {
                                testTag =
                                    C.Tag.professionalInfoScreenCategoryDropdownMenuItem + index
                              })
                    }
                  }
            }

            Spacer(modifier = Modifier.weight(0.07f))
            val maxSubcategoryTextWidth =
                calculateMaxTextWidth(
                    texts = selectedCategory.subcategories.map { it.name },
                    textStyle = categoryTextStyle)

            val subDropdownMenuWidth = maxSubcategoryTextWidth + 40.dp
            Column(
                modifier = Modifier.weight(0.465f),
            ) {
              Spacer(modifier = Modifier.height(40.dp * heightRatio.value))
              QuickFixTextFieldCustom(
                  modifier =
                      Modifier.semantics { testTag = C.Tag.professionalInfoScreenSubcategoryField },
                  heightField = 27.dp * heightRatio.value,
                  widthField = 380.dp * widthRatio.value,
                  value = selectedSubcategory.name,
                  onValueChange = {},
                  shape = RoundedCornerShape(8.dp),
                  hasShadow = false,
                  borderColor = colorScheme.tertiaryContainer,
                  placeHolderText = "Select Occupation",
                  isTextField = false,
                  alwaysShowTrailingIcon = true,
                  trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "dropdown",
                        tint = colorScheme.onSecondaryContainer,
                        modifier =
                            Modifier.size(30.dp)
                                .graphicsLayer(rotationZ = rotationAngleSubcategory))
                  },
                  iconButtonOnClickable = {
                    expandedDropDownSubcategory = !expandedDropDownSubcategory
                  },
                  moveTrailingIconLeft = 2.dp,
                  onTextFieldClick = { expandedDropDownSubcategory = !expandedDropDownSubcategory },
                  enabled = selectedCategory.name.isNotEmpty(),
                  singleLine = false,
                  heightInEnabled = true,
                  minHeight = 27.dp * heightRatio.value, // Set default height
                  maxHeight =
                      54.dp * heightRatio.value, // Allow expansion up to double the default height
                  maxLines = 2)
              DropdownMenu(
                  expanded = expandedDropDownSubcategory,
                  onDismissRequest = { expandedDropDownSubcategory = false },
                  modifier =
                      Modifier.width(subDropdownMenuWidth * widthRatio.value).semantics {
                        testTag = C.Tag.professionalInfoScreenSubcategoryDropdownMenu
                      },
                  containerColor = colorScheme.surface) {
                    selectedCategory.subcategories.forEachIndexed { index, subcategory ->
                      DropdownMenuItem(
                          text = { Text(text = subcategory.name, style = categoryTextStyle) },
                          onClick = {
                            selectedSubcategory = subcategory
                            expandedDropDownSubcategory = false
                            fieldOfWork.value = selectedSubcategory.name
                          },
                          modifier =
                              Modifier.height(30.dp * heightRatio.value).semantics {
                                testTag =
                                    C.Tag.professionalInfoScreenSubcategoryDropdownMenuItem + index
                              })
                    }
                  }
            }
          }
          Spacer(modifier = Modifier.height(16.dp * heightRatio.value))
        }
      }
      if (selectedCategory.name.isNotEmpty() && selectedSubcategory.name.isNotEmpty()) {

        item(key = "Reference Price") {
          Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
                Column {
                  Text(
                      text =
                          buildAnnotatedString {
                            append("Reference Price")
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium)) {
                                  append("*")
                                }
                          },
                      style =
                          poppinsTypography.headlineMedium.copy(
                              fontSize = 12.sp, fontWeight = FontWeight.Medium),
                      color = colorScheme.onBackground,
                      modifier =
                          Modifier.semantics {
                            testTag = C.Tag.professionalInfoScreenLabelReferencePrice
                          })
                  Spacer(modifier = Modifier.height(1.dp * heightRatio.value))

                  Row {
                    selectedSubcategory.scale?.let {
                      Log.d("scale", it.longScale)
                      Text(
                          text = it.longScale,
                          style =
                              poppinsTypography.headlineMedium.copy(
                                  fontSize = 9.sp, fontWeight = FontWeight.Medium),
                          color = colorScheme.onSurface,
                          modifier =
                              Modifier.weight(0.8f).semantics {
                                testTag = C.Tag.professionalInfoScreenSectionScale
                              })
                    }
                  }
                  Spacer(modifier = Modifier.height(2.dp * heightRatio.value))
                  Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(0.4f)) {
                      QuickFixTextFieldCustom(
                          modifier =
                              Modifier.semantics {
                                testTag = C.Tag.professionalInfoScreenPriceField
                              },
                          heightField = 27.dp,
                          widthField = 380.dp * widthRatio.value,
                          value = priceString.value,
                          onValueChange = { input ->
                            priceString.value = input

                            // Try to parse the input to double
                            val parsedPrice = input.toDoubleOrNull()
                            if (parsedPrice != null) {
                              price.doubleValue = parsedPrice
                              priceError.value = false
                            } else {
                              // Parsing failed, set error
                              priceError.value = true
                            }
                          },
                          shape = RoundedCornerShape(8.dp),
                          hasShadow = false,
                          borderColor = colorScheme.tertiaryContainer,
                          placeHolderText = "Enter Price",
                          isError = priceError.value,
                          errorText = "Please enter a valid number",
                          showError = priceError.value,
                          keyboardOptions =
                              KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                          trailingText = {
                            Text(
                                text = "CHF",
                                style =
                                    poppinsTypography.headlineMedium.copy(
                                        fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(end = 8.dp))
                          },
                          alwaysShowTrailingIcon = true,
                          moveTrailingIconLeft = 20.dp)
                    }
                    Spacer(modifier = Modifier.weight(0.6f))
                  }
                  Spacer(modifier = Modifier.height(16.dp * heightRatio.value))
                }
              }
        }
        item(key = "Tags") {
          val indices = (0 until listTags.size step 2)
          Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
                QuickFixCheckedList(
                    listServices = listTags,
                    checkedStatesServices = checkedStatesTags,
                    heightRatio = heightRatio,
                    indices = indices,
                    minToSelect = 3,
                    maxToSelect = listTags.size,
                    onClickActionOk = {
                      tags.value = listTags.filterIndexed { index, _ -> checkedStatesTags[index] }
                      formValidatedTags.value = true
                    },
                    formValidated = formValidatedTags,
                    boldText = " at least 3 Tags",
                    label = "Choose",
                    secondPartLabel = " that describes your skills",
                    widthRatio = widthRatio,
                    modifier =
                        Modifier.semantics { testTag = C.Tag.professionalInfoScreenTagsList },
                    testTagPrecision = "Tags")
                Spacer(modifier = Modifier.height(16.dp * heightRatio.value))
              }
        }

        item(key = "Included Services") {
          Column(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
                val indices = (0 until listServices.size step 2)
                QuickFixCheckedList(
                    listServices = listServices,
                    checkedStatesServices = checkedStatesIncludedServices,
                    heightRatio = heightRatio,
                    indices = indices,
                    minToSelect = 5,
                    maxToSelect = listServices.size,
                    onClickActionOk = {
                      includedServices.value =
                          listServices
                              .filterIndexed { index, _ -> checkedStatesIncludedServices[index] }
                              .map { IncludedService(it) }
                      formValidatedIncludedServices.value = true
                    },
                    formValidated = formValidatedIncludedServices,
                    boldText = " at least 5 Included services",
                    label = "Choose",
                    secondPartLabel = " in your ${selectedCategory.id} job from this set",
                    widthRatio = widthRatio,
                    modifier =
                        Modifier.semantics {
                          testTag = C.Tag.professionalInfoScreenIncludedServicesList
                        },
                    testTagPrecision = "Included Services")
                Spacer(modifier = Modifier.height(16.dp * heightRatio.value))
              }
        }
        if (formValidatedIncludedServices.value ||
            formValidatedTest ||
            (!formValidatedIncludedServices.value && checkedStatesAddOnServices.any { it })) {
          item(key = "Add-on Services") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
                  val indices = (0 until listAddOnServicesFromSet.size step 2)
                  QuickFixCheckedList(
                      modifier =
                          Modifier.semantics {
                            testTag = C.Tag.professionalInfoScreenAddOnServicesList
                          },
                      listServices = listAddOnServicesFromSet,
                      checkedStatesServices = checkedStatesAddOnServices,
                      heightRatio = heightRatio,
                      indices = indices,
                      minToSelect = 4,
                      maxToSelect = listAddOnServicesFromSet.size,
                      onClickActionOk = {
                        val filteredAddOnServices =
                            listAddOnServicesFromSet
                                .filterIndexed { index, _ -> checkedStatesAddOnServices[index] }
                                .map { AddOnService(it) }

                        // Map textFieldList to AddOnService
                        val textFieldAddOnServices = textFieldList.map { AddOnService(it.value) }

                        // Combine both lists and assign to addOnServices.value
                        addOnServices.value = filteredAddOnServices + textFieldAddOnServices
                        formValidatedAddOnServices.value = true
                        canAddTextField.value = false
                      },
                      formValidated = formValidatedAddOnServices,
                      boldText = " At least 4 Add-on services",
                      label = "Choose",
                      secondPartLabel =
                          " in your ${selectedCategory.id} job from the set or from yourself",
                      widthRatio = widthRatio,
                      isTextFieldList = true,
                      textFieldList = textFieldList,
                      canAddTextField = canAddTextField,
                      testTagPrecision = "Add-On Services")
                }
          }
          if (formValidatedAddOnServices.value || formValidatedTest) {
            item(key = "Continue") {
              Column(
                  modifier =
                      Modifier.fillMaxWidth().padding(horizontal = 16.dp * widthRatio.value)) {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(start = 5.dp, end = 5.dp, bottom = 8.dp)) {
                          QuickFixButton(
                              buttonText = "Cancel",
                              onClickAction = { navigationActions.goBack() },
                              buttonColor = colorScheme.surface,
                              textColor = colorScheme.error,
                              modifier =
                                  Modifier.weight(0.5f).semantics {
                                    testTag = C.Tag.professionalInfoScreencancelButton
                                  },
                              textStyle =
                                  poppinsTypography.headlineMedium.copy(
                                      fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
                          QuickFixButton(
                              buttonText = "Continue",
                              onClickAction = {
                                coroutineScope.launch {
                                  pagerState.scrollToPage(pagerState.currentPage + 1)
                                }
                              },
                              buttonColor = colorScheme.primary,
                              enabled =
                                  formValidatedAddOnServices.value &&
                                      formValidatedIncludedServices.value &&
                                      formValidatedTags.value &&
                                      priceError.value.not() &&
                                      selectedCategory.name.isNotEmpty() &&
                                      selectedSubcategory.name.isNotEmpty() &&
                                      price.doubleValue != 0.0 &&
                                      workingHours.value.first != null &&
                                      workingHours.value.second != null,
                              textColor = colorScheme.onPrimary,
                              textStyle =
                                  poppinsTypography.headlineMedium.copy(
                                      fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                              modifier =
                                  Modifier.weight(0.5f).semantics {
                                    testTag = C.Tag.professionalInfoScreencontinueButton
                                  },
                          )
                        }
                  }
            }
          }
        }
      }
    }
  }
}

@Composable
fun calculateMaxTextWidth(texts: List<String>, textStyle: TextStyle): Dp {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current

  var maxWidthPx = 0f

  texts.forEach { text ->
    val textLayoutResult = textMeasurer.measure(text = text, style = textStyle, maxLines = 1)
    val width = textLayoutResult.size.width.toFloat()
    if (width > maxWidthPx) {
      maxWidthPx = width
    }
  }

  // Convert pixels to Dp
  return with(density) { maxWidthPx.toDp() }
}

suspend fun smoothScrollToItem(
    listState: LazyListState,
    targetIndex: Int,
    durationMillis: Int = 1 // Total animation duration
) {
  val startIndex = listState.firstVisibleItemIndex
  val distance = targetIndex - startIndex
  val stepDuration = durationMillis / distance.coerceAtLeast(1)

  for (index in startIndex until targetIndex) {
    listState.animateScrollToItem(index)
    delay(stepDuration.toLong())
  }
}
