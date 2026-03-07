package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import io.quarkus.test.QuarkusUnitTest;

public class FluencyConfigPortValidationTest {

    @RegisterExtension
    static final QuarkusUnitTest zeroPortTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.port", "0")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            TestUtils.hasCause(t, IllegalStateException.class),
                                            "Expected IllegalStateException for port=0, got: "
                                                    + t));

    @Test
    public void testZeroPortFailsStartup() {
        Assertions.fail("Application should not have started with port=0");
    }
}
