package io.dabrowski.current.service

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.repository.AccountRepository
import io.dabrowski.current.repository.BalanceSnapshotRepository
import io.dabrowski.current.repository.TransactionRepository
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
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class AccountServiceTest {
    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var balanceSnapshotRepository: BalanceSnapshotRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var accountService: AccountService

    companion object {
        private val ACCOUNT_1 = Account(1L, "Account 1", BigDecimal("1000.00"))
        private val ACCOUNT_2 = Account(2L, "Account 2", BigDecimal("2000.00"))
    }

    @Test
    fun `should return all accounts`() {
        // Given
        val accounts = listOf(ACCOUNT_1, ACCOUNT_2)
        `when`(accountRepository.findAll()).thenReturn(accounts)

        // When
        val result = accountService.findAll()

        // Then
        assertEquals(2, result.size)
        assertEquals("Account 1", result[0].name)
        assertEquals("Account 2", result[1].name)
    }

    @Test
    fun `should return empty list when no accounts exist`() {
        // Given
        `when`(accountRepository.findAll()).thenReturn(emptyList())

        // When
        val result = accountService.findAll()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return account by id`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT_1))

        // When
        val result = accountService.findById(1L)

        // Then
        assertNotNull(result)
        assertEquals("Account 1", result!!.name)
        assertEquals(BigDecimal("1000.00"), result.cashBalance)
    }

    @Test
    fun `should return null when account not found by id`() {
        // Given
        `when`(accountRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = accountService.findById(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return account with transactions`() {
        // Given
        `when`(accountRepository.findByIdWithTransactions(1L)).thenReturn(ACCOUNT_1)

        // When
        val result = accountService.findByIdWithTransactions(1L)

        // Then
        assertNotNull(result)
        assertEquals("Account 1", result!!.name)
    }

    @Test
    fun `should return null when account with transactions not found`() {
        // Given
        `when`(accountRepository.findByIdWithTransactions(999L)).thenReturn(null)

        // When
        val result = accountService.findByIdWithTransactions(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun `should create account with initial balance`() {
        // Given
        val savedAccount = Account(1L, "New Account", BigDecimal("5000.00"))
        `when`(accountRepository.save(org.mockito.ArgumentMatchers.any(Account::class.java)))
            .thenReturn(savedAccount)

        // When
        val result = accountService.create("New Account", BigDecimal("5000.00"))

        // Then
        assertEquals("New Account", result.name)
        assertEquals(BigDecimal("5000.00"), result.cashBalance)
    }

    @Test
    fun `should create account with zero balance when initial balance is null`() {
        // Given
        val savedAccount = Account(1L, "New Account", BigDecimal.ZERO)
        `when`(accountRepository.save(org.mockito.ArgumentMatchers.any(Account::class.java)))
            .thenReturn(savedAccount)

        // When
        val result = accountService.create("New Account", null)

        // Then
        assertEquals("New Account", result.name)
        assertEquals(BigDecimal.ZERO, result.cashBalance)
    }

    @Test
    fun `should update cash balance for existing account`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT_1))
        `when`(accountRepository.save(org.mockito.ArgumentMatchers.any(Account::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result = accountService.updateCashBalance(1L, BigDecimal("3000.00"))

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("3000.00"), result!!.cashBalance)
    }

    @Test
    fun `should return null when updating cash balance for non-existent account`() {
        // Given
        `when`(accountRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = accountService.updateCashBalance(999L, BigDecimal("3000.00"))

        // Then
        assertNull(result)
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any(Account::class.java))
    }

    @Test
    fun `should delete existing account without transactions`() {
        // Given
        `when`(accountRepository.existsById(1L)).thenReturn(true)
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(emptyList())

        // When
        val result = accountService.delete(1L)

        // Then
        assertEquals(DeleteResult.DELETED, result)
        verify(balanceSnapshotRepository).deleteByAccountId(1L)
        verify(accountRepository).deleteById(1L)
    }

    @Test
    fun `should return NOT_FOUND when deleting non-existent account`() {
        // Given
        `when`(accountRepository.existsById(999L)).thenReturn(false)

        // When
        val result = accountService.delete(999L)

        // Then
        assertEquals(DeleteResult.NOT_FOUND, result)
        verify(accountRepository, never()).deleteById(999L)
    }

    @Test
    fun `should return HAS_TRANSACTIONS when account has transactions`() {
        // Given
        `when`(accountRepository.existsById(1L)).thenReturn(true)
        val transaction =
            Transaction(
                id = 1L,
                account = ACCOUNT_1,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = io.dabrowski.current.entity.AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                totalAmount = BigDecimal("1500.00"),
            )
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(listOf(transaction))

        // When
        val result = accountService.delete(1L)

        // Then
        assertEquals(DeleteResult.HAS_TRANSACTIONS, result)
        verify(accountRepository, never()).deleteById(1L)
        verify(balanceSnapshotRepository, never()).deleteByAccountId(1L)
    }

    @Test
    fun `should adjust cash balance by positive amount`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT_1))
        `when`(accountRepository.save(org.mockito.ArgumentMatchers.any(Account::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result = accountService.adjustCash(1L, BigDecimal("500.00"))

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("1500.00"), result!!.cashBalance)
    }

    @Test
    fun `should adjust cash balance by negative amount`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT_1))
        `when`(accountRepository.save(org.mockito.ArgumentMatchers.any(Account::class.java)))
            .thenAnswer { it.arguments[0] }

        // When
        val result = accountService.adjustCash(1L, BigDecimal("-200.00"))

        // Then
        assertNotNull(result)
        assertEquals(BigDecimal("800.00"), result!!.cashBalance)
    }

    @Test
    fun `should return null when withdrawal would make balance negative`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(ACCOUNT_1))

        // When
        val result = accountService.adjustCash(1L, BigDecimal("-2000.00"))

        // Then
        assertNull(result)
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any(Account::class.java))
    }

    @Test
    fun `should return null when adjusting cash for non-existent account`() {
        // Given
        `when`(accountRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = accountService.adjustCash(999L, BigDecimal("100.00"))

        // Then
        assertNull(result)
    }
}
