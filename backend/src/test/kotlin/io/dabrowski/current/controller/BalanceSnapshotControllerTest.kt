package io.dabrowski.current.controller

import io.dabrowski.current.service.BalanceSnapshotResponse
import io.dabrowski.current.service.BalanceSnapshotService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime

@WebMvcTest(BalanceSnapshotController::class)
class BalanceSnapshotControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var balanceSnapshotService: BalanceSnapshotService

    @Test
    fun `should record snapshot successfully`() {
        // Given
        val response =
            BalanceSnapshotResponse(
                totalValue = BigDecimal("2500.00"),
                snapshotDate = LocalDateTime.of(2026, 1, 15, 12, 0),
            )
        `when`(balanceSnapshotService.recordSnapshot(1L)).thenReturn(response)

        // Expect
        mockMvc
            .perform(post("/api/snapshots/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalValue").value(2500.00))
    }

    @Test
    fun `should return 404 when recording snapshot for non-existent account`() {
        // Given
        `when`(balanceSnapshotService.recordSnapshot(999L)).thenReturn(null)

        // Expect
        mockMvc
            .perform(post("/api/snapshots/999"))
            .andExpect(status().isNotFound())
    }

    @Test
    fun `should return snapshot history`() {
        // Given
        val history =
            listOf(
                BalanceSnapshotResponse(
                    totalValue = BigDecimal("1000.00"),
                    snapshotDate = LocalDateTime.of(2026, 1, 1, 12, 0),
                ),
                BalanceSnapshotResponse(
                    totalValue = BigDecimal("1500.00"),
                    snapshotDate = LocalDateTime.of(2026, 1, 2, 12, 0),
                ),
            )
        `when`(balanceSnapshotService.getHistory(1L)).thenReturn(history)

        // Expect
        mockMvc
            .perform(get("/api/snapshots/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].totalValue").value(1000.00))
            .andExpect(jsonPath("$[1].totalValue").value(1500.00))
    }

    @Test
    fun `should return empty list when no snapshots exist`() {
        // Given
        `when`(balanceSnapshotService.getHistory(1L)).thenReturn(emptyList())

        // Expect
        mockMvc
            .perform(get("/api/snapshots/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0))
    }
}
