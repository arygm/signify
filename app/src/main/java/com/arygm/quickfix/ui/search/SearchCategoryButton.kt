package com.arygm.quickfix.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arygm.quickfix.model.categories.painting.PaintingCategory
import com.arygm.quickfix.ui.theme.poppinsFontFamily

@Composable
fun ExpandableCategoryItem(
    item: ExpandableCategory,
    isExpanded: Boolean,
    backgroundColor: Color = colorScheme.surface,
    onExpandedChange: (Boolean) -> Unit,
    size: Dp
) {
    val subCategories = remember { PaintingCategory.entries.toList() }
    val interactionSource = remember { MutableInteractionSource() }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .background(color = colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                onExpandedChange(!isExpanded)
            }
    ) {
        Row(
            modifier =
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp).background(backgroundColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = item.category.icon,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(size).testTag("categoryIcon")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Text Column
            Column(
                modifier = Modifier.weight(5f)
            ) {
                Text(
                    text = item.category.displayName,
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = poppinsFontFamily,
                    fontSize = 16.sp
                )
                Text(
                    text = item.category.description,
                    color = colorScheme.onSecondary,
                    fontWeight = FontWeight.Medium,
                    fontFamily = poppinsFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.graphicsLayer(rotationZ = rotationAngle).weight(1f)
            )

        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                subCategories.forEach {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(10f).clickable { },
                            text = it.displayName,
                            color = colorScheme.onSecondary,
                            fontWeight = FontWeight.Medium,
                            fontFamily = poppinsFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.weight(1f).clickable { }
                        )
                        Spacer(modifier = Modifier.height(10.dp))                    }

                }
            }
        }
    }
}