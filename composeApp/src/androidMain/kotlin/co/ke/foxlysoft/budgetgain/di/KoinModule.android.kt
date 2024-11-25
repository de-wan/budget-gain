package co.ke.foxlysoft.budgetgain.di

import co.ke.foxlysoft.budgetgain.database.AppDatabase
import co.ke.foxlysoft.budgetgain.database.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module =
    module {
        single<AppDatabase> { getRoomDatabase(get()) }
    }