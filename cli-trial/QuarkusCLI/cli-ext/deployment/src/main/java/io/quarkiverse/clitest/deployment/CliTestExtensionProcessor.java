package io.quarkiverse.clitest.deployment;

import io.quarkiverse.clitest.runtime.CliTestsExtensionServlet;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.undertow.deployment.ServletBuildItem;

class CliTestExtensionProcessor {

    private static final String FEATURE = "cli-test-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    ServletBuildItem createServlet() {
        return ServletBuildItem
                .builder("cli-ext", CliTestsExtensionServlet.class.getName())
                .addMapping("/doit")
                .build();
    }

}
