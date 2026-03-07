package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import io.quarkus.test.QuarkusUnitTest;

public class FluencyConfigValidationTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest =
            new QuarkusUnitTest()
                    .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
                    .overrideConfigKey("quarkus.fluency.host", " ")
                    .assertException(
                            t ->
                                    Assertions.assertTrue(
                                            TestUtils.hasCause(t, IllegalStateException.class),
                                            "Expected IllegalStateException in cause chain, got: "
                                                    + t));

    @Test
    public void testBlankHostFailsStartup() {
        Assertions.fail("Application should not have started with blank host");
    }
}
