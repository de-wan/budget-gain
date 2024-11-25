package co.ke.foxlysoft.budgetgain.database

import androidx.room.Database
import androidx.room.RoomDatabase
//import androidx.room.RoomDatabaseConstructor

@Database(entities = [UserEntity::class, SettingsEntity::class, BudgetEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao
}

//@Suppress("NO_ACTUAL_FOR_EXPECT")
//expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
//    override fun initialize(): AppDatabase
//}