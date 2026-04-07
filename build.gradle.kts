plugins {
    kotlin("jvm") version "2.3.0"
    application
}
application {
    mainClass.set("org.example.MainKt")
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-simple:2.0.7")
}
group = "org.example"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}