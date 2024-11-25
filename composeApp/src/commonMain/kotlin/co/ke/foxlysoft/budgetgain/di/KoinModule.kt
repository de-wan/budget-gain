package co.ke.foxlysoft.budgetgain.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin

import co.ke.foxlysoft.budgetgain.ui.HomeScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.AllBudgetsScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.CreateBudgetScreenViewModel

expect fun platformModule(): Module

fun initKoin(config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)
        modules(
            platformModule(),
            provideSettingsRepository,
            provideBudgetRepository,
            viewModelModule,
        )
    }

val provideSettingsRepository =
    module {
        singleOf(::SettingsRepository)
    }

val provideBudgetRepository =
    module {
        singleOf(::BudgetRepository)
    }

val viewModelModule =
    module {
        viewModelOf(::HomeScreenViewModel)
        viewModelOf(::AllBudgetsScreenViewModel)
        viewModelOf(::CreateBudgetScreenViewModel)
    }