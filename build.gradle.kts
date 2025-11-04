plugins {
    id("java")
}

group = "com.tuca"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("mysql:mysql-connector-java:8.0.33")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
