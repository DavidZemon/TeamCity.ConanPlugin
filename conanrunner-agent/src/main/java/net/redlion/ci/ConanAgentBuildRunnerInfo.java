package net.redlion.ci;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import org.jetbrains.annotations.NotNull;

public class ConanAgentBuildRunnerInfo implements AgentBuildRunnerInfo {
    @NotNull
    public String getType() {
        return ConanConstants.RLC_CONAN_RUNNER_RUN_TYPE;
    }

    public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
        return true; // TODO: Run some basic checks
    }
}
