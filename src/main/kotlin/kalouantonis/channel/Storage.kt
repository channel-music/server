package kalouantonis.channel

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Files.copy
import java.nio.file.Path
import java.nio.file.Paths

interface StorageService {
    fun store(filename: String, inputStream: InputStream): File
    fun retrieve(path: Path): File
    fun delete(path: Path)
}

@ConfigurationProperties("storage")
class StorageProperties(val location: String = "media")

@Service
class FileStorageService(storageProperties: StorageProperties) : StorageService {
    private val rootLocation: Path = Paths.get(storageProperties.location)

    override fun store(filename: String, inputStream: InputStream): File {
        val path = rootLocation.resolve(filename)
        copy(inputStream, path)
        return retrieve(path)
    }

    override fun retrieve(path: Path): File = path.toFile()

    override fun delete(path: Path) {
        path.toFile().delete()
    }
}
