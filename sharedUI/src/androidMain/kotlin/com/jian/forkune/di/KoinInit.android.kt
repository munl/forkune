package com.jian.forkune.di

import android.content.Context
import com.jian.forkune.utilities.preferences.AndroidPreferences
import com.jian.forkune.utilities.preferences.AppPreferencesInterface
import org.koin.dsl.module

/** Android entry point: binds the SharedPreferences-backed store, then starts Koin. */
fun initKoin(context: Context) = initKoin(
    platformModule = module {
        single<AppPreferencesInterface> { AndroidPreferences(context) }
    },
)
