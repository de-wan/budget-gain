package co.ke.foxlysoft.budgetgain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false) val settingKey: String = "",
    val value: String = ""
)