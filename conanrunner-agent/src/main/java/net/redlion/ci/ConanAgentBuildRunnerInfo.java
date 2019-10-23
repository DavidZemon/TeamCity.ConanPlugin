package net.redlion.ci;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConanAgentBuildRunnerInfo implements AgentBuildRunnerInfo {
    @NotNull
    public String getType() {
        return ConanConstants.RLC_CONAN_RUNNER_RUN_TYPE;
    }

    public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
        boolean canRun;
        final Map<String, String> configParams = agentConfiguration.getConfigurationParameters();
        if (!StringUtil.isEmptyOrSpaces(configParams.getOrDefault(ConanConstants.CONAN_DOCKER_IMAGE_NAME_KEY, null))) {
            canRun = this.executableIsValid("docker");
        } else if (!StringUtil.isEmptyOrSpaces(configParams.getOrDefault(ConanConstants.CONAN_COMMAND_KEY, null))) {
            canRun = this.executableIsValid(configParams.get(ConanConstants.CONAN_COMMAND_KEY).trim());
        } else {
            canRun = this.executableIsValid("conan");
        }
        return canRun;
    }

    public boolean executableIsValid(@NotNull final String executablePath) {
        final File executable = new File(executablePath);
        if (FileUtil.isAbsolute(executablePath)) {
            return executable.canExecute();
        } else {
            return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                    .map(Paths::get)
                    .anyMatch(path -> Files.isExecutable(path.resolve(executable.toPath())));
        }
    }
}
