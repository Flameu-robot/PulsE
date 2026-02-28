plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(platform(libs.spring.cloud.dependencies))

    implementation(libs.bundles.gateway)

    annotationProcessor(libs.spring.boot.configuration.processor)
}

tasks.withType<Test> {
    useJUnitPlatform()
}