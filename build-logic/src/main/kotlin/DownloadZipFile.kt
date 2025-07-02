import org.gradle.api.provider.Property
import java.io.InputStream
import java.net.URI
import java.util.zip.ZipInputStream

abstract class DownloadZipFile : DownloadArchiveFile<ZipInputStream>() {
    abstract class GitLabSource {
        abstract val domain: Property<String>
        abstract val owner: Property<String>
        abstract val project: Property<String>
        abstract val hash: Property<String>

        fun toUrl(): URI {
            return URI.create("https://${domain.get()}/${owner.get()}/${project.get()}/-/archive/${hash.get()}/${project.get()}-${hash.get()}.zip")
        }

    }

    fun gitlab(configure: GitLabSource.() -> Unit) {
        stripRoot.convention(true)
        url.set(
            project.provider(
                project.objects.newInstance(GitLabSource::class.java).also(configure)::toUrl
            )
        )
    }

    override fun mapInputStream(inputStream: InputStream): ZipInputStream {
        return ZipInputStream(inputStream)
    }

    override fun nextEntry(stream: ZipInputStream): String? {
        return stream.nextEntry?.name
    }
}