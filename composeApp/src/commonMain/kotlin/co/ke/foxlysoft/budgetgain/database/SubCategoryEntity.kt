package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index(value = ["categoryId", "catalogKey"], unique = true),
    ],
)
data class SubCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val catalogKey: String? = null,
    var name: String,
    var iconKey: String? = null,
    var lightColorArgb: Long? = null,
    var darkColorArgb: Long? = null,
    val createdAt: Long = Clock.System.now().epochSeconds,
)

fun SubCategoryEntity.resolvedIconKey(parent: CategoryEntity): String = iconKey ?: parent.iconKey

fun SubCategoryEntity.resolvedColorArgb(parent: CategoryEntity, isDarkMode: Boolean): Long =
    if (isDarkMode) darkColorArgb ?: parent.darkColorArgb
    else lightColorArgb ?: parent.lightColorArgb
