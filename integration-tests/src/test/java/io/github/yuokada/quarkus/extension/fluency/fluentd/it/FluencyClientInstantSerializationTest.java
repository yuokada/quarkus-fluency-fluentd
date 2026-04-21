package io.github.yuokada.quarkus.extension.fluency.fluentd.it;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.FluencyClient;

/**
 * Snapshot of how {@link FluencyClient} handles {@link Instant} values inside an emit payload.
 *
 * <p>Fluency 2.7.4 encodes {@code Map<String, Object>} payloads with msgpack-jackson. Neither
 * msgpack-jackson nor Fluency register {@code jackson-datatype-jsr310} by default, so
 * {@code java.time.Instant} is not a natively supported payload value in this configuration.
 *
 * <p>This test pins the three relevant outcomes:
 *
 * <ul>
 *   <li>{@code Instant#toEpochMilli()} (long) — serializable.
 *   <li>{@code Instant#toString()} (ISO-8601 String) — serializable.
 *   <li>Raw {@code Instant} value — serialization fails; because {@link FluencyClient#emit}
 *       only catches {@link java.io.IOException}, any msgpack/Jackson {@link RuntimeException}
 *       propagates to the caller.
 * </ul>
 *
 * <p>If a Fluency upgrade or a future change registers {@code JavaTimeModule} on the internal
 * {@code ObjectMapper}, the raw-Instant assertion below should be relaxed to expect {@code true}.
 */
@QuarkusTest
@QuarkusTestResource(FluentdTestResource.class)
public class FluencyClientInstantSerializationTest {

    private static final Instant FIXED = Instant.parse("2026-04-21T06:43:00Z");

    @Inject FluencyClient fluencyClient;

    @Test
    public void emitAcceptsInstantAsEpochMillis() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "instant-millis");
        data.put("timestamp_ms", FIXED.toEpochMilli());

        Assertions.assertTrue(
                fluencyClient.emit("it.instant.millis", data),
                "long epoch millis must always serialize");
    }

    @Test
    public void emitAcceptsInstantAsIsoString() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "instant-iso");
        data.put("timestamp_iso", FIXED.toString());

        Assertions.assertTrue(
                fluencyClient.emit("it.instant.iso", data),
                "ISO-8601 String must always serialize");
    }

    @Test
    public void emitWithRawInstantValuePropagatesUncheckedException() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", "instant-raw");
        data.put("timestamp", FIXED);

        // Snapshot (fluency 2.7.4): raw Instant is not a supported msgpack value and Fluency's
        // Jackson ObjectMapper has no JavaTimeModule. FluencyClient.emit() only catches
        // IOException, so an unchecked serialization error surfaces to the caller.
        Assertions.assertThrows(
                RuntimeException.class,
                () -> fluencyClient.emit("it.instant.raw", data),
                "raw Instant must fail msgpack serialization until JavaTimeModule is wired in");
    }
}
