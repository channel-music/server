package kalouantonis.channel

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files.copy
import java.nio.file.Path
import java.nio.file.Paths

interface StorageService {
    fun store(file: File): Path
    fun retrieve(path: Path): File
}

@ConfigurationProperties("storage")
class StorageProperties(val location: String = "uploads")

@Service
class FileStorageService(storageProperties: StorageProperties) : StorageService {
    private val rootLocation: Path = Paths.get(storageProperties.location)

    override fun store(file: File): Path {
        val path = rootLocation.resolve(file.name)
        try {
            copy(file.inputStream(), path)
        } catch (e: IOException) {
            // TODO: throw custom exception
            throw e
        }
        return path
    }

    override fun retrieve(path: Path): File {
        TODO("NOT IMPLEMENTED")
    }

}
