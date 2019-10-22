package net.redlion.ci;

import jetbrains.buildServer.serverSide.RunTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class AppServer {
    public AppServer(@NotNull final RunTypeRegistry runTypeRegistry,
                     @NotNull final ConanRunnerRunType conanBuildRunner) {
        runTypeRegistry.registerRunType(conanBuildRunner);
    }
}
