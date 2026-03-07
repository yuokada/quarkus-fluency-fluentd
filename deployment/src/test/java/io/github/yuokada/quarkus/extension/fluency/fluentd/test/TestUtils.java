package io.github.yuokada.quarkus.extension.fluency.fluentd.test;

class TestUtils {

    static boolean hasCause(Throwable t, Class<?> type) {
        while (t != null) {
            if (type.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private TestUtils() {}
}
