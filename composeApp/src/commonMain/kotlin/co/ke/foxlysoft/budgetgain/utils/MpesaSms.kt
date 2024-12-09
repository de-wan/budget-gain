package co.ke.foxlysoft.budgetgain.utils


class MpesaSms(
    var smsType: MpesaSmsTypes, // specify sms type after constructing the object
    var ref: String,
    var amount: Long, // cents
    var date: String,
    var time: String,
    var subjectPrimaryIdentifierType: String,
    var subjectPrimaryIdentifier: String,
    var subjectSecondaryIdentifierType: String,
    var subjectSecondaryIdentifier: String,
    var cost: Long, // cents
    var balance: Long, // cents
) {
}