package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ref: String = "",   // external reference
    val type: AccountType = AccountType.CREDIT, // "debit" or "credit"
    val description: String = "",
    val budgetId: Long = 0,
    val debitAccountId: Long = 0,
    val creditAccountId: Long = 0,
    val categoryId: Long = 0,
    val amount: Long = 0, // in cents
    val createdAt: Long = Clock.System.now().epochSeconds,
    val timestamp: String = "", // yyyy-MM-dd HH:mm
)
