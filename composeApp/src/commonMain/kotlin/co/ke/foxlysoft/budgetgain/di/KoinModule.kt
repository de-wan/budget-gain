package co.ke.foxlysoft.budgetgain.di

import co.ke.foxlysoft.budgetgain.shared.ToastManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import co.ke.foxlysoft.budgetgain.repos.SettingsRepository
import co.ke.foxlysoft.budgetgain.repos.AccountRepository
import co.ke.foxlysoft.budgetgain.repos.TransactionRepository
import co.ke.foxlysoft.budgetgain.repos.BudgetRepository
import co.ke.foxlysoft.budgetgain.repos.CategoryRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin

import co.ke.foxlysoft.budgetgain.ui.HomeScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.AllBudgetsScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.CreateBudgetScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.AddCategoryScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.CategoryDetailsScreenViewModel
import co.ke.foxlysoft.budgetgain.ui.SpendScreenViewModel
import org.koin.compose.viewmodel.dsl.viewModel


expect fun platformModule(): Module
expect val targetModule: Module

fun initKoin(config: KoinAppDeclaration? = null) =
    startKoin {
        config?.invoke(this)
        modules(
            platformModule(),
            targetModule,
            provideSettingsRepository,
            provideAccountRepository,
            provideTransactionRepository,
            provideBudgetRepository,
            provideCategoryRepository,
            viewModelModule,
        )
    }

val provideSettingsRepository =
    module {
        singleOf(::SettingsRepository)
    }

val provideAccountRepository =
    module {
        singleOf(::AccountRepository)
    }

val provideTransactionRepository =
    module {
        singleOf(::TransactionRepository)
    }

val provideBudgetRepository =
    module {
        singleOf(::BudgetRepository)
    }

val provideCategoryRepository =
    module {
        singleOf(::CategoryRepository)
    }

val viewModelModule =
    module {
        viewModelOf(::HomeScreenViewModel)
        viewModelOf(::AllBudgetsScreenViewModel)
        viewModelOf(::CreateBudgetScreenViewModel)
        viewModelOf(::AddCategoryScreenViewModel)
        viewModel{
            (categoryId: Long) -> SpendScreenViewModel(
            categoryId = categoryId,
            categoryRepository = get(),
            accountRepository = get(),
            budgetRepository = get(),
            transactionRepository = get()
        )
        }
        viewModel{
            (categoryId: Long) -> CategoryDetailsScreenViewModel(
                categoryId = categoryId,
                categoryRepository = get(),
                transactionRepository = get(),
                accountRepository = get(),
            )
        }
    }