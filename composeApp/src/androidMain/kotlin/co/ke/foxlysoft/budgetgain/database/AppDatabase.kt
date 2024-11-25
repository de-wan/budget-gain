package co.ke.foxlysoft.budgetgain.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.ke.foxlysoft.budgetgain.ContextProvider
import kotlinx.coroutines.Dispatchers

fun getDatabaseBuilder(context: android.content.Context) = with(context) {
    val dbFile = applicationContext.getDatabasePath("budgetgain.db")
    Room.databaseBuilder<AppDatabase>(
        context = applicationContext,
        name = dbFile.absolutePath,
    )
}

fun getRoomDatabase(ctx: Context): AppDatabase {
    return getDatabaseBuilder(ctx)
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}