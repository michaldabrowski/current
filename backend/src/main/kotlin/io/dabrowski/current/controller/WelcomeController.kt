package io.dabrowski.current.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WelcomeController {

    @GetMapping("/")
    fun welcome(): Map<String, String> {
        return mapOf(
            "message" to "Current API is running!",
            "health" to "/actuator/health",
            "info" to "/actuator/info"
        )
    }
}
