package co.ke.foxlysoft.budgetgain.db
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME =  "budget_gain_app_database.db"
        private const val DATABASE_VERSION = 2

        // SQL statement to create the settings table
        private const val CREATE_TABLE_SETTINGS = """
            CREATE TABLE IF NOT EXISTS settings (
                setting TEXT PRIMARY KEY,
                value TEXT
            )
        """

        // SQL statement to create the merchants table
        private const val CREATE_TABLE_MERCHANTS = """
            CREATE TABLE IF NOT EXISTS merchants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """

        // SQL statement to create the parsed_transactions table
        private const val CREATE_TABLE_PARSED_TRANSACTIONS = """
            CREATE TABLE IF NOT EXISTS parsed_transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaction_code TEXT NOT NULL,
                transaction_type TEXT NOT NULL,
                recipient_id INTEGER,
                amount REAL NOT NULL,
                transaction_fee REAL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(recipient_id) REFERENCES merchants(id)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        db.execSQL(CREATE_TABLE_SETTINGS)
        db.execSQL(CREATE_TABLE_MERCHANTS)
        db.execSQL(CREATE_TABLE_PARSED_TRANSACTIONS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Upgrade database
    }
}
