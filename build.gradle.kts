plugins {
    id("java")
    id("systems.manifold.manifold-gradle-plugin") version "0.0.2-alpha"
    application
}

allprojects {
    group = "moe.nea"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.nea.moe/mirror")
        maven("https://repo.nea.moe/release")
    }
}

manifold {
    manifoldVersion.set("2025.1.24")
}

application {
    mainClass.set("moe.nea.mossbar.Launch")
    // We LOVE deprecating methods without an upgrade path!! shoutout to varhandle for being useless
    applicationDefaultJvmArgs += listOf("--add-opens=java.base/java.lang=ALL-UNNAMED", "--enable-native-access=ALL-UNNAMED")

    // on nix: set LD_LIBRARY_PATH=$(nix-build '<nixpkgs>' -A wayland)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(24))

dependencies {
    val manifoldSubSystems = listOf(
        "json" to true,
        "params" to true,
        "strings" to false,
        "props" to true,
    )
    manifoldSubSystems.forEach { (name, rt) ->
        if (rt)
            implementation("systems.manifold:manifold-$name-rt:${manifold.manifoldVersion.get()}")
        annotationProcessor("systems.manifold:manifold-$name:${manifold.manifoldVersion.get()}")
    }


    implementation(project(":protocols"))
    implementation("org.jspecify:jspecify:1.0.0")

    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}