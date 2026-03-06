plugins {
    // плагин для библиотек
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

group = "com.pulse"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    api(libs.spring.boot.starter)
}