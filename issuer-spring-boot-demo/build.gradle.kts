import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.5"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "2.0.20"
	kotlin("plugin.spring") version "2.0.20"
	kotlin("kapt") version "1.9.10"
}

group = "io.mica.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

kotlin{
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
	// Or shorter:
	jvmToolchain(21)
	// For example:
	jvmToolchain(21)
}

repositories {
	mavenLocal()
	mavenCentral()
	google()
	maven {
		url = uri("https://maven.pkg.github.com/1080network/public")
		credentials {
			username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USER")
			password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
		}
	}
}

dependencies {
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	//grpc stack
	implementation("net.devh:grpc-server-spring-boot-starter:2.15.0.RELEASE")

	//mica skd
	implementation("io.mica.sdk.kotlin:serviceprovider:v0.0.1090")
	implementation("io.mica.sdk.kotlin:micacommon:v0.0.1090")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
