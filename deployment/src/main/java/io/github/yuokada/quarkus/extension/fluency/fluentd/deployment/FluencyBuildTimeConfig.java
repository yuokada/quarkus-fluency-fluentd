package io.github.yuokada.quarkus.extension.fluency.fluentd.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.fluency")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface FluencyBuildTimeConfig {

    /**
     * Whether a health check is published in case the smallrye-health extension is present.
     *
     * <p>When {@code true} (the default), a {@code @Readiness} check named {@code "fluentd"} is
     * registered and reports {@code UP} when the Fluency client has successfully connected to
     * Fluentd.
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean healthEnabled();
}
