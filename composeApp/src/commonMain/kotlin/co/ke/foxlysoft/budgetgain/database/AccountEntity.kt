package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AccountEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String = "", // "debit" or "credit"
    val name: String = "",
    val merchantName: String = "",
    val merchantIdentifierType: String = "", // phone, pochiPhone, till, paybillAccount
    val merchantIdentifier: String = "",
    val balance: Long = 0, // in cents
    val createdAt: Long = 0, // in millis
)