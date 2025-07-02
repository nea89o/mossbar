plugins {
    id("mossbar.downloads")
    `java-library`
}

dependencies {
    annotationProcessor("org.freedesktop.wayland:wayland-scanner:2.0.0-nea-SNAPSHOT")
    api("org.freedesktop.wayland:stubs-client:2.0.0-nea-SNAPSHOT")
    implementation("com.github.spotbugs:spotbugs-annotations:4.9.3")
}

fun DownloadZipFile.GitLabSource.freeDesktopOrg() {
    domain.set("gitlab.freedesktop.org")
}

val waylandCoreProtocols = downloads.zip("waylandCore") {
    gitlab {
        freeDesktopOrg()
        owner.set("wayland")
        project.set("wayland")
        hash.set("1.23.91")
    }
    subDirectory.set("protocol/")
}

val wlrootsProtocols = downloads.zip("wlroots") {
    gitlab {
        freeDesktopOrg()
        owner.set("wlroots")
        project.set("wlr-protocols")
        hash.set("a5028afbe4a1cf0daf020c4104c1565a09d6e58a")
    }
}

val waylandProtocols = downloads.zip("waylandExtra") {
    gitlab {
        freeDesktopOrg()
        owner.set("wayland")
        project.set("wayland-protocols")
        hash.set("1.44")
    }
}

val allProtocols = tasks.register("allProtocols", Copy::class) {
    from(waylandProtocols) {
        into("extra")
    }
    from(waylandCoreProtocols) {
        into("core")
    }
    from(wlrootsProtocols) {
        into("wlr")
    }
    into(layout.buildDirectory.dir("allProtocols"))
}

tasks.compileJava {
    dependsOn(allProtocols)
    options.compilerArgs.add(
        "-Awayland.scanner.protocol.root=${
            allProtocols.map { it.destinationDir.absolutePath }.get()
        }"
    )
}
