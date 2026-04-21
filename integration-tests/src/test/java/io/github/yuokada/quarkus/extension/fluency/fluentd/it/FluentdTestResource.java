package io.github.yuokada.quarkus.extension.fluency.fluentd.it;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class FluentdTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger log = Logger.getLogger(FluentdTestResource.class);
    private static final String IMAGE = "fluent/fluentd:v1.19-2";
    private static final int FORWARD_PORT = 24224;

    private GenericContainer<?> fluentd;

    @Override
    public Map<String, String> start() {
        fluentd =
                new GenericContainer<>(DockerImageName.parse(IMAGE))
                        .withExposedPorts(FORWARD_PORT)
                        .withCopyFileToContainer(
                                MountableFile.forHostPath(
                                        Path.of("src/test/resources/fluent.conf")),
                                "/fluentd/etc/fluent.conf")
                        .waitingFor(Wait.forLogMessage(".*fluentd worker is now running.*\\n", 1))
                        .withStartupTimeout(Duration.ofSeconds(90));

        fluentd.start();

        String host = fluentd.getHost();
        int port = fluentd.getMappedPort(FORWARD_PORT);
        log.infof("Fluentd container started at %s:%d", host, port);

        return Map.of("quarkus.fluency.host", host, "quarkus.fluency.port", String.valueOf(port));
    }

    @Override
    public void stop() {
        if (fluentd != null && fluentd.isRunning()) {
            fluentd.stop();
        }
    }
}
