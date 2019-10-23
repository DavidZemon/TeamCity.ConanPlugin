package net.redlion.ci;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ConanPropertiesProcessor implements PropertiesProcessor {
    @Override
    public Collection<InvalidProperty> process(@NotNull final Map<String, String> properties) {
        this.removeEmptyParameters(properties);
        return Collections.emptyList();
    }

    private void removeEmptyParameters(@NotNull final Map<String, String> properties) {
        properties.entrySet().removeIf(e -> StringUtil.isEmptyOrSpaces(e.getValue()));
    }
}
