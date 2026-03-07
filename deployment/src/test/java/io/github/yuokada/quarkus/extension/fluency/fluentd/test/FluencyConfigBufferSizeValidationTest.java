package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import io.quarkus.test.QuarkusUnitTest;

public class FluencyConfigBufferSizeValidationTest {

    static boolean hasCause(Throwable t, Class<?> type) {
        while (t != null) {
            if (type.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    @RegisterExtension
    static final QuarkusUnitTest negativeBufferTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.buffer-chunk-initial-size", "-1")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            hasCause(t, IllegalStateException.class),
                                            "Expected IllegalStateException for negative bufferChunkInitialSize, got: "
                                                    + t));

    @Test
    public void testNegativeBufferSizeFailsStartup() {
        Assertions.fail("Application should not have started with negative bufferChunkInitialSize");
    }
}
