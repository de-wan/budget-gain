package co.ke.foxlysoft.budgetgain

import android.app.Application
import co.ke.foxlysoft.budgetgain.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class BudgetGainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.setContext(this)
        initKoin {
            androidLogger()
            androidContext(this@BudgetGainApplication)
        }
    }
}