package co.ke.foxlysoft.budgetgain.ui

import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity
import co.ke.foxlysoft.budgetgain.utils.MpesaSmsTypes
import kotlin.test.Test
import kotlin.test.assertEquals

class SmsCategorizationSelectionTest {
    @Test
    fun selectedAndSimilarSmsAreDeduplicatedAndAlreadyLinkedRowsAreExcluded() {
        val first = sms(id = 1)
        val duplicateFirst = sms(id = 1)
        val second = sms(id = 2)
        val alreadyLinked = sms(id = 3, transactionId = 99)

        val ids = categorizationSmsIds(
            selectedSms = listOf(first, second),
            similarSms = listOf(duplicateFirst, second, alreadyLinked),
        )

        assertEquals(setOf(1L, 2L), ids)
    }

    private fun sms(id: Long, transactionId: Long = 0) = MpesaSmsEntity(
        id = id,
        transactionId = transactionId,
        smsType = MpesaSmsTypes.TILL,
        ref = "ref-$id",
        amount = 100,
        dateTime = 0,
        subjectPrimaryIdentifierType = "name",
        subjectPrimaryIdentifier = "merchant",
        subjectSecondaryIdentifierType = "",
        subjectSecondaryIdentifier = "",
        cost = 0,
        balance = 0,
    )
}
