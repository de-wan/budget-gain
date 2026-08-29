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

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(connection: SQLiteConnection) {
        connection.exec("ALTER TABLE `CategoryEntity` ADD COLUMN `catalogKey` TEXT DEFAULT NULL")
        connection.exec("ALTER TABLE `CategoryEntity` ADD COLUMN `lightColorArgb` INTEGER NOT NULL DEFAULT 4282735204")
        connection.exec("ALTER TABLE `CategoryEntity` ADD COLUMN `darkColorArgb` INTEGER NOT NULL DEFAULT 4287669422")
        connection.exec("ALTER TABLE `CategoryEntity` ADD COLUMN `iconKey` TEXT NOT NULL DEFAULT 'category'")

        connection.exec(
            """
            CREATE TABLE IF NOT EXISTS `SubCategoryEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `catalogKey` TEXT,
                `name` TEXT NOT NULL,
                `iconKey` TEXT,
                `lightColorArgb` INTEGER,
                `darkColorArgb` INTEGER,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`categoryId`) REFERENCES `CategoryEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.exec("CREATE INDEX IF NOT EXISTS `index_SubCategoryEntity_categoryId` ON `SubCategoryEntity` (`categoryId`)")
        connection.exec("CREATE UNIQUE INDEX IF NOT EXISTS `index_SubCategoryEntity_categoryId_catalogKey` ON `SubCategoryEntity` (`categoryId`, `catalogKey`)")

        connection.exec(
            """
            CREATE TABLE IF NOT EXISTS `TransactionEntity_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ref` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `budgetId` INTEGER NOT NULL,
                `debitAccountId` INTEGER NOT NULL,
                `creditAccountId` INTEGER NOT NULL,
                `categoryId` INTEGER NOT NULL,
                `subCategoryId` INTEGER,
                `amount` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `timestamp` TEXT NOT NULL,
                FOREIGN KEY(`subCategoryId`) REFERENCES `SubCategoryEntity`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        connection.exec(
            """
            INSERT INTO `TransactionEntity_new` (
                `id`, `ref`, `type`, `description`, `budgetId`, `debitAccountId`,
                `creditAccountId`, `categoryId`, `subCategoryId`, `amount`, `createdAt`, `timestamp`
            ) SELECT `id`, `ref`, `type`, `description`, `budgetId`, `debitAccountId`,
                `creditAccountId`, `categoryId`, NULL, `amount`, `createdAt`, `timestamp`
              FROM `TransactionEntity`
            """.trimIndent()
        )
        connection.exec("DROP TABLE `TransactionEntity`")
        connection.exec("ALTER TABLE `TransactionEntity_new` RENAME TO `TransactionEntity`")
        connection.exec("CREATE INDEX IF NOT EXISTS `index_TransactionEntity_subCategoryId` ON `TransactionEntity` (`subCategoryId`)")
    }
}

val APP_DATABASE_MIGRATIONS = arrayOf(MIGRATION_14_18, MIGRATION_15_18, MIGRATION_16_18, MIGRATION_18_19)

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
