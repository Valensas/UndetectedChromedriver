import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("com.diffplug.spotless") version "6.21.0"
    kotlin("jvm") version "2.0.21"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
    id("maven-publish")
}

spotless {
    java {
        googleJavaFormat("1.17.0") // Use Google's Java formatter
        target("src/**/*.java")
    }
}

group = "com.valensas"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:4.27.0")
    api("org.seleniumhq.selenium:selenium-devtools-v129:4.27.0")
    api("org.seleniumhq.selenium:selenium-devtools-v130:4.27.0")
    api("org.seleniumhq.selenium:selenium-devtools-v131:4.27.0")
    api("io.github.bonigarcia:webdrivermanager:5.9.2")
    api("com.alibaba:fastjson:2.0.53")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

signing {
    val keyId = System.getenv("SIGNING_KEYID")
    val secretKey = System.getenv("SIGNING_SECRETKEY")
    val passphrase = System.getenv("SIGNING_PASSPHRASE")

    useInMemoryPgpKeys(keyId, secretKey, passphrase)
}

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")

    pom {
        name = "Valensas Kafka"
        description = "This library contains an Chrome driver implementation which is " +
                "implemented to avoid bot detections for a selenium project"
        url = "https://valensas.com/"
        scm {
            url = "https://github.com/Valensas/UndetectedChromeDriver"
        }

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("0")
                name.set("Valensas")
                email.set("info@valensas.com")
            }
        }
    }
}
