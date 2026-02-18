package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.repository.AccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class AccountService(
    private val accountRepository: AccountRepository,
) {
    fun findAll(): List<Account> = accountRepository.findAll()

    fun findById(id: Long): Account? = accountRepository.findById(id).orElse(null)

    fun findByIdWithTransactions(id: Long): Account? = accountRepository.findByIdWithTransactions(id)

    fun create(
        name: String,
        initialBalance: BigDecimal?,
    ): Account {
        val account =
            Account(
                name = name,
                cashBalance = initialBalance ?: BigDecimal.ZERO,
            )
        return accountRepository.save(account)
    }

    fun updateCashBalance(
        id: Long,
        newBalance: BigDecimal,
    ): Account? {
        val account = findById(id) ?: return null
        val updatedAccount =
            account.copy(
                cashBalance = newBalance,
                updatedAt = LocalDateTime.now(),
            )
        return accountRepository.save(updatedAccount)
    }

    fun delete(id: Long): Boolean {
        if (!accountRepository.existsById(id)) return false
        accountRepository.deleteById(id)
        return true
    }
}
