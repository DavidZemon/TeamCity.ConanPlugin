package net.redlion.ci;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConanBuildService extends BuildServiceAdapter {
    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final String checkoutDirectory = this.getCheckoutDirectory().getAbsolutePath();
        final String cwd = this.getWorkingDirectory().getAbsolutePath();

        final Map<String, String> params = this.getRunnerParameters();

        final List<String> arguments = new ArrayList<>();
        final String command;
        final String conanCommand = params.getOrDefault(ConanConstants.CONAN_COMMAND_KEY, "conan");

        final String dockerImageName = params.get(ConanConstants.CONAN_DOCKER_IMAGE_NAME_KEY);
        if (StringUtil.isEmptyOrSpaces(dockerImageName)) {
            command = conanCommand;
        } else {
            command = "docker";

            arguments.addAll(Arrays.asList(
                    "run",
                    "--rm",
                    "--label", "jetbrains.teamcity.buildId=" + params.get("teamcity.build.id"),
                    "--workdir", cwd,
                    "--volume", checkoutDirectory + ":" + checkoutDirectory
            ));

            this.addCommonDockerMounts(arguments);

            arguments.add("--env-file");
            try {
                arguments.add(this.getEnvironmentVariablesFilePath());
            } catch (final IOException e) {
                throw new RunBuildException(e);
            }

            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(params.getOrDefault(
                    ConanConstants.CONAN_DOCKER_PARAMETERS_KEY, ""
            )));

            arguments.add(dockerImageName);

            arguments.add(conanCommand);
        }

        arguments.add("create");
        arguments.add(params.getOrDefault(ConanConstants.CONAN_RECIPE_PATH_KEY, "."));
        arguments.add(params.get(ConanConstants.CONAN_USER_KEY) + "/" + params.get(ConanConstants.CONAN_CHANNEL_KEY));

        return this.createProgramCommandline(command, arguments);
    }

    @NotNull
    private String getEnvironmentVariablesFilePath() throws IOException {
        final Path filePath = Paths.get(this.getBuildTempDirectory().getAbsolutePath(), "docker_env_vars.txt");
        try (final BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (Map.Entry<String, String> entry : this.getEnvironmentVariables().entrySet()) {
                writer.write(String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        return filePath.toString();
    }

    private void addCommonDockerMounts(@NotNull final List<String> arguments) {
        final List<File> writable = Arrays.asList(
                this.getAgentTempDirectory(),
                this.getBuildTempDirectory(),
                this.getAgentConfiguration().getSystemDirectory()
        );
        final List<File> readOnly = Arrays.asList(
                this.getAgentConfiguration().getAgentHomeDirectory(),
                this.getAgentConfiguration().getAgentToolsDirectory(),
                this.getAgentConfiguration().getAgentPluginsDirectory()
        );

        writable.stream().map(File::getAbsolutePath)
                .forEach(f -> arguments.addAll(Arrays.asList("--volume", f + ":" + f)));
        readOnly.stream().map(File::getAbsolutePath)
                .forEach(f -> arguments.addAll(Arrays.asList("--volume", f + ":" + f + ":ro")));
    }
}
