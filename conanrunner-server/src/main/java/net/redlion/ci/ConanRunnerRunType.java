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
    final private PropertiesProcessor conanPropertiesProcessor;

    public ConanRunnerRunType(@NotNull final RunTypeRegistry runTypeRegistry,
                              @NotNull final PluginDescriptor pluginDescriptor,
                              @NotNull final PropertiesProcessor conanPropertiesProcessor) {
        this.pluginDescriptor = pluginDescriptor;
        this.conanPropertiesProcessor = conanPropertiesProcessor;

        runTypeRegistry.registerRunType(this);
    }

    @NotNull
    @Override
    public String getType() {
        return "RLC_CONAN_BUILD_RUNNER";
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
        return this.conanPropertiesProcessor;
    }

    @Nullable
    @Override
    public String getEditRunnerParamsJspFilePath() {
        return this.pluginDescriptor.getPluginResourcesPath("example.jsp");
    }

    @Nullable
    @Override
    public String getViewRunnerParamsJspFilePath() {
        return this.pluginDescriptor.getPluginResourcesPath("example.jsp");
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
