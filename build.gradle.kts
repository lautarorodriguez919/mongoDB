plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"
    kotlin("plugin.allopen") version "2.3.0"
    application
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
application {
    mainClass.set("Practica_Hibernate.MainKt")
}

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(kotlin("test"))
    //implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    //implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.hibernate.orm:hibernate-core:6.4.0.Final")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    runtimeOnly("org.postgresql:postgresql:42.7.0") // o tu DB
}
group = "org.example"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}