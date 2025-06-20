plugins {
    id("java")
    id("application")
}


application {
    mainClass.set("it.unibas.ids.IDSMain")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "it.unibas.ids.IDSMain"
    }
}

group = "it.unibas"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":simulator"))
    implementation(project(":common"))
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    implementation("com.google.inject:guice:7.0.0")
    implementation("org.aspectj:aspectjweaver:1.9.19")
    implementation("org.aspectj:aspectjrt:1.9.19")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // JUnit per i test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.1.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.0")
}

tasks.test {
    useJUnitPlatform()
}