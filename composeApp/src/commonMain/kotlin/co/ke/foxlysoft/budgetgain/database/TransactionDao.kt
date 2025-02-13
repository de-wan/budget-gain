package co.ke.foxlysoft.budgetgain.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Upsert
    suspend fun upsert(transactionEntity: TransactionEntity): Long

    @Delete
    suspend fun delete(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM TransactionEntity WHERE categoryId = :categoryId")
    fun getCategoryTransactions(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity WHERE categoryId = :categoryId ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingCategoryTransactions(categoryId: Long, limit: Int, offset: Int): List<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE budgetId = :budgetId AND creditAccountId = :accountId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagingMerchantTransactions(budgetId: Long, accountId: Long, limit: Int, offset: Int): List<TransactionEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM TransactionEntity WHERE ref = :ref)")
    fun existsByRef(ref: String): Boolean
}