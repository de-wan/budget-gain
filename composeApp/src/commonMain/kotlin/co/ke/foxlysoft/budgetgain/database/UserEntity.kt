package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val username: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String
)
