package net.redlion.ci;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConanRunnerRunType extends RunType {
    @NotNull
    private final PluginDescriptor pluginDescriptor;
    @NotNull
    final private PropertiesProcessor propertiesProcessor;

    public ConanRunnerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                              @NotNull final PluginDescriptor pluginDescriptor,
                              @NotNull final PropertiesProcessor propertiesProcessor) {
        this.pluginDescriptor = pluginDescriptor;
        this.propertiesProcessor = propertiesProcessor;

        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return ConanConstants.RLC_CONAN_RUNNER_RUN_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Conan";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Build and upload Conan artifacts for each set of options";
    }

    @Nullable
    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return this.propertiesProcessor;
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return this.pluginDescriptor.getPluginResourcesPath("conanrunner.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return null; // TODO
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
