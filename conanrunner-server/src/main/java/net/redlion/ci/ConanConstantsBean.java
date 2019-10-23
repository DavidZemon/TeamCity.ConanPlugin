package net.redlion.ci;

import org.jetbrains.annotations.NotNull;

public class ConanConstantsBean {
    @NotNull
    public String getConanCommandKey() {
        return ConanConstants.CONAN_COMMAND_KEY;
    }

    @NotNull
    public String getConanRecipePathKey() {
        return ConanConstants.CONAN_RECIPE_PATH_KEY;
    }

    @NotNull
    public String getConanUserKey() {
        return ConanConstants.CONAN_USER_KEY;
    }

    @NotNull
    public String getConanChannelKey() {
        return ConanConstants.CONAN_CHANNEL_KEY;
    }

    @NotNull
    public String getConanDockerImageNameKey() {
        return ConanConstants.CONAN_DOCKER_IMAGE_NAME_KEY;
    }

    @NotNull
    public String getConanDockerPlatformKey() {
        return ConanConstants.CONAN_DOCKER_PLATFORM_KEY;
    }

    @NotNull
    public String getConanDockerPullEnabledKey() {
        return ConanConstants.CONAN_DOCKER_PULL_ENABLED_KEY;
    }

    @NotNull
    public String getConanDockerParametersKey() {
        return ConanConstants.CONAN_DOCKER_PARAMETERS_KEY;
    }
}
