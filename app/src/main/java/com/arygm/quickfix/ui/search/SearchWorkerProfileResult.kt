package com.arygm.quickfix.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arygm.quickfix.ui.elements.QuickFixButton
import com.arygm.quickfix.ui.theme.poppinsFontFamily
import com.arygm.quickfix.ui.theme.poppinsTypography

@Composable
fun SearchWorkerProfileResult(
    modifier: Modifier = Modifier,
    profileImage: Int,
    name: String,
    category: String,
    rating: Float,
    reviewCount: Int,
    location: String,
    price: String,
    onBookClick: () -> Unit
) {
  Card(
      shape = RoundedCornerShape(8.dp),
      modifier = modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 10.dp),
      elevation = CardDefaults.cardElevation(10.dp),
      colors = CardDefaults.cardColors().copy(containerColor = colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              Image(
                  painter = painterResource(id = profileImage),
                  contentDescription = "Profile image of $name, $category",
                  modifier = Modifier.clip(RoundedCornerShape(8.dp)).size(100.dp).aspectRatio(1f),
                  contentScale = ContentScale.FillBounds)

              Spacer(modifier = Modifier.width(8.dp))

              Column(
                  modifier = Modifier.weight(0.6f).height(100.dp),
                  verticalArrangement = Arrangement.SpaceAround,
              ) {
                Column(
                    verticalArrangement = Arrangement.Top,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$rating ★",
                        fontFamily = poppinsFontFamily,
                        color = colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium)
                    Text(
                        text = "($reviewCount+)",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp))
                  }

                  Text(
                      text = category,
                      fontSize = 15.sp,
                      fontWeight = FontWeight.SemiBold,
                      fontFamily = poppinsFontFamily,
                      color = colorScheme.onBackground)

                  Text(text = name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                  Text(
                      text = "CHF",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold,
                      lineHeight = 20.sp,
                      color = colorScheme.onBackground)
                  Text(
                      text = price,
                      fontSize = 19.sp,
                      fontWeight = FontWeight.Bold,
                      lineHeight = 20.sp,
                      color = colorScheme.onBackground)
                  Text(
                      text = "/Hour",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.SemiBold,
                      lineHeight = 20.sp,
                      color = colorScheme.onSurface)
                }
              }

              Column(
                  horizontalAlignment = Alignment.End,
                  verticalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.weight(0.4f).height(100.dp).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.LocationOn,
                          contentDescription = "Location",
                          tint = colorScheme.onSurface,
                          modifier = Modifier.size(16.dp))
                      Text(text = location, fontSize = 11.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                      QuickFixButton(
                          onClickAction = onBookClick,
                          buttonText = "Book",
                          textStyle =
                              poppinsTypography.bodyMedium.copy(
                                  fontWeight = FontWeight.SemiBold,
                                  fontSize = 16.sp,
                                  lineHeight = 20.sp),
                          buttonColor = colorScheme.primary,
                          textColor = colorScheme.onPrimary,
                          contentPadding = PaddingValues(0.dp),
                          height = 30.dp)
                    }
                  }
            }
      }
}