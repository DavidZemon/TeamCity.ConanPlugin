TeamCity Conan Plugin
=====================

This plugin provides a TeamCity build configuration runner (a build step) for creating Conan artifacts in the local
Conan cache.

Features
--------

This Conan plugin makes it dead-simple to create Conan artifacts in the agent's local Conan cache. Sane defaults are
used wherever possible, while still providing a variety of advanced features for more complex scenarios.

For projects that need to built with a multiple different settings/options combinations, support for a file named
`conanopts.json` is provided. This file can be used to define a list of builds, each with different settings/options
used, and the Conan plugin will invoke `conan create ...` for each different combination. Support for a CSV file
defining the same information is coming soon.

Optionally, Conan can be invoked from within a Docker image of your choice. The UI for configuring the Docker container
was shamelessly pulled from JetBrain's own Docker plugin, and all the same features are (should be) available.

Screenshots
-----------

### Forward Assumptions

This sample build configuration assumes the following:

1. The Conan home directory (importantly, the Conan cache, Conan database/credentials, and remote repository listing) is
   located on the TeamCity agent's host filesystem.
2. When the Conan plugin makes use of Docker, the Conan cache is bound inside the Docker container, allowing for use by
   later build steps in the same configuration or other projects on the same agent.
3. A separate `conan upload ...` step would normally be the last step of this build configuration, but was left off
   during testing and is therefore not in the screenshots.

### Sample Build Configuration

To begin with, we have a simple build configuration that contains three steps:

1. The Conan remote repository list is configured to include any custom repositories needed, allowing the following to
   build steps to pull any private dependencies.
2. The project is built for the TeamCity agent's native architecture.
3. The project is built for TI's Sitara family of ARM processors.

(Again, a fourth step would usually need to be included, to upload the resulting artifacts, but was left off during the
testing of this plugin.)

![Screenshot of sample build steps][1]

The simplest configuration is, indeed, quite simple. Device the user and channel for the
`conan create . <user>/<channel>` command, and the rest is figured out automatically.

![Screenshot of the most basic configuration][2]

If needed, numerous advanced options are available, including defining a specific path to Conan (in case there are
multiple versions installed) or the Conan recipe.

![Screenshot of all the advanced settings][3]

And finally, an example of using Docker with the Conan plugin. This UI should look happily familiar to anyone that has
grown accustomed to JetBrain's own Docker integration, as documented [here][5].

![Screenshot of a simple configuration but using Docker][4]


[1]: https://raw.githubusercontent.com/wsbu/TeamCity.ConanPlugin/master/images/build_steps.png
[2]: https://raw.githubusercontent.com/wsbu/TeamCity.ConanPlugin/master/images/simple_step.png
[3]: https://raw.githubusercontent.com/wsbu/TeamCity.ConanPlugin/master/images/advanced_settings.png
[4]: https://raw.githubusercontent.com/wsbu/TeamCity.ConanPlugin/master/images/simple_step_in_docker.png
[5]: https://www.jetbrains.com/help/teamcity/integrating-teamcity-with-docker.html
