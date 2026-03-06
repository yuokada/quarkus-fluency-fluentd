package io.github.yuokada.quarkus.extension.fluency.fluentd.runtime;

import java.io.IOException;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.komamitsu.fluency.Fluency;
import org.komamitsu.fluency.fluentd.FluencyBuilderForFluentd;

/**
 * CDI bean that manages the lifecycle of a {@link Fluency} client.
 * <p>
 * Connection failures at startup are handled gracefully — the bean remains
 * available but silently drops events until Fluentd becomes reachable.
 */
@ApplicationScoped
public class FluencyClient {

    private static final Logger log = Logger.getLogger(FluencyClient.class);

    @Inject
    FluencyConfig config;

    private Fluency fluency;

    @PostConstruct
    void init() {
        if (!config.enabled()) {
            log.info("Fluency client is disabled (quarkus.fluency.enabled=false)");
            return;
        }
        try {
            FluencyBuilderForFluentd builder = new FluencyBuilderForFluentd();
            builder.setSenderMaxRetryCount(config.senderMaxRetryCount());
            builder.setBufferChunkInitialSize(config.bufferChunkInitialSize());
            builder.setBufferChunkRetentionTimeMillis(config.bufferChunkRetentionTimeMillis());
            fluency = builder.build(config.host(), config.port());
            log.infof("Fluency client initialized — target: %s:%d", config.host(), config.port());
        } catch (Exception e) {
            log.warnf("Failed to initialize Fluency client (%s). Log forwarding disabled.", e.getMessage());
        }
    }

    /**
     * Emits a record to Fluentd.
     *
     * @param tag  the Fluentd tag
     * @param data the record fields
     * @return {@code true} if the record was accepted into the buffer
     */
    public boolean emit(String tag, Map<String, Object> data) {
        if (fluency == null) {
            return false;
        }
        try {
            fluency.emit(tag, data);
            return true;
        } catch (IOException e) {
            log.debugf("Failed to emit record to Fluentd (tag=%s): %s", tag, e.getMessage());
            return false;
        }
    }

    /**
     * Returns whether the client successfully connected to Fluentd.
     */
    public boolean isAvailable() {
        return fluency != null;
    }

    @PreDestroy
    void destroy() {
        if (fluency != null) {
            try {
                fluency.close();
                log.info("Fluency client closed");
            } catch (Exception e) {
                log.warnf("Error closing Fluency client: %s", e.getMessage());
            }
        }
    }
}
