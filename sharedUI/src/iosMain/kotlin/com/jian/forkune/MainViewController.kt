package com.jian.forkune

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

/**
 * iOS host for the shared Compose UI. Koin is started from the SwiftUI app's init()
 * (see iosApp.swift) so that DI is up before any view is built.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App(onThemeChanged = { ThemeChanged(it) })
}

/**
 * Requires `UIViewControllerBasedStatusBarAppearance = false` in Info.plist — with the
 * default (true) UIKit ignores the app-level setter and this is a silent no-op.
 */
@Composable
private fun ThemeChanged(isDark: Boolean) {
    LaunchedEffect(isDark) {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (isDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
        )
    }
}
