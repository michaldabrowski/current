plugins {
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.2.10" apply false
    kotlin("plugin.spring") version "2.2.20" apply false
    kotlin("plugin.jpa") version "2.2.10" apply false
    id("com.github.node-gradle.node") version "7.1.0" apply false
}

allprojects {
    version = "0.1.0"
}

// Global tasks for the entire project
tasks.register("buildAll") {
    group = "build"
    description = "Build both backend and frontend"
    dependsOn(":backend:build", ":frontend:build")
}

tasks.register("runDev") {
    group = "application"
    description = "Run both backend and frontend for development"
    doLast {
        println("=".repeat(80))
        println("To run both backend and frontend for development:")
        println("1. In terminal 1, run: ./gradlew :backend:bootRunDev")
        println("2. In terminal 2, run: ./gradlew :frontend:dev")
        println()
        println("Backend will be available at: http://localhost:8080")
        println("Frontend will be available at: http://localhost:5173")
        println("=".repeat(80))
    }
}

tasks.register("runBackend") {
    group = "application"
    description = "Run backend for development"
    dependsOn(":backend:bootRunDev")
}

tasks.register("runFrontend") {
    group = "application"
    description = "Run frontend for development"
    dependsOn(":frontend:dev")
}

tasks.register("testAll") {
    group = "verification"
    description = "Run all tests in the project"
    dependsOn(":backend:test", ":frontend:test")
}

tasks.register("cleanAll") {
    group = "build"
    description = "Clean all modules"
    dependsOn(":backend:clean", ":frontend:clean")
}
