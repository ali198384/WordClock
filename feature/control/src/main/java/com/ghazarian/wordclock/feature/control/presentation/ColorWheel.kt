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
import com.ghazarian.wordclock.core.model.LedColorMath
import com.ghazarian.wordclock.feature.control.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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

        val position = LedColorMath.hueAndSaturation(selectedColor)
        val angle = position.hueDegrees.toFloat() * (PI.toFloat() / 180f)
        val markerCenter = center + Offset(
            x = cos(angle) * wheelRadius * position.saturation.toFloat(),
            y = sin(angle) * wheelRadius * position.saturation.toFloat(),
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
    return LedColorMath.colorAt(
        x = offset.x.toDouble(),
        y = offset.y.toDouble(),
        centerX = centerX.toDouble(),
        centerY = centerY.toDouble(),
        radius = minOf(size.width, size.height).toDouble() / 2.0,
    )
}

internal fun colorOf(color: LedColor): Color = Color(
    red = color.red / 255f,
    green = color.green / 255f,
    blue = color.blue / 255f,
)
