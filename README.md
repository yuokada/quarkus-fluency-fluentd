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
    <version>0.0.6-SNAPSHOT</version>
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

# Build everything including integration tests (requires no live Fluentd)
./mvnw verify -pl integration-tests -DskipITs=false

# Skip tests
./mvnw install -DskipTests
```

## Code Style

This project uses [spotless-maven-plugin](https://github.com/diffplug/spotless/blob/main/plugin-maven/README.md) with [google-java-format](https://github.com/google/google-java-format) to enforce consistent Java formatting.

```bash
# Check formatting (runs automatically during verify)
./mvnw spotless:check

# Apply formatting
./mvnw spotless:apply
```

The `spotless:check` goal is bound to the `verify` phase, so CI will fail on unformatted code. Run `spotless:apply` before committing.

## Project Structure

```
quarkus-fluency-fluentd-parent
├── runtime/            # CDI beans and config — the artifact users depend on
├── deployment/         # Build-time processors (@BuildStep) for augmentation
└── integration-tests/  # Quarkus application exercising the extension
```

## Releasing

This project releases to Maven Central with `maven-release-plugin`, the `release` Maven profile, GPG signing, and the Sonatype Central Portal publisher.

### Release prerequisites

Before starting a release, make sure the following are configured on your machine:

- Push access to this GitHub repository
- A GPG key available to Maven for artifact signing
- A `central` server entry in `~/.m2/settings.xml` for Sonatype Central Portal publishing
- A clean working tree (`git status`)

Example `settings.xml` server entry:

```xml
<servers>
    <server>
        <id>central</id>
        <username>${env.CENTRAL_USERNAME}</username>
        <password>${env.CENTRAL_PASSWORD}</password>
    </server>
</servers>
```

### Release steps

1. Verify the branch state and test suite:

   ```bash
   git status
   ./mvnw verify
   ```

2. Run the Maven release:

   ```bash
   ./mvnw release:clean release:prepare release:perform -Darguments="-DskipTests"
   ```

   What this does:

   - updates the root, `runtime`, and `deployment` module versions
   - creates and pushes a Git tag in the form `vX.Y.Z`
   - runs the `release` profile during `release:perform`
   - signs and publishes artifacts to Maven Central

3. Confirm the release was published:

   - check the generated GitHub tag/release commit
   - verify the new version appears in Maven Central

### Post-release manual step

`integration-tests` is intentionally excluded from the parent `<modules>` so it is not published to Maven Central. Because of that, the release plugin does **not** update its parent version automatically.

After each release, manually update the `<parent><version>` in `integration-tests/pom.xml` to the next development version so it matches the root `pom.xml` again:

```xml
<parent>
    <groupId>io.github.yuokada.quarkus.extension</groupId>
    <artifactId>quarkus-fluency-fluentd-parent</artifactId>
    <version>X.Y.Z-SNAPSHOT</version>
</parent>
```

Then commit that follow-up change:

```bash
git add integration-tests/pom.xml
git commit -m "chore: align integration-tests parent version after release"
git push
```

## License

MIT License
