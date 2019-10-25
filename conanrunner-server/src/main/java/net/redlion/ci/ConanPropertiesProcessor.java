package net.redlion.ci;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConanPropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(@NotNull final Map<String, String> properties) {
        final List<InvalidProperty> invalidParams = new ArrayList<>();
        this.removeEmptyParameters(properties);
        if (StringUtil.isEmptyOrSpaces(properties.getOrDefault(ConanConstants.CONAN_USER_KEY, null))) {
            invalidParams.add(new InvalidProperty(ConanConstants.CONAN_USER_KEY, "Conan user is required"));
        }
        if (StringUtil.isEmptyOrSpaces(properties.getOrDefault(ConanConstants.CONAN_CHANNEL_KEY, null))) {
            invalidParams.add(new InvalidProperty(ConanConstants.CONAN_CHANNEL_KEY, "Conan channel is required"));
        }
        return invalidParams;
    }

    private void removeEmptyParameters(@NotNull final Map<String, String> properties) {
        properties.entrySet().removeIf(e -> StringUtil.isEmptyOrSpaces(e.getValue()));
    }
}
