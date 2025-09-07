package io.dabrowski.current.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.dabrowski.current.entity.Account
import io.dabrowski.current.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.Optional

@WebMvcTest(AccountController::class)
class AccountControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private const val NON_EXISTENT_ACCOUNT_ID = 999L
        private val SAMPLE_ACCOUNT_1 = Account(1L, "Account 1", BigDecimal("1000.00"))
        private val SAMPLE_ACCOUNT_2 = Account(2L, "Account 2", BigDecimal("2000.00"))
        private val TEST_ACCOUNT = Account(1L, "Test Account", BigDecimal("1500.00"))
        private val NEW_ACCOUNT = Account(1L, "New Account", BigDecimal("5000.00"))
    }

    @Test
    fun `should return all accounts`() {
        // Given
        val accounts = listOf(SAMPLE_ACCOUNT_1, SAMPLE_ACCOUNT_2)
        `when`(accountRepository.findAll()).thenReturn(accounts)

        // Expect
        mockMvc
            .perform(get("/api/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Account 1"))
            .andExpect(jsonPath("$[0].cashBalance").value(1000.00))
            .andExpect(jsonPath("$[1].name").value("Account 2"))
            .andExpect(jsonPath("$[1].cashBalance").value(2000.00))
    }

    @Test
    fun `should return account by id`() {
        // Given
        `when`(accountRepository.findById(1L)).thenReturn(Optional.of(TEST_ACCOUNT))

        // Expect
        mockMvc
            .perform(get("/api/accounts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Account"))
            .andExpect(jsonPath("$.cashBalance").value(1500.00))
    }

    @Test
    fun `should return 404 when account not found`() {
        // Given
        `when`(accountRepository.findById(NON_EXISTENT_ACCOUNT_ID)).thenReturn(Optional.empty())

        // Expect
        mockMvc
            .perform(get("/api/accounts/$NON_EXISTENT_ACCOUNT_ID"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `should create new account`() {
        // Given
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        `when`(accountRepository.save(accountCaptor.capture())).thenReturn(NEW_ACCOUNT)

        val request = CreateAccountRequest(name = "New Account", initialBalance = BigDecimal("5000.00"))

        // Expect
        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Account"))
            .andExpect(jsonPath("$.cashBalance").value(5000.00))

        verify(accountRepository).save(any())
        val savedAccount = accountCaptor.value
        assert(savedAccount.name == "New Account")
        assert(savedAccount.cashBalance == BigDecimal("5000.00"))
    }

    @Test
    fun `should return 400 when creating account with invalid data`() {
        val invalidRequest = CreateAccountRequest(name = "", initialBalance = BigDecimal("-100.00"))

        // Expect
        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)),
            ).andExpect(status().isBadRequest())
    }

    @Test
    fun `should return 400 when creating account with null name`() {
        // Expect
        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialBalance": 1000.00}"""),
            ).andExpect(status().isBadRequest())
    }

    @Test
    fun `should return 400 when creating account with malformed JSON`() {
        // Expect
        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"),
            ).andExpect(status().isBadRequest())
    }

    @Test
    fun `should handle large decimal values correctly`() {
        // Given
        val largeBalance = BigDecimal("999999999999.99")
        val accountWithLargeBalance = Account(1L, "Rich Account", largeBalance)
        val accountCaptor = ArgumentCaptor.forClass(Account::class.java)
        `when`(accountRepository.save(accountCaptor.capture())).thenReturn(accountWithLargeBalance)

        val request = CreateAccountRequest(name = "Rich Account", initialBalance = largeBalance)

        // Expect
        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.cashBalance").value(999999999999.99))
    }
}
