package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val isActive: Boolean = false,
    val initialBalance: Float = 0f,
    val budgetedAmount: Float = 0f,
    val spentAmount: Float = 0f,
    val startDate: String = "",
    val endDate: String = ""
)
