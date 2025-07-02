import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.InputStream
import java.net.URI
import java.util.zip.GZIPInputStream

abstract class DownloadTarFile : DownloadArchiveFile<TarArchiveInputStream>() {
    @get:Input
    abstract val gunzip: Property<Boolean>

    init {
        gunzip.convention(url.map { it.path.endsWith(".tar.gz") })
    }

    abstract class CGitSource {
        abstract val domain: Property<String>
        abstract val projectPath: Property<String>
        abstract val hash: Property<String>

        fun toUrl(): URI {
            return URI.create("https://${domain.get()}/${projectPath.get()}/snapshot/${hash.get()}.tar.gz")
        }
    }

    fun cGit(configure: CGitSource.() -> Unit) {
        stripRoot.convention(true)
        url.set(
            project.provider(
                project.objects.newInstance(CGitSource::class.java).also(configure)::toUrl
            )
        )
    }


    override fun mapInputStream(inputStream: InputStream): TarArchiveInputStream {
        return TarArchiveInputStream(
            if (gunzip.get()) {
                GZIPInputStream(inputStream, 8192)
            } else {
                inputStream
            }
        )
    }

    override fun nextEntry(stream: TarArchiveInputStream): String? {
        return stream.nextTarEntry?.name
    }
}