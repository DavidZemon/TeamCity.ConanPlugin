/**
 * JetBrains Space Automation
 * This Kotlin-script file lets you automate build activities
 * For more info, see https://www.jetbrains.com/help/space/automation.html
 */

job("Build") {
    container("maven:3-jdk-11") {
        args("mvn", "clean", "package")
    }
}
