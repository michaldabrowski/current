package io.dabrowski.current.controller

import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.TransactionRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {

    @GetMapping
    fun getAllTransactions(): List<Transaction> {
        return transactionRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getTransaction(@PathVariable id: Long): ResponseEntity<Transaction> {
        val transaction = transactionRepository.findById(id).orElse(null)
        return if (transaction != null) {
            ResponseEntity.ok(transaction)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/account/{accountId}")
    fun getTransactionsByAccount(@PathVariable accountId: Long): List<Transaction> {
        return transactionRepository.findByAccountIdOrderByDateDesc(accountId)
    }

    @GetMapping("/symbol/{symbol}")
    fun getTransactionsBySymbol(@PathVariable symbol: String): List<Transaction> {
        return transactionRepository.findBySymbol(symbol)
    }

    @PostMapping
    fun createTransaction(@RequestBody request: CreateTransactionRequest): ResponseEntity<Transaction> {
        val account = accountRepository.findById(request.accountId).orElse(null)
            ?: return ResponseEntity.badRequest().build()

        val transaction = Transaction(
            account = account,
            symbol = request.symbol,
            type = request.type,
            assetType = request.assetType,
            quantity = request.quantity,
            price = request.price,
            totalAmount = request.quantity * request.price,
            notes = request.notes
        )

        val savedTransaction = transactionRepository.save(transaction)
        return ResponseEntity.ok(savedTransaction)
    }

    @GetMapping("/holdings/{accountId}")
    fun getHoldings(@PathVariable accountId: Long): List<HoldingResponse> {
        val transactions = transactionRepository.findByAccountId(accountId)

        return transactions
            .groupBy { it.symbol }
            .mapNotNull { (symbol, txns) ->
                // Calculate current quantity (buys - sells)
                val totalQuantity = txns.sumOf {
                    if (it.type == TransactionType.BUY) it.quantity else -it.quantity
                }

                // Only process if we still hold this asset
                if (totalQuantity <= BigDecimal.ZERO) return@mapNotNull null

                // Calculate weighted average price of all purchases
                val buyTransactions = txns.filter { it.type == TransactionType.BUY }
                val totalPurchaseAmount = buyTransactions.sumOf { it.totalAmount }
                val totalPurchaseQuantity = buyTransactions.sumOf { it.quantity }

                val averagePrice = if (totalPurchaseQuantity > BigDecimal.ZERO) {
                    totalPurchaseAmount / totalPurchaseQuantity
                } else BigDecimal.ZERO

                HoldingResponse(
                    symbol = symbol,
                    quantity = totalQuantity,
                    averagePrice = averagePrice,
                    assetType = txns.first().assetType
                )
            }
    }
}

data class CreateTransactionRequest(
    val accountId: Long,
    val symbol: String,
    val type: TransactionType,
    val assetType: AssetType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val notes: String? = null
)

data class HoldingResponse(
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val assetType: AssetType
)
