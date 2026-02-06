package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val yearMonth: String = "",
    var isActive: Boolean = false,
    val initialBalance: Long = 0,   // amount in cents
    var budgetedAmount: Long = 0,   // amount in cents
    var spentAmount: Long = 0,   // amount in cents
    val createdAt: Long = Clock.System.now().epochSeconds,
)
