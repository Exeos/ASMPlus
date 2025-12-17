plugins {
    `java-library`
    `maven-publish`
}

group = "me.exeos"
version = "1.5.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://stianloader.org/maven")
    }
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(libs.bundles.asm)
    implementation(libs.stianloader.remapper)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "me.exeos"
            artifactId = "asmplus"
            version = "1.5.0"
        }
    }
}