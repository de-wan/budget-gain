package co.ke.foxlysoft.budgetgain.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

val MIGRATION_14_18 = object : Migration(14, 18) {
    override fun migrate(connection: SQLiteConnection) {
        connection.exec(
            """
            CREATE TABLE IF NOT EXISTS `BudgetEntity_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `yearMonth` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `initialBalance` INTEGER NOT NULL,
                `budgetedAmount` INTEGER NOT NULL,
                `spentAmount` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        connection.exec(
            """
            INSERT INTO `BudgetEntity_new` (
                `id`, `yearMonth`, `isActive`, `initialBalance`,
                `budgetedAmount`, `spentAmount`, `createdAt`
            )
            SELECT
                `id`, strftime('%Y-%m', `startDate` / 1000, 'unixepoch'),
                `isActive`, `initialBalance`, `budgetedAmount`, `spentAmount`, `createdAt`
            FROM `BudgetEntity`
            """.trimIndent()
        )
        connection.exec("DROP TABLE `BudgetEntity`")
        connection.exec("ALTER TABLE `BudgetEntity_new` RENAME TO `BudgetEntity`")
        connection.exec("ALTER TABLE `CategoryEntity` ADD COLUMN `trackMode` TEXT DEFAULT NULL")
        connection.addIsIgnoredColumn()
    }
}

val MIGRATION_15_18 = object : Migration(15, 18) {
    override fun migrate(connection: SQLiteConnection) {
        connection.addIsIgnoredColumn()
    }
}

val MIGRATION_16_18 = object : Migration(16, 18) {
    override fun migrate(connection: SQLiteConnection) {
        connection.addIsIgnoredColumn()
    }
}

val APP_DATABASE_MIGRATIONS = arrayOf(MIGRATION_14_18, MIGRATION_15_18, MIGRATION_16_18)

private fun SQLiteConnection.addIsIgnoredColumn() {
    exec("ALTER TABLE `MpesaSmsEntity` ADD COLUMN `isIgnored` INTEGER NOT NULL DEFAULT 0")
}

private fun SQLiteConnection.exec(sql: String) {
    val statement = prepare(sql)
    try {
        statement.step()
    } finally {
        statement.close()
    }
}
