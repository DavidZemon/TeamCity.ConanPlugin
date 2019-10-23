package net.redlion.ci;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession;
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory;
import org.jetbrains.annotations.NotNull;

public class ConanCLBSFactory implements MultiCommandBuildSessionFactory {
    @NotNull
    private final ConanAgentBuildRunnerInfo agentBuildRunnerInfo;

    public ConanCLBSFactory(@NotNull final ConanAgentBuildRunnerInfo agentBuildRunnerInfo) {
        this.agentBuildRunnerInfo = agentBuildRunnerInfo;
    }

    @NotNull
    @Override
    public MultiCommandBuildSession createSession(@NotNull final BuildRunnerContext runnerContext) {
        return new ConanBuildService(runnerContext);
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return this.agentBuildRunnerInfo;
    }
}
