package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ref: String = "",   // external reference
    val type: String = "", // "debit" or "credit"
    val debitAccountId: Int = 0,
    val creditAccountId: Int = 0,
    val categoryId: Long = 0,
    val amount: Long = 0, // in cents
    val createdAt: Long = 0, // in millis
    val timestamp: Long = 0, // in millis (When the transaction was completed)
)
