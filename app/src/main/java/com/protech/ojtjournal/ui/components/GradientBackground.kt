package com.protech.ojtjournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.protech.ojtjournal.ui.theme.gradientEnd
import com.protech.ojtjournal.ui.theme.gradientStart

@Composable
fun GradientBackground(
    startColor: Color = gradientStart,
    endColor: Color = gradientEnd,
    content: @Composable BoxScope.() -> Unit
) {
    // Remove the system bar insets and padding to eliminate white space
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        startColor.copy(alpha = 0.3f),
                        endColor.copy(alpha = 0.1f)
                    )
                )
            ),
        content = content
    )
} 