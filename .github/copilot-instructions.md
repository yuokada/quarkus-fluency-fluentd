# GitHub Copilot Instructions

Follow these repository instructions when working in this project.

## General guidance

- Keep changes focused and consistent with the current Quarkus extension structure.
- Write new or updated repository instructions, comments, and documentation in English.
- Avoid machine-specific paths, local-only assumptions, and committed secrets.
- Preserve clear separation between parent build configuration, runtime module, deployment module, and integration tests.
- Keep public extension API and documented configuration stable unless the task explicitly changes them.

## Project context

- The parent build and release configuration live in the root `pom.xml`.
- Runtime code lives under `runtime/`, deployment-side code lives under `deployment/`, and verification lives under `integration-tests/`.
- CI and publishing workflows live under `.github/workflows/`.
- Formatting and Maven plugin conventions are managed centrally in the parent POM.

## Validation

- Prefer `./mvnw install -pl deployment,runtime` to build modules and run unit tests.
- Prefer `./mvnw verify -pl integration-tests -DskipITs=false` to run integration tests.
- Clearly distinguish between checks you ran and checks you did not run.
