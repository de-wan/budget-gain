package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity
data class AccountEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String = "", // "debit" or "credit"
    val name: String = "",
    val holderType: AccountHolderType = AccountHolderType.MERCHANT,

    // budget account
    val budgetId: Long = 0L,

    // merchant account
    val merchantName: String = "",
    val merchantIdentifierType: String = "", // phone, pochiPhone, till, paybillAccount
    val merchantIdentifier: String = "",

    var balance: Long = 0, // in cents
    val createdAt: Long = Clock.System.now().epochSeconds,
)