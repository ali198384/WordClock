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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.core.designsystem.resources.Res
import com.ghazarian.wordclock.core.designsystem.resources.color_wheel_content_description
import com.ghazarian.wordclock.core.model.LedColor
import com.ghazarian.wordclock.core.model.ledColorAtPosition
import com.ghazarian.wordclock.core.model.toArgb
import com.ghazarian.wordclock.core.model.toHueAndSaturation
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val WheelColors =
    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)

@Composable
internal fun ColorWheel(
    selectedColor: LedColor,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onColorSelected: (LedColor) -> Unit,
) {
    val latestCallback by rememberUpdatedState(onColorSelected)
    val description = stringResource(Res.string.color_wheel_content_description)
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = description }
            .pointerInput(enabled) {
                if (enabled) detectTapGestures {
                    latestCallback(ledColorAtPosition(it.x, it.y, size.width, size.height))
                }
            }
            .pointerInput(enabled) {
                if (enabled) detectDragGestures(
                    onDragStart = {
                        latestCallback(ledColorAtPosition(it.x, it.y, size.width, size.height))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        latestCallback(
                            ledColorAtPosition(
                                change.position.x,
                                change.position.y,
                                size.width,
                                size.height,
                            ),
                        )
                    },
                )
            },
    ) {
        val radius = size.minDimension / 2f - 5.dp.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(Brush.sweepGradient(WheelColors, center), radius, center)
        drawCircle(
            Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius),
            radius,
            center,
        )
        drawCircle(Color.White.copy(alpha = 0.9f), radius, center, style = Stroke(1.dp.toPx()))
        val (hue, saturation) = selectedColor.toHueAndSaturation()
        val angle = hue * PI.toFloat() / 180f
        val marker =
            center + Offset(cos(angle) * radius * saturation, sin(angle) * radius * saturation)
        drawCircle(Color.White, 14.dp.toPx(), marker)
        drawCircle(Color(selectedColor.toArgb()), 10.dp.toPx(), marker, style = Stroke(4.dp.toPx()))
    }
}
