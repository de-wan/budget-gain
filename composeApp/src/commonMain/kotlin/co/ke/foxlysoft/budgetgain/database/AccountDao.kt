package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao
interface AccountDao {
    @Upsert
    fun upsert(account: AccountEntity)

    @Delete
    fun delete(account: AccountEntity)
}