plugins {
    id("java")
    `maven-publish`
}

group = "me.exeos"
version = "1.0.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.asm)
    implementation(libs.asm.analysis)
    implementation(libs.asm.commons)
    implementation(libs.asm.tree)
    implementation(libs.asm.util)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "asmplus"
        }
    }
}
