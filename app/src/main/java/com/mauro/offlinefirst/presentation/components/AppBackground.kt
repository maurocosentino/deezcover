package com.mauro.offlinefirst.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val GradientTop = Color(0xFF000000)
private val GradientMiddle = Color(0xFF000409)
private val GradientBottom = Color(0xFF000715)

val AppBackgroundBrush: Brush
    get() = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to GradientTop,
            0.4f to GradientMiddle,
            1.0f to GradientBottom
        )
    )

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    background: Brush = AppBackgroundBrush,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        content()
    }
}
