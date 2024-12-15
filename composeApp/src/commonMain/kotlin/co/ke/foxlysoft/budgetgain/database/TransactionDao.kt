package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Upsert
    fun upsert(transactionEntity: TransactionEntity)

    @Delete
    fun delete(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM TransactionEntity WHERE categoryId = :categoryId")
    fun getCategoryTransactions(categoryId: Long): Flow<List<TransactionEntity>>
}