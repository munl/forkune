package com.jian.forkune.androidApp

import android.app.Application
import com.jian.forkune.di.initKoin

/**
 * Koin is started here, not in [AppActivity]: the Activity is recreated on configuration
 * changes the manifest doesn't absorb (system dark-mode toggle, font scale) while the
 * process survives, and a second startKoin() throws KoinAppAlreadyStartedException.
 */
class ForkuneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(applicationContext)
    }
}
