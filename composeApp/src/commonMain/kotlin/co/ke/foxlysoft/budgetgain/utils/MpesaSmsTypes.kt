package co.ke.foxlysoft.budgetgain.utils

enum class MpesaSmsTypes {
    SEND_MONEY,     // has phone number before on
    POCHI,          // has no phone number before on
    RECEIVE_MONEY,  // has received
    WITHDRAW_MONEY, // has Withdraw
    DEPOSIT_MONEY, // TODO: Need a sample sms to test this
    PAYBILL,        // has for account
    TILL,           // has paid to
    UNKNOWN
}