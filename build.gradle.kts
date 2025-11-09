import jdk.tools.jlink.resources.plugins
import java.lang.Runtime.version

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}


group = "com.tuca"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.jpa)
    implementation(libs.kafka.clients) {
        exclude(group = "org.slf4j")
    }
    implementation(libs.mysql.connector)
    implementation(libs.org.json)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.tuca.Main"
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { zipTree(it) }
    })
}