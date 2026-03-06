package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

import java.util.Map;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.ValidatingFluencyClient;
import io.quarkus.test.QuarkusUnitTest;

public class QuarkusFluencyFluentdTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Inject
    ValidatingFluencyClient validatingClient;

    @Test
    public void testValidatingClientIsInjectable() {
        Assertions.assertNotNull(validatingClient);
    }

    @Test
    public void testNullTagThrows() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validatingClient.emit(null, Map.of("key", "value")));
    }

    @Test
    public void testBlankTagThrows() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validatingClient.emit("   ", Map.of("key", "value")));
    }

    @Test
    public void testInvalidTagFormatThrows() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validatingClient.emit(".start", Map.of("key", "value")));
    }

    @Test
    public void testNullDataThrows() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validatingClient.emit("myapp.events", null));
    }

    @Test
    public void testEmptyDataThrows() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> validatingClient.emit("myapp.events", Map.of()));
    }

    @Test
    public void testValidTagAndDataReturnsFalseWhenNotConnected() {
        // Fluentd is not running in unit tests, so emit returns false but does not throw
        boolean result = validatingClient.emit("myapp.events.user", Map.of("userId", "123"));
        Assertions.assertFalse(result);
    }
}
