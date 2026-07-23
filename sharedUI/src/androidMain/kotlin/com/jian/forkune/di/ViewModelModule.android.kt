package com.jian.forkune.di

import com.jian.forkune.ui.home.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual fun viewModelModule(): Module = module {
    viewModel { HomeViewModel(get(), get()) }
}
