package com.ghazarian.wordclock.core.designsystem.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

data class UiText(
    val resourceId: Int,
    val arguments: List<Any> = emptyList(),
)

@Composable
fun UiText.asString(): String =
    if (arguments.isEmpty()) {
        stringResource(resourceId)
    } else {
        stringResource(resourceId, *arguments.toTypedArray())
    }
