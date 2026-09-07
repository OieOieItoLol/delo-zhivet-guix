import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.spring.dependency-management") version "1.1.0"
    id("org.springframework.boot") version "3.5.5"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.10"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    `java-library`
    id("nu.studer.jooq") version "10.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

group = "delo-zhivet"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.5"))
    implementation("org.springframework.boot:spring-boot-dependencies")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-netty")
    }
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")
    implementation("com.github.kuliginstepan:dadata-client:4.0.0")
    implementation("com.tinder.statemachine:statemachine:0.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    implementation("com.fasterxml.uuid:java-uuid-generator:5.1.0")
    implementation("org.locationtech.jts:jts-core:1.20.0")

    implementation(platform("io.arrow-kt:arrow-stack:1.2.4"))
    implementation("io.arrow-kt:arrow-core")

    implementation("org.postgresql:postgresql:42.7.5")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

allprojects {
    group = "ru.delo.zhivet"
    version = property("version") ?: "1.0.0-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
        outputToConsole.set(true)
        outputColorName.set("RED")
    }

    repositories {
        mavenCentral()
    }
}
