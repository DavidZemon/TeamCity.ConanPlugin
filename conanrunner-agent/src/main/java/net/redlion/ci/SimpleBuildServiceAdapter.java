package net.redlion.ci;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.runner.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is a copy of any and all methods required from {@link jetbrains.buildServer.agent.runner.BuildServiceAdapter}.
 * The initialize() method on that class is marked as unstable and not to be used directly, so I don't know how to use
 * it correctly with the MultiCommandBuildSession. Therefore, I'll just copy all of its code and create my own, stable
 * version. This should keep the plugin from randomly breaking too terribly soon.
 */
public abstract class SimpleBuildServiceAdapter implements MultiCommandBuildSession {
    @NotNull
    private final BuildRunnerContext runnerContext;
    @NotNull
    private final String             flowId = FlowGenerator.generateNewFlow();

    public SimpleBuildServiceAdapter(@NotNull final BuildRunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    @NotNull
    protected final AgentRunningBuild getBuild() {
        return this.runnerContext.getBuild();
    }

    @NotNull
    protected final BuildRunnerContext getRunnerContext() {
        return this.runnerContext;
    }

    @NotNull
    protected final Map<String, String> getEnvironmentVariables() {
        return this.getRunnerContext().getBuildParameters().getEnvironmentVariables();
    }

    @NotNull
    protected final Map<String, String> getRunnerParameters() {
        return this.getRunnerContext().getRunnerParameters();
    }

    @NotNull
    protected final File getCheckoutDirectory() {
        return this.getBuild().getCheckoutDirectory();
    }

    @NotNull
    protected final File getWorkingDirectory() {
        return this.getRunnerContext().getWorkingDirectory();
    }

    @NotNull
    protected final ProgramCommandLine createProgramCommandline(@NotNull final String file,
                                                                @NotNull final List<String> args) {
        return new SimpleProgramCommandLine(this.getRunnerContext(), file, args);
    }

    @NotNull
    public final File getBuildTempDirectory() {
        return this.getBuild().getBuildTempDirectory();
    }

    @NotNull
    public final File getAgentTempDirectory() {
        return this.getBuild().getAgentTempDirectory();
    }

    @NotNull
    public final BuildAgentConfiguration getAgentConfiguration() {
        return this.getBuild().getAgentConfiguration();
    }

    @NotNull
    public List<ProcessListener> getListeners() {
        return Collections.singletonList(new LoggingProcessListener(this.getLogger()));
    }

    @NotNull
    public TerminationAction interrupt() {
        return TerminationAction.KILL_PROCESS_TREE;
    }

    @NotNull
    public final BuildProgressLogger getLogger() {
        return this.runnerContext.getBuild().getBuildLogger().getFlowLogger(this.flowId);
    }

    public boolean isCommandLineLoggingEnabled() {
        return true;
    }
}
