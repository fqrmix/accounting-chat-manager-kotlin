plugins {
    id("org.jetbrains.kotlinx.dataframe") version "0.12.0"
    id("org.springframework.boot") version "3.1.2"

    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"

    application
}

apply(plugin = "io.spring.dependency-management")

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url =  uri("https://jitpack.io")
    }
    maven("https://dl.bintray.com/kotlin/kotlinx/")
}

dependencies {
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")
    implementation("org.jetbrains.kotlinx:dataframe:0.12.0")
    implementation("org.postgresql:postgresql:42.7.1")

    val exposedVersion: String by project
    dependencies {
        implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
        implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    }

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("$group.MainKt")
}
