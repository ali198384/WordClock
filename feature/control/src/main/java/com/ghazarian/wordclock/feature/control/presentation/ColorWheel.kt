package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.feature.control.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private val ColorWheelHues = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red,
)

@Composable
internal fun ColorWheel(
    selectedColor: LedColor,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onColorSelected: (LedColor) -> Unit,
) {
    val latestOnColorSelected by rememberUpdatedState(onColorSelected)
    val wheelDescription = stringResource(R.string.color_wheel_content_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = wheelDescription }
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { offset ->
                        latestOnColorSelected(colorAt(offset, size))
                    }
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            latestOnColorSelected(colorAt(offset, size))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            latestOnColorSelected(colorAt(change.position, size))
                        },
                    )
                }
            },
    ) {
        val wheelRadius = size.minDimension / 2f - 5.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.sweepGradient(colors = ColorWheelHues, center = center),
            radius = wheelRadius,
            center = center,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = center,
                radius = wheelRadius,
            ),
            radius = wheelRadius,
            center = center,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = wheelRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )

        val (hue, saturation) = hueAndSaturation(selectedColor)
        val angle = hue * (PI.toFloat() / 180f)
        val markerCenter = center + Offset(
            x = cos(angle) * wheelRadius * saturation,
            y = sin(angle) * wheelRadius * saturation,
        )
        drawCircle(
            color = Color.White,
            radius = 14.dp.toPx(),
            center = markerCenter,
        )
        drawCircle(
            color = colorOf(selectedColor),
            radius = 10.dp.toPx(),
            center = markerCenter,
            style = Stroke(width = 4.dp.toPx()),
        )
    }
}

private fun colorAt(offset: Offset, size: IntSize): LedColor {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val deltaX = offset.x - centerX
    val deltaY = offset.y - centerY
    val distance = sqrt(deltaX * deltaX + deltaY * deltaY)
    val radius = min(size.width, size.height) / 2f
    val hue = (atan2(deltaY, deltaX) * (180f / PI.toFloat()) + 360f) % 360f
    val saturation = if (radius == 0f) 0f else (distance / radius).coerceIn(0f, 1f)
    return hsvToColor(hue, saturation)
}

private fun hsvToColor(hue: Float, saturation: Float): LedColor {
    val huePart = hue / 60f
    val secondary = saturation * (1f - kotlin.math.abs(huePart % 2f - 1f))
    val (red, green, blue) = when (huePart.toInt()) {
        0 -> Triple(saturation, secondary, 0f)
        1 -> Triple(secondary, saturation, 0f)
        2 -> Triple(0f, saturation, secondary)
        3 -> Triple(0f, secondary, saturation)
        4 -> Triple(secondary, 0f, saturation)
        else -> Triple(saturation, 0f, secondary)
    }
    val white = 1f - saturation
    return LedColor(
        red = ((red + white) * 255f).roundToInt().coerceIn(0, 255),
        green = ((green + white) * 255f).roundToInt().coerceIn(0, 255),
        blue = ((blue + white) * 255f).roundToInt().coerceIn(0, 255),
    )
}

private fun hueAndSaturation(color: LedColor): Pair<Float, Float> {
    val red = color.red / 255f
    val green = color.green / 255f
    val blue = color.blue / 255f
    val maximum = max(red, max(green, blue))
    val minimum = min(red, min(green, blue))
    val delta = maximum - minimum
    val hue = when {
        delta == 0f -> 0f
        maximum == red -> 60f * (((green - blue) / delta) % 6f)
        maximum == green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }
    val saturation = if (maximum == 0f) 0f else delta / maximum
    return hue to saturation
}

internal fun colorOf(color: LedColor): Color = Color(
    red = color.red / 255f,
    green = color.green / 255f,
    blue = color.blue / 255f,
)
