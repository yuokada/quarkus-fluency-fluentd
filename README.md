# Quarkus Fluency Fluentd Extension

A Quarkus extension that integrates [Fluency](https://github.com/komamitsu/fluency) — a high-performance [Fluentd](https://www.fluentd.org/) / [Fluent Bit](https://fluentbit.io/) client for Java — into your Quarkus application as a managed CDI bean.

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
    <version>1.0.0-SNAPSHOT</version>
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

## Configuration

All properties are under the `quarkus.fluency` prefix.

| Property | Default | Description |
|---|---|---|
| `quarkus.fluency.host` | `localhost` | Fluentd host |
| `quarkus.fluency.port` | `24224` | Fluentd TCP port |
| `quarkus.fluency.enabled` | `true` | Set to `false` to disable log forwarding entirely |
| `quarkus.fluency.sender-max-retry-count` | `4` | Max send retry attempts |
| `quarkus.fluency.buffer-chunk-initial-size` | `1048576` | Buffer chunk initial size in bytes (1 MiB); must be less than retention size |
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

## Project Structure

```
quarkus-fluency-fluentd-parent
├── runtime/            # CDI beans and config — the artifact users depend on
├── deployment/         # Build-time processors (@BuildStep) for augmentation
└── integration-tests/  # Quarkus application exercising the extension
```

## License

Apache License 2.0
