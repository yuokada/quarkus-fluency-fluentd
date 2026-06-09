package io.github.yuokada.quarkus.extension.fluency.fluentd.runtime;

import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * SmallRye Health readiness check for Fluentd connectivity.
 *
 * <p>This check is only registered when {@code quarkus-smallrye-health} is on the classpath
 * <em>and</em> {@code quarkus.fluency.health.enabled=true} (via the {@code HealthBuildItem}
 * produced in the deployment module). The CDI bean is not annotated {@code @ApplicationScoped}
 * so SmallRye Health cannot auto-discover it; registration is controlled entirely by the
 * deployment-time {@code HealthBuildItem}.
 *
 * <p>When {@code quarkus.fluency.enabled=false} the check reports {@code UP} because Fluentd
 * forwarding was intentionally disabled and should not block pod readiness.
 */
@Readiness
public class FluencyHealthCheck implements HealthCheck {

    private final FluencyConfig fluencyConfig;
    private final FluencyClient fluencyClient;

    @Inject
    public FluencyHealthCheck(FluencyConfig fluencyConfig, FluencyClient fluencyClient) {
        this.fluencyConfig = fluencyConfig;
        this.fluencyClient = fluencyClient;
    }

    @Override
    public HealthCheckResponse call() {
        if (!fluencyConfig.enabled()) {
            return HealthCheckResponse.up("fluentd");
        }
        if (fluencyClient.isAvailable()) {
            return HealthCheckResponse.up("fluentd");
        }
        return HealthCheckResponse.down("fluentd");
    }
}
