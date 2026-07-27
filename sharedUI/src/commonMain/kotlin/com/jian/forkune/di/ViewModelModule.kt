package com.jian.forkune.di

import com.jian.forkune.ui.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Every ViewModel in the app, registered once for all targets (clean-architecture §7).
 * Koin's `viewModelOf` is multiplatform, so there is no platform actual to keep in sync —
 * add new ViewModels here and nowhere else.
 */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
}
