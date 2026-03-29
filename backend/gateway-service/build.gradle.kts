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
    implementation(libs.bundles.jwt)

    implementation(libs.springdoc.openapi.webflux.ui)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(libs.jspecify)
    testCompileOnly(libs.jspecify)

    testImplementation(libs.bundles.testing.base)
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Убирает предупреждение о загрузке агентов в Java 25
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("25"))
    }
}