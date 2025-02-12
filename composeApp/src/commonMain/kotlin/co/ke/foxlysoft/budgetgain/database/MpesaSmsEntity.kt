package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes

@Entity
data class MpesaSmsEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var transactionId: Long = 0,
    var smsType: MpesaSmsTypes, // specify sms type after constructing the object
    var ref: String,
    var amount: Long, // cents
    var dateTime: Long,
    var subjectPrimaryIdentifierType: String,
    var subjectPrimaryIdentifier: String,
    var subjectSecondaryIdentifierType: String,
    var subjectSecondaryIdentifier: String,
    var cost: Long, // cents
    var balance: Long, // cents
)