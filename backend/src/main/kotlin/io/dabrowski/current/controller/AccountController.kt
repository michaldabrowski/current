package io.dabrowski.current.controller

import io.dabrowski.current.entity.Account
import io.dabrowski.current.service.AccountService
import io.dabrowski.current.service.DeleteResult
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService,
) {
    @GetMapping
    fun getAllAccounts(): List<Account> = accountService.findAll()

    @GetMapping("/{id}")
    fun getAccount(
        @PathVariable id: Long,
    ): ResponseEntity<Account> {
        val account = accountService.findById(id)
        return if (account != null) {
            ResponseEntity.ok(account)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/with-transactions")
    fun getAccountWithTransactions(
        @PathVariable id: Long,
    ): ResponseEntity<Account> {
        val account = accountService.findByIdWithTransactions(id)
        return if (account != null) {
            ResponseEntity.ok(account)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createAccount(
        @Valid @RequestBody request: CreateAccountRequest,
    ): Account = accountService.create(request.name, request.initialBalance)

    @PutMapping("/{id}/cash")
    fun updateCashBalance(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCashRequest,
    ): ResponseEntity<Account> {
        val updatedAccount = accountService.updateCashBalance(id, request.newBalance)
        return if (updatedAccount != null) {
            ResponseEntity.ok(updatedAccount)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{id}/cash")
    fun adjustCash(
        @PathVariable id: Long,
        @Valid @RequestBody request: AdjustCashRequest,
    ): ResponseEntity<Account> {
        val updatedAccount = accountService.adjustCash(id, request.amount)
        return if (updatedAccount != null) {
            ResponseEntity.ok(updatedAccount)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAccount(
        @PathVariable id: Long,
    ): ResponseEntity<Void> =
        when (accountService.delete(id)) {
            DeleteResult.DELETED -> ResponseEntity.noContent().build()
            DeleteResult.NOT_FOUND -> ResponseEntity.notFound().build()
            DeleteResult.HAS_TRANSACTIONS ->
                ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
}

data class CreateAccountRequest(
    @NotBlank(message = "Account name cannot be blank")
    val name: String,
    @PositiveOrZero(message = "Initial balance must be zero or positive")
    val initialBalance: BigDecimal? = null,
)

data class UpdateCashRequest(
    @PositiveOrZero(message = "Cash balance must be zero or positive")
    val newBalance: BigDecimal,
)

data class AdjustCashRequest(
    val amount: BigDecimal,
)
