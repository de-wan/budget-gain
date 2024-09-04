import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import co.ke.foxlysoft.budgetgain.db.DatabaseHelper
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import android.util.Base64

class SettingsManager(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    // Insert or update a key-value pair
    fun put(key: String, value: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("setting", key)
            put("value", value)
        }
        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // Retrieve a value by key
    fun get(key: String): String? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            "settings",
            arrayOf("value"),
            "setting = ?",
            arrayOf(key),
            null,
            null,
            null
        )
        var value: String? = null
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
        }
        cursor.close()
        db.close()
        return value
    }

    // Delete a key-value pair
    fun delete(key: String) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        db.delete("settings", "setting = ?", arrayOf(key))
        db.close()
    }

    // Insert or update a key with a hashed value using Argon2
    fun putHashed(key: String, value: String) {
        val salt = generateSalt()
        val hashedValue = hashValue(value, salt)
        val storedValue = "$salt:$hashedValue"
        put(key, storedValue)
    }

    // Verify if the hashed value matches the stored hash using Argon2
    fun verifyHashed(key: String, value: String): Boolean {
        val storedValue = get(key) ?: return false
        val parts = storedValue.split(":")
        if (parts.size != 2) return false
        val salt = parts[0]
        val storedHash = parts[1]
        val hashedValue = hashValue(value, salt)
        return storedHash == hashedValue
    }

    // Generate a salt
    private fun generateSalt(): String {
        val salt = ByteArray(16) // 128-bit salt
        java.security.SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT).trim()
    }

    // Hash the value using Argon2
    private fun hashValue(value: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.DEFAULT)
        val argon2Params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withSalt(saltBytes)
            .withIterations(3)
            .withMemoryAsKB(65536) // 64 MB
            .withParallelism(1)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(argon2Params)

        val hashBytes = ByteArray(32) // 256-bit hash
        generator.generateBytes(value.toByteArray(), hashBytes, 0, hashBytes.size)

        return Base64.encodeToString(hashBytes, Base64.DEFAULT).trim()
    }
}
