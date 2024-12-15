package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock

@Entity
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val budgetId: Long,
    val name: String,
    val amount: Long,   // amount in cents
    var spentAmount: Long,   // amount in cents
    val createdAt: Long = Clock.System.now().epochSeconds,
)
