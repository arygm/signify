package com.arygm.quickfix.ui.elements

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.Dp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuickFixPriceRange(
    modifier: Modifier,
    rangeColor: Color,
    backColor: Color,
    barHeight: Dp,
    circleRadius: Dp,
    cornerRadius: CornerRadius,
    minValue: Int,
    maxValue: Int,
    progress1InitialValue: Int,
    progress2InitialValue: Int,
    tooltipSpacing: Dp,
    tooltipWidth: Dp,
    tooltipHeight: Dp,
    tooltipTriangleSize: Dp,
    onProgressChanged: (value1: Int, value2: Int) -> Unit
) {

  val circleRadiusInPx = with(LocalDensity.current) { circleRadius.toPx() }

  val primaryColor = MaterialTheme.colorScheme.primary
  val surfaceColor = MaterialTheme.colorScheme.surface

  val range = if (maxValue != minValue) (maxValue - minValue) else 1

  var progress1 by remember { mutableStateOf((progress1InitialValue - minValue).toFloat() / range) }
  var progress2 by remember { mutableStateOf((progress2InitialValue - minValue).toFloat() / range) }

  LaunchedEffect(Unit) {
    val value1 = minValue + (progress1 * range).roundToInt()
    val value2 = minValue + (progress2 * range).roundToInt()
    onProgressChanged(value1, value2)
  }

  var width by remember { mutableStateOf(0f) }

  var height by remember { mutableStateOf(0f) }

  var leftCircleDragging by remember { mutableStateOf(false) }

  var rightCircleDragging by remember { mutableStateOf(false) }

  val leftTooltipOverlapping = remember { mutableStateOf(false) }

  var leftCircleOffset by remember { mutableStateOf(Offset.Zero) }
  var rightCircleOffset by remember { mutableStateOf(Offset.Zero) }

  val scaleAnim1 by
      animateFloatAsState(
          targetValue = if (leftCircleDragging) 2f else 1f,
          animationSpec = tween(durationMillis = 300))

  val scaleAnim2 by
      animateFloatAsState(
          targetValue = if (rightCircleDragging) 2f else 1f,
          animationSpec = tween(durationMillis = 300))

  val tooltipAnim1 by
      animateFloatAsState(
          targetValue = if (leftTooltipOverlapping.value) -180f else 0f,
          animationSpec = tween(durationMillis = 300))

  val path = remember { Path() }

  val textMeasurer = rememberTextMeasurer()

  Canvas(
      modifier =
          modifier
              .height(barHeight)
              .pointerInteropFilter(
                  onTouchEvent = { motionEvent ->
                    when (motionEvent.action) {
                      MotionEvent.ACTION_DOWN -> {
                        val x = motionEvent.x
                        val y = motionEvent.y
                        val dis1 =
                            sqrt((x - leftCircleOffset.x).pow(2) + (y - leftCircleOffset.y).pow(2))
                        val dis2 =
                            sqrt(
                                (x - rightCircleOffset.x).pow(2) + (y - rightCircleOffset.y).pow(2))

                        if (dis1 < circleRadiusInPx) { // left circle clicked
                          leftCircleDragging = true
                        } else if (dis2 < circleRadiusInPx) { // right circle clicked
                          rightCircleDragging = true
                        }
                      }
                      MotionEvent.ACTION_MOVE -> {
                        var x = motionEvent.x.coerceIn(0f, width)

                        if (leftCircleDragging) {
                          x = x.coerceAtMost(rightCircleOffset.x)
                          progress1 = x / width
                          leftCircleOffset = Offset(x = x, y = height / 2f)
                        } else if (rightCircleDragging) {
                          x = x.coerceAtLeast(leftCircleOffset.x)
                          progress2 = x / width
                          rightCircleOffset = Offset(x = x, y = height / 2f)
                        }
                      }
                      MotionEvent.ACTION_UP -> {
                        leftCircleDragging = false
                        rightCircleDragging = false
                        val value1 = minValue + (progress1 * range).roundToInt()
                        val value2 = minValue + (progress2 * range).roundToInt()
                        onProgressChanged(value1, value2)
                      }
                    }
                    true
                  })
              .onGloballyPositioned {
                width = it.size.width.toFloat()
                height = it.size.height.toFloat()

                leftCircleOffset = Offset(x = width * progress1, y = height / 2f)
                rightCircleOffset = Offset(x = width * progress2, y = height / 2f)
              }
              .semantics { testTag = "QuickFixPriceRange" } // Add test tag to the Canvas
      ) {
        val barTop = height / 2f - barHeight.toPx() / 4f
        val barHeightPx = barHeight.toPx() / 2f

        drawRoundRect(
            color = backColor,
            cornerRadius = cornerRadius,
            topLeft = Offset(x = 0f, y = barTop),
            size = Size(width = width, height = barHeightPx))

        // Draw inner rect (between two circles)
        drawRect(
            color = rangeColor,
            topLeft = Offset(x = width * progress1, y = barTop),
            size = Size(width = width * (progress2 - progress1), height = barHeightPx))

        // Draw left circle
        scale(scaleAnim1, pivot = leftCircleOffset) {
          drawCircle(
              color = rangeColor.copy(alpha = 0.2f),
              radius = circleRadius.toPx(),
              center = leftCircleOffset)
        }
        drawCircle(color = rangeColor, radius = circleRadius.toPx(), center = leftCircleOffset)

        // Draw right circle
        scale(scaleAnim2, pivot = rightCircleOffset) {
          drawCircle(
              color = rangeColor.copy(alpha = 0.2f),
              radius = circleRadius.toPx(),
              center = rightCircleOffset)
        }
        drawCircle(
            color = rangeColor,
            radius = circleRadius.toPx(),
            center = rightCircleOffset,
        )

        // Define shadow properties
        val shadowRadius = 8f // Adjust as needed
        val shadowOffset = Offset(0f, 4f) // Adjust as needed
        val shadowColor = Color.Gray

        // Prepare Paint for tooltip with shadow
        val tooltipPaint =
            Paint().apply {
              color = surfaceColor
              this.asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(shadowRadius, shadowOffset.x, shadowOffset.y, shadowColor.toArgb())
              }
            }

        // Draw left Tooltip with shadow
        val leftL = leftCircleOffset.x - tooltipWidth.toPx() / 2f
        val topL =
            leftCircleOffset.y - tooltipSpacing.toPx() - circleRadiusInPx - tooltipHeight.toPx()

        val leftR = rightCircleOffset.x - tooltipWidth.toPx() / 2f
        val topR =
            rightCircleOffset.y - tooltipSpacing.toPx() - circleRadiusInPx - tooltipHeight.toPx()

        if (leftCircleDragging || rightCircleDragging) {
          leftTooltipOverlapping.value = (leftL + tooltipWidth.toPx()) >= leftR
        }

        rotate(tooltipAnim1, pivot = leftCircleOffset) {
          drawIntoCanvas { canvas ->
            path.reset()
            path.apply {
              addRoundRect(
                  RoundRect(
                      left = leftL,
                      top = topL,
                      right = leftL + tooltipWidth.toPx(),
                      bottom = topL + tooltipHeight.toPx(),
                      cornerRadius = CornerRadius(x = 15f, y = 15f)))
              moveTo(
                  x = leftCircleOffset.x - tooltipTriangleSize.toPx(),
                  y = leftCircleOffset.y - circleRadiusInPx - tooltipSpacing.toPx())
              relativeLineTo(tooltipTriangleSize.toPx(), tooltipTriangleSize.toPx())
              relativeLineTo(tooltipTriangleSize.toPx(), -tooltipTriangleSize.toPx())
              close()
            }
            canvas.drawPath(path, tooltipPaint)
          }
        }

        // Draw right Tooltip with shadow
        drawIntoCanvas { canvas ->
          path.reset()
          path.apply {
            addRoundRect(
                RoundRect(
                    left = leftR,
                    top = topR,
                    right = leftR + tooltipWidth.toPx(),
                    bottom = topR + tooltipHeight.toPx(),
                    cornerRadius = CornerRadius(x = 15f, y = 15f)))
            moveTo(
                x = rightCircleOffset.x - tooltipTriangleSize.toPx(),
                y = rightCircleOffset.y - circleRadiusInPx - tooltipSpacing.toPx())
            relativeLineTo(tooltipTriangleSize.toPx(), tooltipTriangleSize.toPx())
            relativeLineTo(tooltipTriangleSize.toPx(), -tooltipTriangleSize.toPx())
            close()
          }
          canvas.drawPath(path, tooltipPaint)
        }

        val valueLeft = minValue + (progress1 * range).roundToInt()
        val textLeft = valueLeft.toString()
        var textLayoutResult =
            textMeasurer.measure(
                text = AnnotatedString(textLeft), style = TextStyle(color = primaryColor))
        var textSize = textLayoutResult.size

        rotate(tooltipAnim1, pivot = leftCircleOffset) {
          drawText(
              textLayoutResult = textLayoutResult,
              topLeft =
                  Offset(
                      x = leftL + tooltipWidth.toPx() / 2 - textSize.width / 2,
                      y = topL + tooltipHeight.toPx() / 2 - textSize.height / 2))
        }

        val valueRight = minValue + (progress2 * range).roundToInt()
        val textRight = valueRight.toString()
        textLayoutResult =
            textMeasurer.measure(
                text = AnnotatedString(textRight),
                style = TextStyle(color = primaryColor),
            )
        textSize = textLayoutResult.size

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft =
                Offset(
                    x = leftR + tooltipWidth.toPx() / 2 - textSize.width / 2,
                    y = topR + tooltipHeight.toPx() / 2 - textSize.height / 2),
        )
      }
}