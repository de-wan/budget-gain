package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao
interface TransactionDao {
    @Upsert
    fun upsert(transactionEntity: TransactionEntity)

    @Delete
    fun delete(transactionEntity: TransactionEntity)
}