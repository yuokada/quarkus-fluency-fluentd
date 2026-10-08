# Coding guidelines

- Follow the existing Java 17 conventions and Spotless/Palantir formatting.
- Use descriptive camelCase names for variables and methods, PascalCase for types, and UPPER_SNAKE_CASE for constants.
- Prefer guard clauses to deeply nested conditionals. Target at most two nested if/for levels and cyclomatic complexity <= 10.
- Prefer switch expressions where they improve clarity and are supported by Java 17.
- Extract complex conditions only when doing so improves readability; avoid unnecessary abstractions.
- Preserve behavior and public APIs. Do not refactor unrelated legacy code to satisfy a new rule.
- Run `mvn -Pcheckstyle-trial validate` to inspect findings. This trial reports warnings and does not fail the build.
