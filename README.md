# Quarkus Fluency Fluentd Extension
[![CI](https://github.com/yuokada/quarkus-fluency-fluentd/actions/workflows/ci.yml/badge.svg)](https://github.com/yuokada/quarkus-fluency-fluentd/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuokada.quarkus.extension/quarkus-fluency-fluentd)](https://central.sonatype.com/artifact/io.github.yuokada.quarkus.extension/quarkus-fluency-fluentd)
[![Javadoc](https://javadoc.io/badge2/io.github.yuokada.quarkus.extension/quarkus-fluency-fluentd/javadoc.svg)](https://javadoc.io/doc/io.github.yuokada.quarkus.extension/quarkus-fluency-fluentd)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)

A Quarkus extension that integrates [Fluency](https://github.com/komamitsu/fluency) — a high-performance [Fluentd](https://www.fluentd.org/) / [Fluent Bit](https://fluentbit.io/) client for Java — into your Quarkus application as a managed CDI bean.

> This extension addresses the direct Fluentd integration request raised in [quarkusio/quarkus#453](https://github.com/quarkusio/quarkus/issues/453).

## Prerequisites

- Java 17+
- Maven 3.9+
- A running Fluentd or Fluent Bit instance (optional for development — the client degrades gracefully if unavailable)

## Installation

Add the runtime artifact to your project:

```xml
<dependency>
    <groupId>io.github.yuokada.quarkus.extension</groupId>
    <artifactId>quarkus-fluency-fluentd</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

Inject `FluencyClient` wherever you need to forward log records to Fluentd:

```java
@ApplicationScoped
public class MyService {

    @Inject
    FluencyClient fluencyClient;

    public void doSomething() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("message", "something happened");
        record.put("userId", 42);

        fluencyClient.emit("myapp.events", record);
    }
}
```

`emit()` returns `false` (and does not throw) when Fluentd is unreachable, so no special error handling is required in application code.

## Centralized Log Management

This extension is one approach to forwarding application events to a centralized log management stack such as **EFK (Elasticsearch + Fluentd + Kibana)**.

The official Quarkus guide — [Centralized Log Management](https://quarkus.io/guides/centralized-log-management) — covers an alternative approach using Quarkus's built-in **syslog handler** to push logs to Fluentd:

### Comparison with alternatives

| | `quarkus-logging-gelf` | Quarkus syslog handler | This extension (Fluency) |
|---|---|---|---|
| **Status** | **Deprecated** | Active | Active |
| **Protocol** | GELF over UDP/TCP (port 12201) | Syslog UDP/TCP (port 5140) | Fluentd forward TCP (port 24224) |
| **Transport** | jboss-logmanager log handler | jboss-logmanager log handler | Fluency client library |
| **What gets sent** | All log output automatically | All log output automatically | Only records explicitly emitted via `emit()` |
| **Wire format** | GELF JSON | Syslog (RFC 5424) | MessagePack (Fluentd native) |
| **Primary target** | Graylog (Fluentd via plugin) | Fluentd / any syslog sink | Fluentd / Fluent Bit natively |
| **Config prefix** | `quarkus.log.handler.gelf.*` | `quarkus.log.syslog.*` | `quarkus.fluency.*` |

- Use **quarkus-logging-gelf** — not recommended; deprecated in favour of OpenTelemetry Logging or the socket handler.
- Use the **syslog handler** when you want all Quarkus log output forwarded automatically with no code changes.
- Use **this extension** when you need to emit specific structured events (audit logs, metrics, domain events) from application code with full control over the Fluentd tag and record payload.

## Configuration

All properties are under the `quarkus.fluency` prefix.

| Property | Default | Description |
|---|---|---|
| `quarkus.fluency.host` | `localhost` | Fluentd host |
| `quarkus.fluency.port` | `24224` | Fluentd TCP port |
| `quarkus.fluency.enabled` | `true` | Set to `false` to disable log forwarding entirely |
| `quarkus.fluency.sender-max-retry-count` | `4` | Max send retry attempts |
| `quarkus.fluency.buffer-chunk-initial-size` | `1048576` | Buffer chunk initial size in bytes (1 MiB); must be less than retention size |
| `quarkus.fluency.buffer-chunk-retention-size` | `4194304` | Buffer chunk retention size in bytes (4 MiB); must be greater than initial size |
| `quarkus.fluency.buffer-chunk-retention-time-millis` | `1000` | Buffer flush interval in milliseconds |
| `quarkus.fluency.health.enabled` | `false` | Register a `@Readiness` check for Fluentd connectivity at `/q/health/ready`; requires `quarkus-smallrye-health` on the classpath |

Example `application.properties`:

```properties
quarkus.fluency.host=fluentd.internal
quarkus.fluency.port=24224
quarkus.fluency.sender-max-retry-count=8
```

## Building

```bash
# Build extension modules and run unit tests
./mvnw install -pl deployment,runtime

# Build everything including integration tests (requires Docker for Testcontainers)
./mvnw verify -DskipITs=false

# Skip tests
./mvnw install -DskipTests
```

## Code Style

This project uses [spotless-maven-plugin](https://github.com/diffplug/spotless/blob/main/plugin-maven/README.md) with [palantir-java-format](https://github.com/palantir/palantir-java-format) to enforce consistent Java formatting.

```bash
# Check formatting (runs automatically during verify)
./mvnw spotless:check

# Apply formatting
./mvnw spotless:apply
```

The `spotless:check` goal is bound to the `verify` phase, so CI will fail on unformatted code. Run `spotless:apply` before committing.

### Checkstyle (local violation report)

Checkstyle checks naming, nesting depth, cyclomatic complexity, star imports, parameter counts, and method length. The production and test source sets use separate rule files: `checkstyle/main.xml` and `checkstyle/test.xml`.

Run the checks from the repository root (Java 17+ and Maven 3.9+):

```bash
./mvnw -Pcheckstyle-trial validate
```

The Checkstyle profile **fails the build if the number of violations exceeds the configured limit in any Checkstyle execution**. The default is 0 violations per execution (per module and per main/test rule set). This is not a repository-wide total. You can override the limit with `-Dcheckstyle.maxAllowedViolations=5`, which permits up to 5 findings in **each** execution. Review the Maven console output for individual findings. Each module also writes separate machine-readable reports:

- `runtime/target/checkstyle-main.xml` and `runtime/target/checkstyle-test.xml`
- `deployment/target/checkstyle-main.xml` and `deployment/target/checkstyle-test.xml`
- The `integration-tests` module is excluded from Checkstyle via `checkstyle.skip=true` in its `checkstyle-trial` profile. It remains part of the Maven reactor and its other build/test goals still run.

The XML files that are produced depend on which included modules have applicable source directories. The `integration-tests` module intentionally produces no Checkstyle reports when the trial profile is active. To render the same Markdown summary used by GitHub Actions, run:

```bash
python3 .github/scripts/checkstyle_report.py > checkstyle-summary.md
cat checkstyle-summary.md
```

The summary separates **Main sources** and **Test sources**, shows counts by rule, and lists up to 50 findings for each source set. These are **threshold violations**, not numeric complexity scores for every method.

If you want to check only one module, use Maven's `-pl` option (for example, `./mvnw -pl deployment -Pcheckstyle-trial validate`). If no XML report is found, inspect the Maven output for configuration or execution errors; an absent report does not prove that there are no violations.

The GitHub Actions workflow `.github/workflows/checkstyle.yml` uses the same Maven threshold (default 0). To run locally with a different per-execution threshold, use `./mvnw -Pcheckstyle-trial -Dcheckstyle.maxAllowedViolations=5 validate`. The XML report and summary script are informational; Maven decides the CI result. This approach is proposed as an alternative to repository-wide aggregation in [PR #79](https://github.com/yuokada/quarkus-fluency-fluentd/pull/79), for [issue #77](https://github.com/yuokada/quarkus-fluency-fluentd/issues/77).


## Project Structure

```
quarkus-fluency-fluentd-parent
├── runtime/            # CDI beans and config — the artifact users depend on
├── deployment/         # Build-time processors (@BuildStep) for augmentation
└── integration-tests/  # Quarkus application exercising the extension
```

## Releasing

This project releases to Maven Central from GitHub Actions. The `release` Maven profile is intended for the GitHub Actions workflow in `.github/workflows/maven-central-publish.yml`, not for routine local use.

### Release prerequisites

Before starting a release, make sure the following are configured:

- Push access to this GitHub repository
- GitHub Actions secrets for Maven Central publishing (`CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`)
- A clean working tree (`git status`)

### Release steps

1. Verify the branch state and test suite:

   ```bash
   git status
   ./mvnw verify
   ```

2. Bump project versions and create the release tag. This project uses `maven-release-plugin` for version/tag management:

   ```bash
   ./mvnw release:clean release:prepare
   ```

   What this does:

   - updates the root, `runtime`, `deployment`, and `integration-tests` module versions
   - creates a Git tag in the form `vX.Y.Z`
   - updates the repository to the next development version

3. Push the release commit and tag to GitHub:

   ```bash
   git push origin master
   git push origin vX.Y.Z
   ```

4. GitHub Actions publishes the package to Maven Central:

   - the workflow `.github/workflows/maven-central-publish.yml` runs on tag push
   - that workflow activates `-Prelease`
   - the `release` profile performs source/javadoc generation, GPG signing, and Central publishing

5. Confirm the release was published:

   - check the generated GitHub tag/release commit
   - verify the new version appears in Maven Central

If you run `-Prelease` locally, you must provide the same GPG key and Maven Central credentials that GitHub Actions injects. Otherwise artifact signing or publishing will fail.

## License

MIT License
