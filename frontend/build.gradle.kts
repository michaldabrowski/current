plugins {
    id("com.github.node-gradle.node")
    base // Add base plugin to get standard lifecycle tasks
}

node {
    version = "25.2.1"
    npmVersion = "11.10.1"
    download = true
}

val frontendDistDir = layout.projectDirectory.dir("dist")

// Configure the existing npmInstall task
tasks.named("npmInstall") {
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
}

// Create a custom build task for frontend
tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildFrontend") {
    group = "build"
    description = "Build the frontend application"
    dependsOn("npmInstall")
    args = listOf("run", "build")
    inputs.dir("src")
    inputs.file("package.json")
    inputs.file("vite.config.ts")
    inputs.file("tsconfig.json")
    outputs.dir(frontendDistDir)
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("dev") {
    group = "application"
    description = "Start the frontend development server"
    dependsOn("npmInstall")
    args = listOf("run", "dev")
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("test") {
    group = "verification"
    description = "Run frontend tests"
    dependsOn("npmInstall")
    args = listOf("run", "test")
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("lint") {
    group = "verification"
    description = "Run ESLint"
    dependsOn("npmInstall")
    args = listOf("run", "lint")
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("typeCheck") {
    group = "verification"
    description = "Run TypeScript type checking"
    dependsOn("npmInstall")
    args = listOf("run", "type-check")
}

// Configure the existing clean task instead of creating a new one
tasks.named<Delete>("clean") {
    delete(frontendDistDir)
    delete("node_modules/.cache")
}

// Configure the existing build task to depend on our frontend build
tasks.named("build") {
    dependsOn("buildFrontend")
}

tasks.named("assemble") {
    dependsOn("buildFrontend")
}

// Only include type checking in the standard check task, not linting
tasks.named("check") {
    dependsOn("typeCheck")
}
