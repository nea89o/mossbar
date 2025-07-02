plugins {
    java
    `kotlin-dsl`
    kotlin("jvm") version "2.1.21"
}
repositories {
    mavenCentral()
}
java.toolchain.languageVersion.set(JavaLanguageVersion.of(24))

dependencies {
    implementation("org.apache.commons:commons-compress:1.23.0")
}
