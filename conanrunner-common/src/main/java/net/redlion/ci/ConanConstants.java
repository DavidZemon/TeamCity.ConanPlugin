package net.redlion.ci;

public enum ConanConstants {
    ;
    public static final String RLC_CONAN_RUNNER_RUN_TYPE = "RLC_CONAN_BUILD_RUNNER";

    public static final String CONAN_DEFAULT_OPTIONS_FILE_NAME = "conanopts.json";

    // Build property keys
    public static final String CONAN_COMMAND_KEY             = "plugin.conan.command";
    public static final String CONAN_RECIPE_PATH_KEY         = "plugin.conan.recipeFilePath";
    public static final String CONAN_USER_KEY                = "plugin.conan.user";
    public static final String CONAN_CHANNEL_KEY             = "plugin.conan.channel";
    public static final String CONAN_EXTRA_CONAN_OPTIONS_KEY = "plugin.conan.extraConanOptions";
    public static final String CONAN_OPTIONS_PATH_KEY        = "plugin.conan.optionsFilePath";
    public static final String CONAN_DOCKER_IMAGE_NAME_KEY   = "plugin.conan.dockerImageName";
    public static final String CONAN_DOCKER_PLATFORM_KEY     = "plugin.conan.dockerImagePlatform";
    public static final String CONAN_DOCKER_PULL_ENABLED_KEY = "plugin.conan.dockerPullEnabled";
    public static final String CONAN_DOCKER_PARAMETERS_KEY   = "plugin.conan.dockerParameters";
}
