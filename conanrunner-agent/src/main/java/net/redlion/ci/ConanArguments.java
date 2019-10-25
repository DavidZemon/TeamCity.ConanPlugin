package net.redlion.ci;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConanArguments {
    @Nullable
    private Map<String, Object> settings;
    @Nullable
    private Map<String, Object> options;
}
