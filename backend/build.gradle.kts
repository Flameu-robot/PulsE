plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    kotlin("jvm") version "2.1.0" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")

    configure<CheckstyleExtension> {
        toolVersion = "10.21.0"
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        configProperties = mapOf(
            "suppressionFile" to rootProject.file("config/checkstyle/suppressions.xml").absolutePath
        )
        isIgnoreFailures = false
        maxWarnings = 0
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}