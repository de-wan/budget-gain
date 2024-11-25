package co.ke.foxlysoft.budgetgain.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder() = Room.databaseBuilder<AppDatabase>(
    name = NSHomeDirectory() + "/budgetgain.db",
    factory =  { AppDatabase::class.instantiateImpl() }
)

fun getRoomDatabase(): AppDatabase {
    return getDatabaseBuilder()
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}