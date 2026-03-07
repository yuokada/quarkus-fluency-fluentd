package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

import java.util.Map;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import io.quarkus.test.QuarkusUnitTest;

import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.ValidatingFluencyClient;

public class QuarkusFluencyFluentdTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest =
            new QuarkusUnitTest().setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject ValidatingFluencyClient validatingClient;

    @Test
    public void testValidatingClientIsInjectable() {
        Assertions.assertNotNull(validatingClient);
    }

    @Test
    public void testNullTagThrows() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> validatingClient.emit(null, Map.of("key", "value")));
    }

    @Test
    public void testBlankTagThrows() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> validatingClient.emit("   ", Map.of("key", "value")));
    }

    @Test
    public void testInvalidTagFormatThrows() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> validatingClient.emit(".start", Map.of("key", "value")));
    }

    @Test
    public void testNullDataThrows() {
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> validatingClient.emit("myapp.events", null));
    }

    @Test
    public void testEmptyDataThrows() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> validatingClient.emit("myapp.events", Map.of()));
    }

    @Test
    public void testValidTagAndDataDoesNotThrow() {
        // Valid arguments must not raise a validation exception regardless of Fluentd availability
        Assertions.assertDoesNotThrow(
                () -> validatingClient.emit("myapp.events.user", Map.of("userId", "123")));
    }
}

class FluencyConfigValidationTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.host", " ")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            hasCause(t, IllegalStateException.class),
                                            "Expected IllegalStateException in cause chain, got: "
                                                    + t));

    @Test
    public void testBlankHostFailsStartup() {
        Assertions.fail("Application should not have started with blank host");
    }

    static boolean hasCause(Throwable t, Class<?> type) {
        while (t != null) {
            if (type.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}

class FluencyConfigPortValidationTest {

    @RegisterExtension
    static final QuarkusUnitTest zeroPortTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.port", "0")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            FluencyConfigValidationTest.hasCause(
                                                    t, IllegalStateException.class),
                                            "Expected IllegalStateException for port=0, got: "
                                                    + t));

    @Test
    public void testZeroPortFailsStartup() {
        Assertions.fail("Application should not have started with port=0");
    }
}

class FluencyConfigBufferSizeValidationTest {

    @RegisterExtension
    static final QuarkusUnitTest negativeBufferTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.buffer-chunk-initial-size", "-1")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            FluencyConfigValidationTest.hasCause(
                                                    t, IllegalStateException.class),
                                            "Expected IllegalStateException for negative bufferChunkInitialSize, got: "
                                                    + t));

    @Test
    public void testNegativeBufferSizeFailsStartup() {
        Assertions.fail("Application should not have started with negative bufferChunkInitialSize");
    }
}
