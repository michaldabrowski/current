package io.dabrowski.current.controller

import io.dabrowski.current.entity.Account
import io.dabrowski.current.repository.AccountRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
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
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountRepository: AccountRepository
) {

    @GetMapping
    fun getAllAccounts(): List<Account> {
        return accountRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getAccount(@PathVariable id: Long): ResponseEntity<Account> {
        val account = accountRepository.findById(id).orElse(null)
        return if (account != null) {
            ResponseEntity.ok(account)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{id}/with-transactions")
    fun getAccountWithTransactions(@PathVariable id: Long): ResponseEntity<Account> {
        val account = accountRepository.findByIdWithTransactions(id)
        return if (account != null) {
            ResponseEntity.ok(account)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createAccount(@Valid @RequestBody request: CreateAccountRequest): Account {
        val account = Account(
            name = request.name,
            cashBalance = request.initialBalance ?: BigDecimal.ZERO
        )
        return accountRepository.save(account)
    }

    @PutMapping("/{id}/cash")
    fun updateCashBalance(
        @PathVariable id: Long,
        @RequestBody request: UpdateCashRequest
    ): ResponseEntity<Account> {
        val account = accountRepository.findById(id).orElse(null)
        return if (account != null) {
            val updatedAccount = account.copy(
                cashBalance = request.newBalance,
                updatedAt = LocalDateTime.now()
            )
            ResponseEntity.ok(accountRepository.save(updatedAccount))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAccount(@PathVariable id: Long): ResponseEntity<Void> {
        return if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

data class CreateAccountRequest(
    @NotBlank(message = "Account name cannot be blank")
    val name: String,
    @PositiveOrZero(message = "Initial balance must be zero or positive")
    val initialBalance: BigDecimal? = null
)

data class UpdateCashRequest(
    val newBalance: BigDecimal
)
