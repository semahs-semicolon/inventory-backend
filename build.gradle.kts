import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.10"
	kotlin("plugin.spring") version "1.8.10"
	id("com.palantir.docker") version "0.22.1"
}

group = "io.seda"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_18
java.targetCompatibility = JavaVersion.VERSION_18

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}



dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux") {
//		exclude("org.springframework.boot", "spring-boot-starter-reactor-netty")
	}
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	compileOnly("org.projectlombok:lombok")
	implementation("com.auth0:java-jwt:4.3.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	implementation("org.postgresql:postgresql")
	implementation("org.postgresql:r2dbc-postgresql:1.0.4.RELEASE")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation(platform("software.amazon.awssdk:bom:2.17.133"))
	implementation("software.amazon.awssdk:s3")
	implementation("software.amazon.awssdk:ssm")
	implementation("software.amazon.awssdk:lambda")
	implementation("software.amazon.awssdk:netty-nio-client")
	implementation("org.dhatim:fastexcel:0.17.0")

	implementation("com.amazonaws.serverless:aws-serverless-java-container-springboot3:2.0.3") {
//		exclude("org.springframework", "spring-webmvc")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "18"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}


tasks.register<Zip>("buildZip" ) {
	this.archiveFileName.value("inventory.zip")

	this.from(tasks.compileJava)
	this.from(tasks.compileKotlin)
	this.from(tasks.processResources)
	into("lib") {
		from(configurations.compileClasspath) {
			exclude("tomcat-embed-*")
		}
	}
}

tasks.build {
	dependsOn("buildZip")
}
