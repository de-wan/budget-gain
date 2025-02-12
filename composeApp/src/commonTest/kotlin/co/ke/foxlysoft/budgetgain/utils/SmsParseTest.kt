package co.ke.foxlysoft.budgetgain.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SmsParseTest {
    @Test
    fun testExtractSendSms() {
        val sms = "SL85OT9QWF Confirmed. Ksh1,500.00 sent to alice khan 0723456789 on 8/12/24 at 1:53 PM. New M-PESA balance is Ksh10,000.00. Transaction cost, Ksh23.00."
        val t = extractSendSms(sms)

        assertEquals(t.smsType, MpesaSmsTypes.SEND_MONEY)
        assertEquals(t.ref, "SL85OT9QWF")
        assertEquals(t.amount, 150000)
        assertEquals(t.subjectSecondaryIdentifierType, "name")
        assertEquals(t.subjectSecondaryIdentifier, "alice khan")
        assertEquals(t.subjectPrimaryIdentifierType, "phone")
        assertEquals(t.subjectPrimaryIdentifier, "0723456789")
//        assertEquals(t.date, "8/12/24")
//        assertEquals(t.time, "1:53 PM")

        assertEquals(t.balance, 1000000)
        assertEquals(t.cost, 2300)
    }

    @Test
    fun testExtractReceiveSms(){
        val sms = "SL0348234Z Confirmed.You have received Ksh23,000.00 from John Doe 0728374654 on 11/12/24 at 6:51 PM New M-PESA balance is Ksh523,000.00."
        val t = extractReceiveSms(sms)

        assertEquals(t.smsType, MpesaSmsTypes.RECEIVE_MONEY)
        assertEquals(t.subjectPrimaryIdentifierType, "phone")
        assertEquals(t.subjectPrimaryIdentifier, "0728374654")
        assertEquals(t.subjectSecondaryIdentifierType, "name")
        assertEquals(t.subjectSecondaryIdentifier, "John Doe")
        assertEquals(t.ref, "SL0348234Z")
        assertEquals(t.amount, 2300000)
        assertEquals(t.balance, 52300000)
        assertEquals(t.cost, 0)
    }

    @Test
    fun testExtractPochiSms() {
        val sms =
            "SYEISFNEFS Confirmed. Ksh25.00 sent to beatrice ashibo on 11/11/24 at 9:25 PM. New M-PESA balance is Ksh1,000,000.00. Transaction cost, Ksh0.00."
        val t = extractPochiSms(sms)

        assertEquals("SYEISFNEFS", t.ref)
        assertEquals(2500, t.amount)
        assertEquals("name", t.subjectPrimaryIdentifierType)
        assertEquals("beatrice ashibo", t.subjectPrimaryIdentifier)
//        assertEquals("11/11/24", t.date)
//        assertEquals("9:25 PM", t.time)
        assertEquals(100000000, t.balance)
        assertEquals(0, t.cost)
        assertEquals(MpesaSmsTypes.POCHI, t.smsType)

        // fail extraction of send sms
        val sendSms = "SL85OT9QWF Confirmed. Ksh1,500.00 sent to alice khan 0723456789 on 8/12/24 at 1:53 PM. New M-PESA balance is Ksh10,000.00. Transaction cost, Ksh23.00."
        val t1 = extractPochiSms(sendSms)

        assertEquals(MpesaSmsTypes.UNKNOWN, t1.smsType)
    }

    @Test
    fun testExtractTillSms() {
        val tillSms = "SYEISFNEFS Confirmed. Ksh1,500.00 paid to FOXLYSOFT TECHNOLOGIES - KENYA. on 11/11/24 at 9:25 PM.New M-PESA balance is Ksh1,000,000.00. Transaction cost, Ksh0.00."
        val t = extractTillSms(tillSms)

        assertEquals("SYEISFNEFS", t.ref)
        assertEquals(150000, t.amount)
        assertEquals("FOXLYSOFT TECHNOLOGIES - KENYA", t.subjectPrimaryIdentifier)
        assertEquals("name", t.subjectPrimaryIdentifierType)
//        assertEquals("11/11/24", t.date)
//        assertEquals("9:25 PM", t.time)
        assertEquals(100000000, t.balance)
        assertEquals(0, t.cost)
        assertEquals(MpesaSmsTypes.TILL, t.smsType)
    }

    @Test
    fun testExtractPaybillSms() {
        val paybillSms = "SYEISFNEFS Confirmed. Ksh800.00 sent to PayPal Top Up Account for account 0729511560 on 11/11/24 at 9:25 PM New M-PESA balance is Ksh1,000,000.00. Transaction cost, Ksh0.00."
        val t = extractPaybillSms(paybillSms)

        assertEquals("SYEISFNEFS", t.ref)
        assertEquals(80000, t.amount)
        assertEquals("paybill", t.subjectPrimaryIdentifierType)
        assertEquals("PayPal Top Up Account", t.subjectPrimaryIdentifier)
        assertEquals("account", t.subjectSecondaryIdentifierType)
        assertEquals("0729511560", t.subjectSecondaryIdentifier)
//        assertEquals("11/11/24", t.date)
//        assertEquals("9:25 PM", t.time)
        assertEquals(100000000, t.balance)
        assertEquals(0, t.cost)
        assertEquals(MpesaSmsTypes.PAYBILL, t.smsType)
    }

    @Test
    fun testExtractWithdrawSms() {
        val tillSms = "SL85OT9QWF Confirmed.on 8/12/24 at 1:53 PMWithdraw Ksh1,500.00 from 3234323 - FOXLYSOFT TECHNOLOGIES KENYA New M-PESA balance is Ksh10,000.00. Transaction cost, Ksh23.00."
        val t = extractWithdrawSms(tillSms)

        assertEquals("SL85OT9QWF", t.ref)
//        assertEquals("8/12/24", t.date)
//        assertEquals("1:53 PM", t.time)
        assertEquals(150000, t.amount)
        assertEquals("till", t.subjectPrimaryIdentifierType)
        assertEquals("3234323", t.subjectPrimaryIdentifier)
        assertEquals("name", t.subjectSecondaryIdentifierType)
        assertEquals("FOXLYSOFT TECHNOLOGIES KENYA", t.subjectSecondaryIdentifier)
        assertEquals(1000000, t.balance)
        assertEquals(2300, t.cost)

        assertEquals(MpesaSmsTypes.WITHDRAW_MONEY, t.smsType)
    }
}