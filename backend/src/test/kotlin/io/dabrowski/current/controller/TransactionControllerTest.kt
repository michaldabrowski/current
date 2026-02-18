package io.dabrowski.current.controller

import io.dabrowski.current.entity.Account
import io.dabrowski.current.entity.AssetType
import io.dabrowski.current.entity.Transaction
import io.dabrowski.current.entity.TransactionType
import io.dabrowski.current.service.HoldingResponse
import io.dabrowski.current.service.TransactionService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal

@WebMvcTest(TransactionController::class)
class TransactionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        private val TEST_ACCOUNT = Account(id = 1L, name = "Test Account")
        private val SAMPLE_TRANSACTION =
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
    }

    @Test
    fun `should return all transactions`() {
        // Given
        `when`(transactionService.findAll()).thenReturn(listOf(SAMPLE_TRANSACTION))

        // Expect
        mockMvc
            .perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].quantity").value(10))
            .andExpect(jsonPath("$[0].price").value(150.00))
    }

    @Test
    fun `should return transaction by id`() {
        // Given
        `when`(transactionService.findById(1L)).thenReturn(SAMPLE_TRANSACTION)

        // Expect
        mockMvc
            .perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.type").value("BUY"))
            .andExpect(jsonPath("$.assetType").value("STOCK"))
    }

    @Test
    fun `should return 404 when transaction not found`() {
        // Given
        `when`(transactionService.findById(999L)).thenReturn(null)

        // Expect
        mockMvc
            .perform(get("/api/transactions/999"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `should return transactions by account id`() {
        // Given
        `when`(transactionService.findByAccountId(1L))
            .thenReturn(listOf(SAMPLE_TRANSACTION))

        // Expect
        mockMvc
            .perform(get("/api/transactions/account/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
    }

    @Test
    fun `should return transactions by symbol`() {
        // Given
        `when`(transactionService.findBySymbol("AAPL"))
            .thenReturn(listOf(SAMPLE_TRANSACTION))

        // Expect
        mockMvc
            .perform(get("/api/transactions/symbol/AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `should create transaction successfully`() {
        // Given
        doAnswer { SAMPLE_TRANSACTION }
            .`when`(transactionService)
            .create(
                accountId = 1L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                notes = null,
            )

        val request =
            CreateTransactionRequest(
                accountId = 1L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
            )

        // Expect
        mockMvc
            .perform(
                post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.type").value("BUY"))
            .andExpect(jsonPath("$.totalAmount").value(1500.00))
    }

    @Test
    fun `should return 400 when creating transaction for non-existent account`() {
        // Given
        doAnswer { null }
            .`when`(transactionService)
            .create(
                accountId = 999L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
                notes = null,
            )

        val request =
            CreateTransactionRequest(
                accountId = 999L,
                symbol = "AAPL",
                type = TransactionType.BUY,
                assetType = AssetType.STOCK,
                quantity = BigDecimal("10"),
                price = BigDecimal("150.00"),
            )

        // Expect
        mockMvc
            .perform(
                post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest())
    }

    @Test
    fun `should return holdings for account`() {
        // Given
        val holdings =
            listOf(
                HoldingResponse(
                    symbol = "AAPL",
                    quantity = BigDecimal("10"),
                    averagePrice = BigDecimal("150.00"),
                    assetType = AssetType.STOCK,
                ),
                HoldingResponse(
                    symbol = "BTC",
                    quantity = BigDecimal("0.5"),
                    averagePrice = BigDecimal("50000.00"),
                    assetType = AssetType.CRYPTO,
                ),
            )
        `when`(transactionService.getHoldings(1L)).thenReturn(holdings)

        // Expect
        mockMvc
            .perform(get("/api/transactions/holdings/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].quantity").value(10))
            .andExpect(jsonPath("$[0].averagePrice").value(150.00))
            .andExpect(jsonPath("$[0].assetType").value("STOCK"))
            .andExpect(jsonPath("$[1].symbol").value("BTC"))
            .andExpect(jsonPath("$[1].assetType").value("CRYPTO"))
    }

    @Test
    fun `should return 400 when creating transaction with malformed JSON`() {
        // Expect
        mockMvc
            .perform(
                post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"),
            ).andExpect(status().isBadRequest())
    }
}
