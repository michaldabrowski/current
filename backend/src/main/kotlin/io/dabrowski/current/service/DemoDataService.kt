package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.BalanceSnapshot
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.BalanceSnapshotRepository
import io.dabrowski.current.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class DemoDataService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val balanceSnapshotRepository: BalanceSnapshotRepository,
) {
    companion object {
        const val DEMO_ACCOUNT_NAME = "Demo Portfolio"
    }

    fun findDemoAccount(): Account? = accountRepository.findByName(DEMO_ACCOUNT_NAME)

    @Transactional
    fun seed(): Account {
        // Remove existing demo account if present
        val existing = accountRepository.findByName(DEMO_ACCOUNT_NAME)
        if (existing != null) {
            balanceSnapshotRepository.deleteByAccountId(existing.id!!)
            transactionRepository.deleteAll(transactionRepository.findByAccountId(existing.id))
            accountRepository.delete(existing)
        }

        val now = LocalDateTime.now()

        // Create account with cash
        val account =
            accountRepository.save(
                Account(
                    name = DEMO_ACCOUNT_NAME,
                    cashBalance = BigDecimal("5240.50"),
                    createdAt = now.minusDays(30),
                ),
            )

        // Create realistic transactions over the past 30 days
        val transactions =
            listOf(
                txn(account, "AAPL", TransactionType.BUY, AssetType.STOCK, "15", "178.50", now.minusDays(28)),
                txn(account, "GOOGL", TransactionType.BUY, AssetType.STOCK, "8", "141.20", now.minusDays(25)),
                txn(account, "BTC", TransactionType.BUY, AssetType.CRYPTO, "0.12", "43500.00", now.minusDays(22)),
                txn(account, "AAPL", TransactionType.BUY, AssetType.STOCK, "10", "182.30", now.minusDays(18)),
                txn(account, "ETH", TransactionType.BUY, AssetType.CRYPTO, "1.5", "2280.00", now.minusDays(15)),
                txn(account, "MSFT", TransactionType.BUY, AssetType.STOCK, "12", "415.60", now.minusDays(12)),
                txn(account, "AAPL", TransactionType.SELL, AssetType.STOCK, "5", "191.20", now.minusDays(9)),
                txn(account, "TSLA", TransactionType.BUY, AssetType.STOCK, "6", "248.90", now.minusDays(7)),
                txn(account, "BTC", TransactionType.BUY, AssetType.CRYPTO, "0.05", "44800.00", now.minusDays(4)),
                txn(account, "GOOGL", TransactionType.SELL, AssetType.STOCK, "3", "148.50", now.minusDays(2)),
                txn(account, "SOL", TransactionType.BUY, AssetType.CRYPTO, "20", "98.40", now.minusDays(1)),
            )
        transactionRepository.saveAll(transactions)

        // Create daily balance snapshots for the chart
        val snapshots =
            listOf(
                snapshot(account, "18500.00", now.minusDays(28)),
                snapshot(account, "19200.00", now.minusDays(25)),
                snapshot(account, "24100.00", now.minusDays(22)),
                snapshot(account, "24800.00", now.minusDays(20)),
                snapshot(account, "26500.00", now.minusDays(18)),
                snapshot(account, "27200.00", now.minusDays(16)),
                snapshot(account, "30600.00", now.minusDays(15)),
                snapshot(account, "31100.00", now.minusDays(13)),
                snapshot(account, "36000.00", now.minusDays(12)),
                snapshot(account, "35400.00", now.minusDays(10)),
                snapshot(account, "34800.00", now.minusDays(9)),
                snapshot(account, "36300.00", now.minusDays(7)),
                snapshot(account, "37100.00", now.minusDays(5)),
                snapshot(account, "39500.00", now.minusDays(4)),
                snapshot(account, "38900.00", now.minusDays(3)),
                snapshot(account, "40200.00", now.minusDays(2)),
                snapshot(account, "41800.00", now.minusDays(1)),
                snapshot(account, "42350.00", now),
            )
        balanceSnapshotRepository.saveAll(snapshots)

        return account
    }

    @Transactional
    fun reset(): Boolean {
        val account = accountRepository.findByName(DEMO_ACCOUNT_NAME) ?: return false
        balanceSnapshotRepository.deleteByAccountId(account.id!!)
        transactionRepository.deleteAll(transactionRepository.findByAccountId(account.id))
        accountRepository.delete(account)
        return true
    }

    private fun txn(
        account: Account,
        symbol: String,
        type: TransactionType,
        assetType: AssetType,
        qty: String,
        price: String,
        date: LocalDateTime,
    ): Transaction {
        val q = BigDecimal(qty)
        val p = BigDecimal(price)
        return Transaction(
            account = account,
            symbol = symbol,
            type = type,
            assetType = assetType,
            quantity = q,
            price = p,
            totalAmount = q * p,
            transactionDate = date,
        )
    }

    private fun snapshot(
        account: Account,
        value: String,
        date: LocalDateTime,
    ): BalanceSnapshot =
        BalanceSnapshot(
            account = account,
            totalValue = BigDecimal(value),
            snapshotDate = date,
        )
}
