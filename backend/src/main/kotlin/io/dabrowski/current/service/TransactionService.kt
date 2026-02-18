package io.dabrowski.current.service

import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransactionService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    fun findAll(): List<Transaction> = transactionRepository.findAll()

    fun findById(id: Long): Transaction? = transactionRepository.findById(id).orElse(null)

    fun findByAccountId(accountId: Long): List<Transaction> =
        transactionRepository.findByAccountIdOrderByDateDesc(accountId)

    fun findBySymbol(symbol: String): List<Transaction> = transactionRepository.findBySymbol(symbol)

    fun create(
        accountId: Long,
        symbol: String,
        type: TransactionType,
        assetType: AssetType,
        quantity: BigDecimal,
        price: BigDecimal,
        notes: String?,
    ): Transaction? {
        val account = accountRepository.findById(accountId).orElse(null) ?: return null

        val transaction =
            Transaction(
                account = account,
                symbol = symbol,
                type = type,
                assetType = assetType,
                quantity = quantity,
                price = price,
                totalAmount = quantity * price,
                notes = notes,
            )

        return transactionRepository.save(transaction)
    }

    fun getHoldings(accountId: Long): List<HoldingResponse> {
        val transactions = transactionRepository.findByAccountId(accountId)

        return transactions
            .groupBy { it.symbol }
            .mapNotNull { (symbol, txns) ->
                val totalQuantity =
                    txns.sumOf {
                        if (it.type == TransactionType.BUY) it.quantity else -it.quantity
                    }

                if (totalQuantity <= BigDecimal.ZERO) return@mapNotNull null

                val buyTransactions = txns.filter { it.type == TransactionType.BUY }
                val totalPurchaseAmount = buyTransactions.sumOf { it.totalAmount }
                val totalPurchaseQuantity = buyTransactions.sumOf { it.quantity }

                val averagePrice =
                    if (totalPurchaseQuantity > BigDecimal.ZERO) {
                        totalPurchaseAmount / totalPurchaseQuantity
                    } else {
                        BigDecimal.ZERO
                    }

                HoldingResponse(
                    symbol = symbol,
                    quantity = totalQuantity,
                    averagePrice = averagePrice,
                    assetType = txns.first().assetType,
                )
            }
    }
}

data class HoldingResponse(
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val assetType: AssetType,
)
