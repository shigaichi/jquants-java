# Repository Guidelines

## Project Structure & Module Organization

- Java 11 Maven project rooted at `pom.xml`.
- Source lives in `src/main/java/io/github/shigaichi/jquants/` (e.g., `App.java`, `TestDto.java`).
- Tests belong in `src/test/java/` under the same package structure; `target/` holds build outputs and coverage reports.

## Build, Test, and Development Commands

- `./mvnw clean package` — compile, run tests, and build the JAR.
- `./mvnw test` — fast cycle JUnit 5 tests only (stops before signing/verification).
- `./mvnw verify` — full pipeline with JaCoCo coverage check and GPG signing (requires local keys).
- `./mvnw spotless:apply` — format code with Google Java Format (AOSP style).

## Coding Style & Naming Conventions

- Use AOSP Google Java Format via Spotless; run before committing.
- Prefer Lombok when they simplify code.
- Package naming follows `io.github.shigaichi.jquants`; new modules should stay under this root.
- Class names are PascalCase, methods/fields camelCase; keep methods small and side-effect explicit.
- Write meaningful Javadocs for public APIs.
    - Write Javadocs for private methods if they are complex and/or non-obvious.
- Write comments for non-obvious behavior.
- Write displayName() for tests, comments, error message in Japanese.
- Don't output logs, but throw exceptions instead.

## Testing Guidelines

- Framework: JUnit 5; place tests in `src/test/java` mirroring main packages.
- Name test classes `*Test` and methods with clear behavior hints (e.g., `shouldReturnName()`).
- For fast local runs, use `./mvnw test`; for coverage validation before PR, run `./mvnw verify`.

## etc

- Use serena mcp to understand the code.
