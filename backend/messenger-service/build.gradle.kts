plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.pulse"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        // Используем Java 25, как указано в твоем каталоге версий
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(libs.bundles.spring.base)
    implementation(libs.bundles.spring.security)
    implementation(libs.spring.boot.starter.websocket)

    implementation(libs.bundles.database)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.hypersistence.utils)
    implementation(libs.minio)
    runtimeOnly(libs.postgresql)

    implementation(libs.bundles.kafka)

    implementation(libs.springdoc.openapi.webmvc.ui)
    implementation(libs.mapstruct)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(project(":shared"))

    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.junit.launcher)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=IGNORE"
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}