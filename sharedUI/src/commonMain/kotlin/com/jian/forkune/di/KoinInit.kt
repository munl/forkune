package com.jian.forkune.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Shared Koin startup. Each platform calls its own initKoin() (which supplies the
 * platform module binding the key-value store) — that delegates here.
 */
fun initKoin(platformModule: Module, appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(platformModule, appModule, viewModelModule)
    }
}
