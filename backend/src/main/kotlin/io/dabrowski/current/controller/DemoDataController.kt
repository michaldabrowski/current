package io.dabrowski.current.controller

import io.dabrowski.current.entity.Account
import io.dabrowski.current.service.DemoDataService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/demo")
class DemoDataController(
    private val demoDataService: DemoDataService,
) {
    @PostMapping("/seed")
    fun seed(): Account = demoDataService.seed()

    @DeleteMapping("/reset")
    fun reset(): ResponseEntity<Void> =
        if (demoDataService.reset()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
}
