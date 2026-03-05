package io.dabrowski.current.service

import io.dabrowski.current.entity.BalanceSnapshot
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.BalanceSnapshotRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class BalanceSnapshotService(
    private val balanceSnapshotRepository: BalanceSnapshotRepository,
    private val accountRepository: AccountRepository,
    private val transactionService: TransactionService,
) {
    fun recordSnapshot(accountId: Long): BalanceSnapshotResponse? {
        val account = accountRepository.findById(accountId).orElse(null) ?: return null

        // Check if a recent snapshot exists (within the last hour)
        val latest = balanceSnapshotRepository.findLatestByAccountId(accountId)
        if (latest != null && latest.snapshotDate.isAfter(LocalDateTime.now().minusHours(1))) {
            return BalanceSnapshotResponse(
                totalValue = latest.totalValue,
                snapshotDate = latest.snapshotDate,
            )
        }

        // Compute total value: cash + holdings value
        val holdings = transactionService.getHoldings(accountId)
        val holdingsValue = holdings.sumOf { it.quantity * it.averagePrice }
        val totalValue = account.cashBalance + holdingsValue

        val snapshot =
            BalanceSnapshot(
                account = account,
                totalValue = totalValue,
            )
        val saved = balanceSnapshotRepository.save(snapshot)

        return BalanceSnapshotResponse(
            totalValue = saved.totalValue,
            snapshotDate = saved.snapshotDate,
        )
    }

    fun getHistory(accountId: Long): List<BalanceSnapshotResponse> =
        balanceSnapshotRepository.findByAccountIdOrderByDateAsc(accountId).map {
            BalanceSnapshotResponse(
                totalValue = it.totalValue,
                snapshotDate = it.snapshotDate,
            )
        }
}

data class BalanceSnapshotResponse(
    val totalValue: BigDecimal,
    val snapshotDate: LocalDateTime,
)
