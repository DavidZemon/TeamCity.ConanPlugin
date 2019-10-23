package net.redlion.ci;

import jetbrains.buildServer.agent.runner.CommandExecution;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.agent.runner.TerminationAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CommandExecutionAdapter implements CommandExecution {
    @NotNull
    private final SimpleBuildServiceAdapter buildService;
    @NotNull
    private final Activity activity;

    public CommandExecutionAdapter(@NotNull final SimpleBuildServiceAdapter buildService,
                                   @NotNull final Activity activity) {
        this.buildService = buildService;
        this.activity = activity;
    }


    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() {
        return this.activity.getCommandLine();
    }

    @Override
    public void beforeProcessStarted() {
        this.buildService.getBuild().getBuildLogger().activityStarted(
                this.activity.getName(), this.activity.getDescription(), this.activity.getType()
        );
    }

    @NotNull
    @Override
    public TerminationAction interruptRequested() {
        return this.buildService.interrupt();
    }

    @Override
    public boolean isCommandLineLoggingEnabled() {
        return this.buildService.isCommandLineLoggingEnabled();
    }

    @Override
    public void onStandardOutput(@NotNull final String text) {
        this.buildService.getListeners().forEach(l -> l.onStandardOutput(text));
    }

    @Override
    public void onErrorOutput(@NotNull final String text) {
        this.buildService.getListeners().forEach(l -> l.onErrorOutput(text));
    }

    @Override
    public void processStarted(@NotNull final String programCommandLine, @NotNull final File workingDirectory) {
        this.buildService.getListeners().forEach(l -> l.processStarted(programCommandLine, workingDirectory));
    }

    @Override
    public void processFinished(final int exitCode) {
        this.buildService.getListeners().forEach(l -> l.processFinished(exitCode));
        this.buildService.getBuild().getBuildLogger().activityFinished(
                this.activity.getName(), this.activity.getType()
        );
    }
}
