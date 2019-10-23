package net.redlion.ci;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.CommandExecution;
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static jetbrains.buildServer.messages.DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP;
import static net.redlion.ci.ConanConstants.*;

public class ConanBuildService extends SimpleBuildServiceAdapter implements MultiCommandBuildSession {
    @NotNull
    private final LinkedList<Activity> activities = new LinkedList<>();
    @NotNull
    private final Map<String, String> params;

    public ConanBuildService(@NotNull final BuildRunnerContext runnerContext) {
        super(runnerContext);
        this.params = this.getRunnerParameters();
    }

    @Override
    public void sessionStarted() throws RunBuildException {
        if (this.params.containsKey(CONAN_DOCKER_IMAGE_NAME_KEY)
                && this.params.containsKey(CONAN_DOCKER_PULL_ENABLED_KEY)) {
            this.addDockerPullCommand();
        }
        this.addConanCreateCommand();
    }

    private void addDockerPullCommand() {
        final List<String> args = Arrays.asList("pull", this.params.get(CONAN_DOCKER_IMAGE_NAME_KEY));
        this.activities.add(new Activity("Pull Docker image", "Ensure the Docker image is up-to-date",
                BLOCK_TYPE_BUILD_STEP, this.createProgramCommandline("docker", args)));
    }

    private void addConanCreateCommand() throws RunBuildException {
        final String checkoutDirectory = this.getCheckoutDirectory().getAbsolutePath();
        final String cwd = this.getWorkingDirectory().getAbsolutePath();

        final List<String> arguments = new ArrayList<>();
        final String command;
        final String conanCommand = this.params.getOrDefault(CONAN_COMMAND_KEY, "conan");

        if (this.params.containsKey(CONAN_DOCKER_IMAGE_NAME_KEY)) {
            command = "docker";

            arguments.addAll(Arrays.asList(
                    "run",
                    "--rm",
                    "--label", "jetbrains.teamcity.buildId=" + this.params.get("teamcity.build.id"),
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

            if (this.params.containsKey(CONAN_DOCKER_PARAMETERS_KEY)) {
                arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(this.params.get(
                        CONAN_DOCKER_PARAMETERS_KEY
                )));
            }

            arguments.add(this.params.get(CONAN_DOCKER_IMAGE_NAME_KEY));

            arguments.add(conanCommand);
        } else {
            command = conanCommand;
        }

        arguments.add("create");
        arguments.add(this.params.getOrDefault(CONAN_RECIPE_PATH_KEY, "."));
        arguments.add(this.params.get(CONAN_USER_KEY) + "/" + this.params.get(CONAN_CHANNEL_KEY));

        this.activities.add(new Activity("Conan create", "Create the Conan artifact in the local cache",
                BLOCK_TYPE_BUILD_STEP, this.createProgramCommandline(command, arguments)));
    }

    @Nullable
    @Override
    public CommandExecution getNextCommand() {
        if (this.activities.isEmpty()) {
            return null;
        } else {
            return new CommandExecutionAdapter(this, this.activities.pop());
        }
    }

    @Nullable
    @Override
    public BuildFinishedStatus sessionFinished() {
        return null;
    }

    @NotNull
    private String getEnvironmentVariablesFilePath() throws IOException {
        final Path filePath = Paths.get(this.getBuildTempDirectory().getAbsolutePath(), "docker_env_vars.txt");
        try (final BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (final Map.Entry<String, String> entry : this.getEnvironmentVariables().entrySet()) {
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
