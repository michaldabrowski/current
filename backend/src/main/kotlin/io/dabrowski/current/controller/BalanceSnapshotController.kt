package io.dabrowski.current.controller

import io.dabrowski.current.service.BalanceSnapshotResponse
import io.dabrowski.current.service.BalanceSnapshotService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/snapshots")
class BalanceSnapshotController(
    private val balanceSnapshotService: BalanceSnapshotService,
) {
    @PostMapping("/{accountId}")
    fun recordSnapshot(
        @PathVariable accountId: Long,
    ): ResponseEntity<BalanceSnapshotResponse> {
        val snapshot = balanceSnapshotService.recordSnapshot(accountId)
        return if (snapshot != null) {
            ResponseEntity.ok(snapshot)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{accountId}")
    fun getHistory(
        @PathVariable accountId: Long,
    ): List<BalanceSnapshotResponse> = balanceSnapshotService.getHistory(accountId)
}
