# AGENTS.md

Financial portfolio tracking application. Kotlin/Spring Boot backend + Vue 3/TypeScript frontend, structured as a Gradle multi-module project with PostgreSQL for persistence.

## Commands

- `./gradlew buildAll` — Build everything
- `./gradlew testAll` — Run all tests
- `./gradlew runBackend` — Start backend (port 8080)
- `./gradlew runFrontend` — Start frontend dev server (port 5173)
- `./gradlew :backend:ktlintCheck` — Kotlin lint
- `./gradlew :frontend:lint` — Frontend lint
- `./gradlew :frontend:typeCheck` — TypeScript type check

## Backend

- Kotlin with ktlint (official style), 4-space indent, 120 char max line length
- Layered architecture: Entity → Repository → Service → Controller. Introduce a service layer between controllers and repositories.
- Constructor injection via primary constructors
- Data classes for JPA entities and request/response DTOs; DTOs are co-located in controller files
- Jakarta Bean Validation on request bodies (`@NotBlank`, `@PositiveOrZero`)
- Tests use JUnit 5 + Mockito, `@WebMvcTest` for controllers, backtick method names (e.g., `` `should return all accounts`() ``)

## Frontend

- TypeScript with ESLint, 2-space indent, 100 char max line length
- Vue 3 Composition API with `<script setup lang="ts">`
- Pinia setup stores (function-based `defineStore`)
- `@/` path alias for `./src`
- Tailwind CSS v4 for styling with custom component classes in `main.css`
- Axios-based API service module with typed interfaces

## Important Notes

- CORS is configured for `localhost:5173` only
- Frontend tests are not yet implemented (placeholder script)
- Gradle's node-gradle plugin manages Node.js — do not assume system Node.js
- Environment variables are loaded from `.env` (generated via `./setup-env.sh`)
