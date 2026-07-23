package com.jian.forkune.di

import com.jian.forkune.utilities.preferences.AppPreferencesInterface
import com.jian.forkune.utilities.preferences.IosPreferences
import org.koin.dsl.module

/** iOS entry point: binds the NSUserDefaults-backed store, then starts Koin. */
fun initKoin() = initKoin(
    platformModule = module {
        single<AppPreferencesInterface> { IosPreferences() }
    },
)
