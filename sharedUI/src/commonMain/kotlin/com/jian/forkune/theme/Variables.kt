package com.jian.forkune.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * App-wide design tokens. Views read spacing/sizing from [Variables.Dimensions] and the
 * brand accent from [Variables.Colors] rather than hard-coding literals (clean-architecture §4).
 * Semantic colours still come from `MaterialTheme.colorScheme`.
 */
object Variables {

    object Dimensions {
        val screenPadding = 24.dp

        val spacingXs = 4.dp
        val spacingSm = 8.dp
        val spacingMd = 16.dp
        val spacingLg = 24.dp
        val spacingXl = 32.dp

        val cornerRadius = 20.dp
        val buttonCornerRadius = 16.dp

        val surpriseCardHeight = 200.dp
        val secondaryButtonHeight = 72.dp
        val locationDotSize = 8.dp
        val profileButtonSize = 44.dp
    }

    object Colors {
        val accent: Color = AccentCoral
        val onAccent: Color = OnAccentCoral
        val success: Color = SuccessGreen
    }
}
