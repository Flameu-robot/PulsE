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
    api(libs.spring.boot.starter.security)

    api("org.springframework.boot:spring-boot-autoconfigure")

    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(libs.jakarta.servlet.api)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}