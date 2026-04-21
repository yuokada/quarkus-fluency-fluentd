package io.github.yuokada.quarkus.extension.fluency.fluentd.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * SmallRye Health readiness check for Fluentd connectivity.
 *
 * <p>This check is only activated when {@code quarkus-smallrye-health} is on the classpath (via
 * the {@code HealthBuildItem} produced in the deployment module). It reports {@code UP} when the
 * {@link FluencyClient} successfully initialized a connection to Fluentd, and {@code DOWN}
 * otherwise (including when {@code quarkus.fluency.enabled=false}).
 */
@Readiness
@ApplicationScoped
public class FluencyHealthCheck implements HealthCheck {

    private final FluencyClient fluencyClient;

    @Inject
    public FluencyHealthCheck(FluencyClient fluencyClient) {
        this.fluencyClient = fluencyClient;
    }

    @Override
    public HealthCheckResponse call() {
        if (fluencyClient.isAvailable()) {
            return HealthCheckResponse.up("fluentd");
        }
        return HealthCheckResponse.down("fluentd");
    }
}
