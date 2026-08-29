package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val budgetId: Long,
    var name: String,
    var amount: Long,   // amount in cents
    var spentAmount: Long,   // amount in cents
    var trackMode: String? = null,
    val catalogKey: String? = null,
    var lightColorArgb: Long = DEFAULT_CATEGORY_LIGHT_COLOR_ARGB,
    var darkColorArgb: Long = DEFAULT_CATEGORY_DARK_COLOR_ARGB,
    var iconKey: String = DEFAULT_CATEGORY_ICON_KEY,
    val createdAt: Long = Clock.System.now().epochSeconds,
)

// Material Blue Grey 700/300 provide safe defaults for existing and custom categories.
const val DEFAULT_CATEGORY_LIGHT_COLOR_ARGB: Long = 0xFF455A64
const val DEFAULT_CATEGORY_DARK_COLOR_ARGB: Long = 0xFF90A4AE
const val DEFAULT_CATEGORY_ICON_KEY: String = "category"
