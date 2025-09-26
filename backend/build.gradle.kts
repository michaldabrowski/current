import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "io.dabrowski"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kotlin Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // Development Tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

ktlint {
    version.set("1.7.1")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-parameters",
            "-Xannotation-default-target=param-property",
        )
        jvmTarget.set(JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configure backend to serve frontend static files
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    dependsOn(":frontend:buildFrontend")
    from("${project(":frontend").layout.buildDirectory.dir("dist").get()}") {
        into("static")
    }
}

// Development task that doesn't include frontend files
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("bootRunDev") {
    group = "application"
    description = "Run the application for development (without frontend static files)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "io.dabrowski.current.CurrentApplicationKt"
}
