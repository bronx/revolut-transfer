import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.3.61"
}

group = "com.revolut"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_13

repositories {
	mavenCentral()
	jcenter()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.exposed:exposed:0.17.7")
	implementation("io.javalin:javalin:3.6.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")
	implementation("org.slf4j:slf4j-simple:1.7.28")
	implementation("com.h2database:h2:1.4.200")
	implementation("org.jetbrains.exposed:exposed:0.17.7")
	implementation("com.zaxxer:HikariCP:3.4.1")
	testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
	testImplementation("io.rest-assured:rest-assured:4.1.2")
	testImplementation("io.rest-assured:rest-assured-all:4.1.2")
	testImplementation("org.assertj:assertj-core:3.14.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

val jar by tasks.getting(Jar::class) {
	manifest {
		attributes["Main-Class"] = "com.revolut.transfer.TransferService"
	}
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
