package io.dabrowski.current

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CurrentApplication

fun main(args: Array<String>) {
    runApplication<CurrentApplication>(*args)
}
