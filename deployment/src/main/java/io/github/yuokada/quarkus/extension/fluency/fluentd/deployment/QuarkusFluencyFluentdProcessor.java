package io.github.yuokada.quarkus.extension.fluency.fluentd.deployment;

import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.FluencyClient;
import io.github.yuokada.quarkus.extension.fluency.fluentd.runtime.ValidatingFluencyClient;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

class QuarkusFluencyFluentdProcessor {

    private static final String FEATURE = "quarkus-fluency-fluentd";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(FluencyClient.class)
                .addBeanClass(ValidatingFluencyClient.class)
                .setUnremovable()
                .build();
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        // Fluency uses reflection for MessagePack serialization in native image
        reflectiveClass.produce(ReflectiveClassBuildItem.serializationClass(
                "org.komamitsu.fluency.fluentd.FluencyBuilderForFluentd",
                "org.komamitsu.fluency.Fluency",
                "org.komamitsu.fluency.recordformat.FluentdRecordFormatter"
        ));
    }
}
