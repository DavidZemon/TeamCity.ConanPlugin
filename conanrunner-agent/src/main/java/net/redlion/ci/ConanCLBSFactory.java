package net.redlion.ci;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

public class ConanCLBSFactory implements CommandLineBuildServiceFactory {
    @NotNull
    private final ConanAgentBuildRunnerInfo agentBuildRunnerInfo;

    public ConanCLBSFactory(@NotNull final ConanAgentBuildRunnerInfo agentBuildRunnerInfo) {
        this.agentBuildRunnerInfo = agentBuildRunnerInfo;
    }

    @NotNull
    public CommandLineBuildService createService() {
        return new ConanBuildService();
    }

    @NotNull
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return this.agentBuildRunnerInfo;
    }
}
