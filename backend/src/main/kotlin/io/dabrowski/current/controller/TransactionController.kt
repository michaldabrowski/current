package io.dabrowski.current.controller

import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.service.HoldingResponse
import io.dabrowski.current.service.TransactionService
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
    private val transactionService: TransactionService,
) {
    @GetMapping
    fun getAllTransactions(): List<Transaction> = transactionService.findAll()

    @GetMapping("/{id}")
    fun getTransaction(
        @PathVariable id: Long,
    ): ResponseEntity<Transaction> {
        val transaction = transactionService.findById(id)
        return if (transaction != null) {
            ResponseEntity.ok(transaction)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/account/{accountId}")
    fun getTransactionsByAccount(
        @PathVariable accountId: Long,
    ): List<Transaction> = transactionService.findByAccountId(accountId)

    @GetMapping("/symbol/{symbol}")
    fun getTransactionsBySymbol(
        @PathVariable symbol: String,
    ): List<Transaction> = transactionService.findBySymbol(symbol)

    @PostMapping
    fun createTransaction(
        @RequestBody request: CreateTransactionRequest,
    ): ResponseEntity<Transaction> {
        val transaction =
            transactionService.create(
                accountId = request.accountId,
                symbol = request.symbol,
                type = request.type,
                assetType = request.assetType,
                quantity = request.quantity,
                price = request.price,
                notes = request.notes,
            )
        return if (transaction != null) {
            ResponseEntity.ok(transaction)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/holdings/{accountId}")
    fun getHoldings(
        @PathVariable accountId: Long,
    ): List<HoldingResponse> = transactionService.getHoldings(accountId)
}

data class CreateTransactionRequest(
    val accountId: Long,
    val symbol: String,
    val type: TransactionType,
    val assetType: AssetType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val notes: String? = null,
)
