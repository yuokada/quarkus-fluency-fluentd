package io.github.yuokada.quarkus.extension.fluency.fluentd.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.fluency")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface FluencyConfig {

    /** Fluentd host. */
    @WithDefault("localhost")
    String host();

    /** Fluentd port. */
    @WithDefault("24224")
    int port();

    /** Whether to enable log forwarding to Fluentd. */
    @WithDefault("true")
    boolean enabled();

    /** Max retry count for sending. */
    @WithDefault("4")
    int senderMaxRetryCount();

    /**
     * Buffer chunk initial size (bytes). Default is 1 MiB; must be less than
     * {@code quarkus.fluency.buffer-chunk-retention-size}.
     */
    @WithDefault("1048576")
    int bufferChunkInitialSize();

    /**
     * Buffer chunk retention size (bytes). Default is 4 MiB; must be greater than
     * {@code quarkus.fluency.buffer-chunk-initial-size}.
     */
    @WithDefault("4194304")
    int bufferChunkRetentionSize();

    /** Buffer chunk retention time (ms). */
    @WithDefault("1000")
    int bufferChunkRetentionTimeMillis();
}
