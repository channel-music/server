package kalouantonis.channel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.file.Paths
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Song(
    val title: String,
    val artist: String,
    val album: String,
    val filePath: String
) {
    @Id
    @GeneratedValue
    var id: Long? = null
}

interface SongRepository : JpaRepository<Song, Long>

// FIXME: Write extension (spring-hateoas) to allow not using open
// FIXME: for resources.
open class SongResource(val song: Song) : ResourceSupport() {
    init {
        add(linkTo(SongRestController::class.java).withRel("songs"))
        add(linkTo(methodOn(SongRestController::class.java)
                .songById(song.id!!)).withSelfRel())
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class SongNotFoundException(songId: Long) : Exception("could not find song '$songId'")

@RestController
@RequestMapping("/songs")
class SongRestController(val songRepository: SongRepository, val storageService: StorageService) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun allSongs(): Resources<SongResource> =
            Resources(songRepository.findAll().map { SongResource(it) })

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/{songId}")
    fun songById(@PathVariable songId: Long): SongResource =
        SongResource(songRepository.findById(songId)
                .orElseThrow { SongNotFoundException(songId) })

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun createSong(@RequestParam("file") input: MultipartFile): ResponseEntity<Any> {
        val file = storageService.store(input.originalFilename, input.inputStream)
        val metadata = try {
            parseMetadata(file)
        } catch(e: MetadataParserException) {
            storageService.delete(file.toPath())
            // TODO: just rethrow
            return ResponseEntity.badRequest().body(e.message)
        }
        val song = songRepository.save(Song(
                title = metadata.title,
                artist = metadata.artist,
                album = metadata.album,
                filePath = file.path // TODO: don't use path directly, what if rootLocation changes?
        ))
        val link = SongResource(song).getLink("self")
        return ResponseEntity.created(URI.create(link.href)).build()
    }

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "/{songId}")
    fun deleteSong(@PathVariable songId: Long): ResponseEntity<Any> {
        val song = songRepository.findById(songId)
                .orElseThrow { SongNotFoundException(songId) }
        songRepository.delete(song)
        storageService.delete(Paths.get(song.filePath))
        return ResponseEntity.noContent().build()
    }

}
