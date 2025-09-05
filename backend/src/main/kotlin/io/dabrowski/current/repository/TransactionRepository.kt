package io.dabrowski.current.repository

import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    fun findByAccountId(accountId: Long): List<Transaction>

    fun findBySymbol(symbol: String): List<Transaction>

    fun findByAssetType(assetType: AssetType): List<Transaction>

    @Query("SELECT SUM(CASE WHEN t.type = 'BUY' THEN t.quantity ELSE -t.quantity END) " +
           "FROM Transaction t WHERE t.account.id = :accountId AND t.symbol = :symbol")
    fun getTotalQuantityByAccountAndSymbol(accountId: Long, symbol: String): BigDecimal?

    @Query("SELECT t FROM Transaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC")
    fun findByAccountIdOrderByDateDesc(accountId: Long): List<Transaction>
}
