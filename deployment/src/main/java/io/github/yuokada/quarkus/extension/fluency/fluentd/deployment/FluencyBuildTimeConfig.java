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
     * <p>Defaults to {@code false} (opt-in). Set to {@code true} to register a {@code @Readiness}
     * check named {@code "fluentd"} at {@code /q/health/ready}. The check is only available when
     * {@code quarkus-smallrye-health} is on the classpath; setting this to {@code true} without
     * that dependency has no effect.
     */
    @WithName("health.enabled")
    @WithDefault("false")
    boolean healthEnabled();
}
