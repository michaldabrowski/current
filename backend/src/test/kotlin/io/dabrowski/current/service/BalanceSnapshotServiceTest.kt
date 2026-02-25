package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.BalanceSnapshot
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.BalanceSnapshotRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BalanceSnapshotServiceTest {
    @Mock
    private lateinit var balanceSnapshotRepository: BalanceSnapshotRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var transactionService: TransactionService

    @InjectMocks
    private lateinit var balanceSnapshotService: BalanceSnapshotService

    companion object {
        private val TEST_ACCOUNT = Account(id = 1L, name = "Test Account", cashBalance = BigDecimal("1000.00"))
    }

    @Test
    fun `should record snapshot when no recent snapshot exists`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))
        `when`(balanceSnapshotRepository.findLatestByAccountId(1L)).thenReturn(null)
        `when`(transactionService.getHoldings(1L)).thenReturn(
            listOf(
                HoldingResponse(
                    symbol = "AAPL",
                    quantity = BigDecimal("10"),
                    averagePrice = BigDecimal("150.00"),
                    assetType = AssetType.STOCK,
                ),
            ),
        )
        `when`(balanceSnapshotRepository.save(org.mockito.ArgumentMatchers.any(BalanceSnapshot::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result = balanceSnapshotService.recordSnapshot(1L)

        // Then
        assertNotNull(result)
        assertEquals(0, BigDecimal("2500.00").compareTo(result!!.totalValue))
        verify(balanceSnapshotRepository).save(org.mockito.ArgumentMatchers.any(BalanceSnapshot::class.java))
    }

    @Test
    fun `should skip snapshot when recent snapshot exists within one hour`() {
        // Given
        val recentSnapshot =
            BalanceSnapshot(
                id = 1L,
                account = TEST_ACCOUNT,
                totalValue = BigDecimal("2500.00"),
                snapshotDate = LocalDateTime.now().minusMinutes(30),
            )
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))
        `when`(balanceSnapshotRepository.findLatestByAccountId(1L)).thenReturn(recentSnapshot)

        // When
        val result = balanceSnapshotService.recordSnapshot(1L)

        // Then
        assertNotNull(result)
        assertEquals(0, BigDecimal("2500.00").compareTo(result!!.totalValue))
        verify(balanceSnapshotRepository, never()).save(org.mockito.ArgumentMatchers.any(BalanceSnapshot::class.java))
    }

    @Test
    fun `should record snapshot when last snapshot is older than one hour`() {
        // Given
        val oldSnapshot =
            BalanceSnapshot(
                id = 1L,
                account = TEST_ACCOUNT,
                totalValue = BigDecimal("2000.00"),
                snapshotDate = LocalDateTime.now().minusHours(2),
            )
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))
        `when`(balanceSnapshotRepository.findLatestByAccountId(1L)).thenReturn(oldSnapshot)
        `when`(transactionService.getHoldings(1L)).thenReturn(emptyList())
        `when`(balanceSnapshotRepository.save(org.mockito.ArgumentMatchers.any(BalanceSnapshot::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result = balanceSnapshotService.recordSnapshot(1L)

        // Then
        assertNotNull(result)
        assertEquals(0, BigDecimal("1000.00").compareTo(result!!.totalValue))
        verify(balanceSnapshotRepository).save(org.mockito.ArgumentMatchers.any(BalanceSnapshot::class.java))
    }

    @Test
    fun `should return null when account does not exist`() {
        // Given
        `when`(accountRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = balanceSnapshotService.recordSnapshot(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return snapshot history for account`() {
        // Given
        val snapshots =
            listOf(
                BalanceSnapshot(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    totalValue = BigDecimal("1000.00"),
                    snapshotDate = LocalDateTime.of(2026, 1, 1, 12, 0),
                ),
                BalanceSnapshot(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    totalValue = BigDecimal("1500.00"),
                    snapshotDate = LocalDateTime.of(2026, 1, 2, 12, 0),
                ),
            )
        `when`(balanceSnapshotRepository.findByAccountIdOrderByDateAsc(1L)).thenReturn(snapshots)

        // When
        val result = balanceSnapshotService.getHistory(1L)

        // Then
        assertEquals(2, result.size)
        assertEquals(0, BigDecimal("1000.00").compareTo(result[0].totalValue))
        assertEquals(0, BigDecimal("1500.00").compareTo(result[1].totalValue))
    }

    @Test
    fun `should return empty list when no snapshots exist`() {
        // Given
        `when`(balanceSnapshotRepository.findByAccountIdOrderByDateAsc(1L)).thenReturn(emptyList())

        // When
        val result = balanceSnapshotService.getHistory(1L)

        // Then
        assertTrue(result.isEmpty())
    }
}
