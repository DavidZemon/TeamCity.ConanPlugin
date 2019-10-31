package net.redlion.ci;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.runner.CommandExecution;
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static jetbrains.buildServer.messages.DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP;
import static net.redlion.ci.ConanConstants.*;

public class ConanBuildSession extends SimpleBuildServiceAdapter implements MultiCommandBuildSession {
    @NotNull
    private static final Gson GSON            = new Gson();
    @NotNull
    private static final Type CONAN_OPTS_TYPE = new TypeToken<ArrayList<ConanArguments>>() {
    }.getType();

    @NotNull
    private final LinkedList<Activity>                activitiesToRun = new LinkedList<>();
    @NotNull
    private final LinkedList<CommandExecutionAdapter> sentCommands    = new LinkedList<>();
    @NotNull
    private final Map<String, String>                 params;

    public ConanBuildSession(@NotNull final BuildRunnerContext runnerContext) {
        super(runnerContext);
        this.params = this.getRunnerParameters();
    }

    @Override
    public void sessionStarted() throws RunBuildException {
        if (this.params.containsKey(CONAN_DOCKER_IMAGE_NAME_KEY)
            && this.params.containsKey(CONAN_DOCKER_PULL_ENABLED_KEY)) {
            this.addDockerPullCommand();
        }

        final String conanOptionsPath = this.params.getOrDefault(CONAN_OPTIONS_PATH_KEY,
            CONAN_DEFAULT_OPTIONS_FILE_NAME);

        final Path absConanOptsPath = this.getWorkingDirectory().toPath().resolve(conanOptionsPath).toAbsolutePath();
        if (Files.exists(absConanOptsPath)) {
            this.addCommandsFromConanOptionsFile(absConanOptsPath);
        } else if (this.params.containsKey(CONAN_OPTIONS_PATH_KEY)) {
            throw new RunBuildException(String.format("Conan options file was provided (%s) but not found at `%s`.",
                conanOptionsPath, absConanOptsPath));
        } else {
            this.activitiesToRun.add(this.appendConanCreateActivity(Collections.emptyList()));
        }
    }

    private String toCliString(@NotNull final Map.Entry<String, Object> entry) throws RunBuildException {
        final String key = entry.getKey();
        if (null == entry.getValue()) {
            throw new RunBuildException(String.format("Null values are not allowed in Conan options files (key=%s).",
                key));
        }

        final Object value = entry.getValue();
        final String stringValue;
        if (value instanceof Boolean) {
            stringValue = (Boolean) value ? "True" : "False";
        } else {
            stringValue = value.toString();
        }

        return String.format("%s=%s", key, stringValue);
    }

    @Nullable
    @Override
    public BuildFinishedStatus sessionFinished() {
        final CommandExecutionAdapter lastCommand = this.sentCommands.getLast();
        //noinspection ConstantConditions
        final int exitCode = lastCommand.getExitCode();
        if (0 == exitCode) {
            return BuildFinishedStatus.FINISHED_SUCCESS;
        } else if (lastCommand.isInterrupted()) {
            return BuildFinishedStatus.INTERRUPTED;
        } else {
            return BuildFinishedStatus.FINISHED_FAILED;
        }
    }

    @Nullable
    @Override
    public CommandExecution getNextCommand() {
        // Check for errors in the previous command
        if (!this.sentCommands.isEmpty()) {
            final CommandExecutionAdapter previousCommand = this.sentCommands.getLast();
            if (null != previousCommand.getExitCode() && 0 != previousCommand.getExitCode()) {
                return null;
            }
        }

        if (this.activitiesToRun.isEmpty()) {
            return null;
        } else {
            final CommandExecutionAdapter next = new CommandExecutionAdapter(this, this.activitiesToRun.pop());
            this.sentCommands.add(next);
            return next;
        }
    }

    private void addCommandsFromConanOptionsFile(@NotNull final Path absConanOptsPath) throws RunBuildException {
        final List<ConanArguments> conanArgumentsList = this.readConanOptions(absConanOptsPath);

        if (conanArgumentsList.isEmpty()) {
            throw new RunBuildException(String.format(
                "Conan options file (%s) exists but contains an empty array. No builds to run.", absConanOptsPath
            ));
        }

        for (final ConanArguments conanArguments : conanArgumentsList) {
            final List<String> extraArgs = new ArrayList<>();
            if (null != conanArguments.getSettings()) {
                for (final Map.Entry<String, Object> entry : conanArguments.getSettings().entrySet()) {
                    extraArgs.add("--settings");
                    extraArgs.add(this.toCliString(entry));
                }
            }
            if (null != conanArguments.getOptions()) {
                for (final Map.Entry<String, Object> entry : conanArguments.getOptions().entrySet()) {
                    extraArgs.add("--options");
                    extraArgs.add(this.toCliString(entry));
                }
            }
            this.activitiesToRun.add(this.appendConanCreateActivity(extraArgs));
        }
    }

    private List<ConanArguments> readConanOptions(final Path absConanOptsPath) throws RunBuildException {
        try (final Reader f = new FileReader(absConanOptsPath.toFile())) {
            return GSON.fromJson(f, CONAN_OPTS_TYPE);
        } catch (final IOException e) {
            throw new RunBuildException(e);
        }
    }

    private void addDockerPullCommand() {
        final List<String> args = Arrays.asList("pull", this.params.get(CONAN_DOCKER_IMAGE_NAME_KEY));
        this.activitiesToRun.add(new Activity("Pull Docker image", "Ensure the Docker image is up-to-date",
            BLOCK_TYPE_BUILD_STEP, this.createProgramCommandline("docker", args)));
    }

    private Activity appendConanCreateActivity(@NotNull final List<String> extraArgs) throws RunBuildException {
        final String checkoutDirectory = this.getCheckoutDirectory().getAbsolutePath();
        final String cwd               = this.getWorkingDirectory().getAbsolutePath();

        final List<String> arguments    = new ArrayList<>();
        final String       command;
        final String       conanCommand = this.params.getOrDefault(CONAN_COMMAND_KEY, "conan");

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

        if (this.params.containsKey(CONAN_APP_NAME_AND_VERSION_KEY)) {
            arguments.add(String.format("%s@%s/%s", this.params.get(CONAN_APP_NAME_AND_VERSION_KEY),
                this.params.get(CONAN_USER_KEY), this.params.get(CONAN_CHANNEL_KEY)));
        } else {
            arguments.add(this.params.get(CONAN_USER_KEY) + "/" + this.params.get(CONAN_CHANNEL_KEY));
        }

        arguments.addAll(extraArgs);

        if (this.params.containsKey(CONAN_EXTRA_CONAN_OPTIONS_KEY)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(
                this.params.get(CONAN_EXTRA_CONAN_OPTIONS_KEY)
            ));
        }

        final String description;
        if (extraArgs.isEmpty()) {
            description = "Default settings and options";
        } else {
            description = StringUtil.join(" ", extraArgs);
        }

        return new Activity("Conan create", description, BLOCK_TYPE_BUILD_STEP,
            this.createProgramCommandline(command, arguments));
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
