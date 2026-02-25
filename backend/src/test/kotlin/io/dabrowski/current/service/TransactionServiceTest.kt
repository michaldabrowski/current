package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TransactionServiceTest {
    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var transactionService: TransactionService

    companion object {
        private val TEST_ACCOUNT = Account(id = 1L, name = "Test Account")
    }

    @Test
    fun `should return all transactions`() {
        // Given
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("150.00"),
                    totalAmount = BigDecimal("1500.00"),
                ),
            )
        `when`(transactionRepository.findAll()).thenReturn(transactions)

        // When
        val result = transactionService.findAll()

        // Then
        assertEquals(1, result.size)
        assertEquals("AAPL", result[0].symbol)
    }

    @Test
    fun `should return transaction by id`() {
        // Given
        val transaction =
            Transaction(
                id = 1L,
                account = TEST_ACCOUNT,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                totalAmount = BigDecimal("1500.00"),
            )
        `when`(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction))

        // When
        val result = transactionService.findById(1L)

        // Then
        assertNotNull(result)
        assertEquals("AAPL", result!!.symbol)
    }

    @Test
    fun `should return null when transaction not found by id`() {
        // Given
        `when`(transactionRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = transactionService.findById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return transactions by account id`() {
        // Given
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("150.00"),
                    totalAmount = BigDecimal("1500.00"),
                ),
            )
        `when`(transactionRepository.findByAccountIdOrderByDateDesc(1L))
            .thenReturn(transactions)

        // When
        val result = transactionService.findByAccountId(1L)

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `should return transactions by symbol`() {
        // Given
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("150.00"),
                    totalAmount = BigDecimal("1500.00"),
                ),
            )
        `when`(transactionRepository.findBySymbol("AAPL")).thenReturn(transactions)

        // When
        val result = transactionService.findBySymbol("AAPL")

        // Then
        assertEquals(1, result.size)
        assertEquals("AAPL", result[0].symbol)
    }

    @Test
    fun `should create transaction for existing account`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))
        `when`(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result =
            transactionService.create(
                accountId = 1L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                notes = "First purchase",
            )

        // Then
        assertNotNull(result)
        assertEquals("AAPL", result!!.symbol)
        assertEquals(TransactionType.BUY, result.type)
        assertEquals(BigDecimal("10"), result.quantity)
        assertEquals(BigDecimal("150.00"), result.price)
        assertEquals(BigDecimal("1500.00"), result.totalAmount)
        assertEquals("First purchase", result.notes)
    }

    @Test
    fun `should return null when creating transaction for non-existent account`() {
        // Given
        `when`(accountRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result =
            transactionService.create(
                accountId = 999L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                notes = null,
            )

        // Then
        assertNull(result)
    }

    @Test
    fun `should calculate totalAmount as quantity times price`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))
        `when`(transactionRepository.save(org.mockito.ArgumentMatchers.any(Transaction::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result =
            transactionService.create(
                accountId = 1L,
                symbol = "BTC",
                type = TransactionType.BUY,
                assetType = AssetType.CRYPTO,
                quantity = BigDecimal("0.5"),
                price = BigDecimal("50000.00"),
                notes = null,
            )

        // Then
        assertNotNull(result)
        assertEquals(0, BigDecimal("25000.00").compareTo(result!!.totalAmount))
    }

    @Test
    fun `getHoldings should calculate correct average price with multiple buys`() {
        // Given
        val accountId = 1L
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("100.00"),
                    totalAmount = BigDecimal("1000.00"),
                ),
                Transaction(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("200.00"),
                    totalAmount = BigDecimal("2000.00"),
                ),
            )
        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionService.getHoldings(accountId)

        // Then
        assertEquals(1, holdings.size)
        val appleHolding = holdings[0]
        assertEquals("AAPL", appleHolding.symbol)
        assertEquals(BigDecimal("20"), appleHolding.quantity)
        assertEquals(BigDecimal("150.00"), appleHolding.averagePrice)
        assertEquals(AssetType.STOCK, appleHolding.assetType)
    }

    @Test
    fun `getHoldings should handle buy and sell transactions correctly`() {
        // Given
        val accountId = 1L
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("20"),
                    price = BigDecimal("100.00"),
                    totalAmount = BigDecimal("2000.00"),
                ),
                Transaction(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.SELL,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("5"),
                    price = BigDecimal("120.00"),
                    totalAmount = BigDecimal("600.00"),
                ),
            )
        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionService.getHoldings(accountId)

        // Then
        assertEquals(1, holdings.size)
        val appleHolding = holdings[0]
        assertEquals("AAPL", appleHolding.symbol)
        assertEquals(BigDecimal("15"), appleHolding.quantity)
        assertEquals(BigDecimal("100.00"), appleHolding.averagePrice)
        assertEquals(AssetType.STOCK, appleHolding.assetType)
    }

    @Test
    fun `getHoldings should exclude assets with zero or negative quantity`() {
        // Given
        val accountId = 1L
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("100.00"),
                    totalAmount = BigDecimal("1000.00"),
                ),
                Transaction(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.SELL,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("120.00"),
                    totalAmount = BigDecimal("1200.00"),
                ),
            )
        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionService.getHoldings(accountId)

        // Then
        assertTrue(holdings.isEmpty())
    }

    @Test
    fun `getHoldings should handle multiple different assets`() {
        // Given
        val accountId = 1L
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("150.00"),
                    totalAmount = BigDecimal("1500.00"),
                ),
                Transaction(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    symbol = "BTC",
                    type = TransactionType.BUY,
                    assetType = AssetType.CRYPTO,
                    quantity = BigDecimal("0.5"),
                    price = BigDecimal("50000.00"),
                    totalAmount = BigDecimal("25000.00"),
                ),
            )
        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionService.getHoldings(accountId)

        // Then
        assertEquals(2, holdings.size)

        val appleHolding = holdings.find { it.symbol == "AAPL" }!!
        assertEquals(BigDecimal("10"), appleHolding.quantity)
        assertEquals(BigDecimal("150.00"), appleHolding.averagePrice)
        assertEquals(AssetType.STOCK, appleHolding.assetType)

        val btcHolding = holdings.find { it.symbol == "BTC" }!!
        assertEquals(BigDecimal("0.5"), btcHolding.quantity)
        assertEquals(BigDecimal("50000.00"), btcHolding.averagePrice)
        assertEquals(AssetType.CRYPTO, btcHolding.assetType)
    }

    @Test
    fun `getHoldings should return empty list when no transactions exist`() {
        // Given
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(emptyList())

        // When
        val holdings = transactionService.getHoldings(1L)

        // Then
        assertTrue(holdings.isEmpty())
    }

    @Test
    fun `should delete existing transaction`() {
        // Given
        `when`(transactionRepository.existsById(1L)).thenReturn(true)

        // When
        val result = transactionService.delete(1L)

        // Then
        assertTrue(result)
        verify(transactionRepository).deleteById(1L)
    }

    @Test
    fun `should return false when deleting non-existent transaction`() {
        // Given
        `when`(transactionRepository.existsById(999L)).thenReturn(false)

        // When
        val result = transactionService.delete(999L)

        // Then
        assertFalse(result)
        verify(transactionRepository, never()).deleteById(999L)
    }

    @Test
    fun `should delete all transactions by account id`() {
        // Given
        val transactions =
            listOf(
                Transaction(
                    id = 1L,
                    account = TEST_ACCOUNT,
                    symbol = "AAPL",
                    type = TransactionType.BUY,
                    assetType = AssetType.STOCK,
                    quantity = BigDecimal("10"),
                    price = BigDecimal("150.00"),
                    totalAmount = BigDecimal("1500.00"),
                ),
                Transaction(
                    id = 2L,
                    account = TEST_ACCOUNT,
                    symbol = "BTC",
                    type = TransactionType.BUY,
                    assetType = AssetType.CRYPTO,
                    quantity = BigDecimal("0.5"),
                    price = BigDecimal("50000.00"),
                    totalAmount = BigDecimal("25000.00"),
                ),
            )
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(transactions)

        // When
        val result = transactionService.deleteAllByAccountId(1L)

        // Then
        assertEquals(2, result)
        verify(transactionRepository).deleteAll(transactions)
    }

    @Test
    fun `should return zero when deleting all transactions for account with none`() {
        // Given
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(emptyList())

        // When
        val result = transactionService.deleteAllByAccountId(1L)

        // Then
        assertEquals(0, result)
        verify(transactionRepository, never()).deleteAll(org.mockito.ArgumentMatchers.anyList())
    }
}
