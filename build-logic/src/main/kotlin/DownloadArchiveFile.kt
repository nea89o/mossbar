import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.InputStream
import java.net.URI

abstract class DownloadArchiveFile<T : InputStream> : DefaultTask() {
    @get:Input
    abstract val url: Property<URI>

    @get:Input
    abstract val stripRoot: Property<Boolean>

    @get:Input
    abstract val subDirectory: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        subDirectory.convention("")
        stripRoot.convention(false)
    }

    fun url(string: String) {
        url.set(URI.create(string))
    }

    abstract fun mapInputStream(inputStream: InputStream): T
    abstract fun nextEntry(stream: T): String?

    @TaskAction
    fun execute() {
        val baseFolder = outputDir.get().asFile
        baseFolder.deleteRecursively()
        baseFolder.mkdirs()
        val pathFilter = this.subDirectory.get()
        require(pathFilter.isEmpty() || pathFilter.endsWith("/")) {
            "subdirectory '$pathFilter' does not end with a /"
        }
        url.get().toURL().openStream().buffered().use { input ->
            val mappedStream = mapInputStream(input)
            while (true) {
                val entry = nextEntry(mappedStream) ?: break
                var name = entry
                if (name.endsWith("/")) continue
                if (stripRoot.get()) {
                    name = name.substringAfter("/")
                }
                if (pathFilter.isNotEmpty()) {
                    if (!name.startsWith(pathFilter)) {
                        continue
                    } else {
                        name = name.substring(pathFilter.length)
                    }
                }
                val fileLocation = baseFolder.resolve(name).absoluteFile
                if (baseFolder !in generateSequence(fileLocation) { it.parentFile }) {
                    error("File escaping output jail: $name")
                }
                fileLocation.parentFile.mkdirs()
                fileLocation.outputStream().use { output ->
                    mappedStream.copyTo(output)
                }
            }
        }
    }
}
