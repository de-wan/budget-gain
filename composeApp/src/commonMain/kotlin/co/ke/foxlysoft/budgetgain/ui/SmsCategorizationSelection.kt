package co.ke.foxlysoft.budgetgain.ui

import co.ke.foxlysoft.budgetgain.database.MpesaSmsEntity

internal fun categorizationSmsIds(
    selectedSms: Iterable<MpesaSmsEntity>,
    similarSms: Iterable<MpesaSmsEntity>,
): Set<Long> = buildSet {
    selectedSms.filterToUncategorizedIds(this)
    similarSms.filterToUncategorizedIds(this)
}

private fun Iterable<MpesaSmsEntity>.filterToUncategorizedIds(destination: MutableSet<Long>) {
    filterTo(mutableListOf()) { it.transactionId == 0L }
        .mapTo(destination) { it.id }
}
