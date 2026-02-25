package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.BalanceSnapshotRepository
import io.dabrowski.current.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

enum class DeleteResult {
    DELETED,
    NOT_FOUND,
    HAS_TRANSACTIONS,
}

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val balanceSnapshotRepository: BalanceSnapshotRepository,
    private val transactionRepository: TransactionRepository,
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

    fun adjustCash(
        id: Long,
        amount: BigDecimal,
    ): Account? {
        val account = findById(id) ?: return null
        val newBalance = account.cashBalance + amount
        if (newBalance < BigDecimal.ZERO) return null
        val updatedAccount =
            account.copy(
                cashBalance = newBalance,
                updatedAt = LocalDateTime.now(),
            )
        return accountRepository.save(updatedAccount)
    }

    @Transactional
    fun delete(id: Long): DeleteResult {
        if (!accountRepository.existsById(id)) return DeleteResult.NOT_FOUND
        val transactions = transactionRepository.findByAccountId(id)
        if (transactions.isNotEmpty()) return DeleteResult.HAS_TRANSACTIONS
        balanceSnapshotRepository.deleteByAccountId(id)
        accountRepository.deleteById(id)
        return DeleteResult.DELETED
    }
}
