package net.redlion.ci;

import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Activity {
    @NotNull
    private final String name;
    @NotNull
    private final String description;
    @NotNull
    private final String type;
    @NotNull
    private final ProgramCommandLine commandLine;
}
