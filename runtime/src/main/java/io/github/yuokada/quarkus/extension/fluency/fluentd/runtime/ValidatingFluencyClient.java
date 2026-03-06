package io.github.yuokada.quarkus.extension.fluency.fluentd.runtime;

import java.util.Map;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * A validating wrapper around {@link FluencyClient} that enforces tag and data
 * constraints before delegating to the underlying client.
 *
 * <p>Throws {@link IllegalArgumentException} on invalid input rather than
 * silently failing or propagating internal library errors.
 */
@ApplicationScoped
public class ValidatingFluencyClient {

    private static final Pattern TAG_PATTERN =
            Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9._-]*");

    @Inject
    FluencyClient delegate;

    public boolean emit(String tag, Map<String, Object> data) {
        validateTag(tag);
        validateData(data);
        return delegate.emit(tag, data);
    }

    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    private void validateTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must not be null or blank");
        }
        if (!TAG_PATTERN.matcher(tag).matches()) {
            throw new IllegalArgumentException(
                    "invalid tag format '" + tag + "': must match " + TAG_PATTERN.pattern());
        }
    }

    private void validateData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("data must not be null or empty");
        }
    }
}
