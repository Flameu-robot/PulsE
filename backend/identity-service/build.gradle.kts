plugins {
	java
	alias(libs.plugins.spring.boot)
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

	implementation(libs.bundles.spring.base)
	implementation(libs.bundles.spring.security)
	implementation(libs.bundles.jwt)
	implementation(libs.bundles.webauthn)
	implementation(libs.bundles.database)

	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.data.redis)
	implementation(libs.spring.boot.starter.mail)
	implementation(libs.spring.kafka)
	implementation(libs.minio)
	implementation(libs.springdoc.openapi.webmvc.ui)
	implementation(libs.mapstruct)
	implementation(libs.hypersistence.utils)

	runtimeOnly(libs.postgresql)
	runtimeOnly(libs.micrometer.prometheus)

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