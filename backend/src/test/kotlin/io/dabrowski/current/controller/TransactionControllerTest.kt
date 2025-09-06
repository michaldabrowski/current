package io.dabrowski.current.controller

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class TransactionControllerTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var transactionController: TransactionController

    @Test
    fun `getHoldings should calculate correct average price with multiple buys`() {
        // Given
        val accountId = 1L
        val account = Account(id = accountId, name = "Test Account")

        val transactions = listOf(
            Transaction(
                id = 1L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("100.00"),
                totalAmount = BigDecimal("1000.00")
            ),
            Transaction(
                id = 2L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("200.00"),
                totalAmount = BigDecimal("2000.00")
            )
        )

        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionController.getHoldings(accountId)

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
        val account = Account(id = accountId, name = "Test Account")

        val transactions = listOf(
            Transaction(
                id = 1L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("20"),
                price = BigDecimal("100.00"),
                totalAmount = BigDecimal("2000.00")
            ),
            Transaction(
                id = 2L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.SELL,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("5"),
                price = BigDecimal("120.00"),
                totalAmount = BigDecimal("600.00")
            )
        )

        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionController.getHoldings(accountId)

        // Then
        assertEquals(1, holdings.size)
        val appleHolding = holdings[0]
        assertEquals("AAPL", appleHolding.symbol)
        assertEquals(BigDecimal("15"), appleHolding.quantity) // 20 - 5 = 15
        assertEquals(BigDecimal("100.00"), appleHolding.averagePrice) // Still $100 avg purchase price
        assertEquals(AssetType.STOCK, appleHolding.assetType)
    }

    @Test
    fun `getHoldings should exclude assets with zero or negative quantity`() {
        // Given
        val accountId = 1L
        val account = Account(id = accountId, name = "Test Account")

        val transactions = listOf(
            Transaction(
                id = 1L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("100.00"),
                totalAmount = BigDecimal("1000.00")
            ),
            Transaction(
                id = 2L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.SELL,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("120.00"),
                totalAmount = BigDecimal("1200.00")
            )
        )

        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionController.getHoldings(accountId)

        // Then
        assertEquals(0, holdings.size) // Should be empty since we sold everything
    }

    @Test
    fun `getHoldings should handle multiple different assets`() {
        // Given
        val accountId = 1L
        val account = Account(id = accountId, name = "Test Account")

        val transactions = listOf(
            Transaction(
                id = 1L,
                account = account,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                totalAmount = BigDecimal("1500.00")
            ),
            Transaction(
                id = 2L,
                account = account,
                symbol = "BTC",
                type = TransactionType.BUY,
                assetType = AssetType.CRYPTO,
                quantity = BigDecimal("0.5"),
                price = BigDecimal("50000.00"),
                totalAmount = BigDecimal("25000.00")
            )
        )

        `when`(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // When
        val holdings = transactionController.getHoldings(accountId)

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
}
