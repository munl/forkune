package com.jian.forkune.di

import com.jian.forkune.data.places.InMemoryPlacesRepository
import com.jian.forkune.data.places.PlacesRepository
import com.jian.forkune.utilities.preferences.AppPreferences
import org.koin.dsl.module

/**
 * App-level singletons: repositories, helpers and the preferences wrapper
 * (clean-architecture §7). The platform-specific [com.jian.forkune.utilities.preferences.AppPreferencesInterface]
 * binding is provided by the per-platform module passed into initKoin().
 */
val appModule = module {
    single { AppPreferences(get()) }
    single<PlacesRepository> { InMemoryPlacesRepository() }
}
