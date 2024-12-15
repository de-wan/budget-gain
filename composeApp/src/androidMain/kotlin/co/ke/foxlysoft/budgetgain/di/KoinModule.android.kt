package co.ke.foxlysoft.budgetgain.di

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

import co.ke.foxlysoft.budgetgain.shared.ToastManager

actual fun platformModule(): Module =
    module {
        single<AppDatabase> { getRoomDatabase(get()) }
    }

actual val targetModule: Module = module {
    singleOf(::ToastManager)
}