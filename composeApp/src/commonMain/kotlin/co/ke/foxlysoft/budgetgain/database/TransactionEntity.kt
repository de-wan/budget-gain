package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SubCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subCategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("subCategoryId")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ref: String = "",   // external reference
    val type: AccountType = AccountType.CREDIT, // "debit" or "credit"
    val description: String = "",
    val budgetId: Long = 0,
    val debitAccountId: Long = 0,
    val creditAccountId: Long = 0,
    val categoryId: Long = 0,
    val subCategoryId: Long? = null,
    val amount: Long = 0, // in cents
    val createdAt: Long = Clock.System.now().epochSeconds,
    val timestamp: String = "", // yyyy-MM-dd HH:mm
)
