import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.util.*
import javax.inject.Inject

abstract class DownloadExtension {
    @get:Inject
    abstract val project: Project

    fun downloadTaskName(name: String) =
        "download" + name.capitalize(Locale.ROOT)

    inline fun <reified T : DownloadArchiveFile<*>> archive(name: String, noinline configure: T.() -> Unit)
            : TaskProvider<T> =
        project.tasks.register(downloadTaskName(name), T::class.java) {
            outputDir.convention(project.layout.buildDirectory.dir("downloadedSources/$name"))
            configure(this)
        }

    fun zip(name: String, configure: DownloadZipFile.() -> Unit): TaskProvider<DownloadZipFile> {
        return archive(name, configure)
    }

    fun tar(name: String, configure: DownloadTarFile.() -> Unit): TaskProvider<DownloadTarFile> {
        return archive(name, configure)
    }
}
