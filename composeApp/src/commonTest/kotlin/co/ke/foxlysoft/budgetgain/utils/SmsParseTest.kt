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
        assertEquals(t.date, "8/12/24")
        assertEquals(t.time, "1:53 PM")
        assertEquals(t.balance, 10000)
        assertEquals(t.cost, 23)
    }

    @Test
    fun testExtractReceiveSms(){
        val sms = "SL0348234Z Confirmed.You have received Ksh23,000.00 from John Doe 0728374654 on 11/12/24 at 6:51 PM New M-PESA balance is Ksh523,000.00."
        val t = extractReceiveSms(sms)

        assertEquals(t.smsType, MpesaSmsTypes.RECEIVE_MONEY)
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
        assertEquals("11/11/24", t.date)
        assertEquals("9:25 PM", t.time)
        assertEquals(100000000, t.balance)
        assertEquals(0, t.cost)
        assertEquals(MpesaSmsTypes.POCHI, t.smsType)

        // fail extraction of send sms
        val sendSms = "SL85OT9QWF Confirmed. Ksh1,500.00 sent to alice khan 0723456789 on 8/12/24 at 1:53 PM. New M-PESA balance is Ksh10,000.00. Transaction cost, Ksh23.00."
        val t1 = extractPochiSms(sendSms)

        assertEquals(MpesaSmsTypes.UNKNOWN, t1.smsType)
    }
}