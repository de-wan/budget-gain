package co.ke.foxlysoft.budgetgain.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

//import androidx.room.RoomDatabaseConstructor

@Database(entities = [UserEntity::class, SettingsEntity::class, BudgetEntity::class, CategoryEntity::class,
    AccountEntity::class, TransactionEntity::class, MpesaSmsEntity::class], version = 13)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun mpesaSmsDao(): MpesaSmsDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}