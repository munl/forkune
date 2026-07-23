package com.jian.forkune.di

import org.koin.core.module.Module

/**
 * ViewModels are registered per platform: Android uses the `viewModel { }` DSL, iOS uses
 * `factoryOf(::X)` (clean-architecture §7). Both actuals must stay in sync.
 */
expect fun viewModelModule(): Module
