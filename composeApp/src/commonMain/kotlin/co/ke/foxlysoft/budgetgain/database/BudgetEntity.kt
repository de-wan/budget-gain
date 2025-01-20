package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String = "",
    var isActive: Boolean = false,
    val initialBalance: Long = 0,   // amount in cents
    var budgetedAmount: Long = 0,   // amount in cents
    var spentAmount: Long = 0,   // amount in cents
    val startDate: Long = 0,
    val endDate: Long = 0,
    val createdAt: Long = Clock.System.now().epochSeconds,
)
