package kalouantonis.channel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Song(
    val title: String,
    val artist: String,
    val album: String
) {
    @Id
    @GeneratedValue
    var id: Long? = null
}

interface SongRepository : JpaRepository<Song, Long>

open class SongResource(val song: Song) : ResourceSupport() {
    init {
        add(linkTo(SongRestController::class.java).withRel("songs"))
        add(linkTo(methodOn(SongRestController::class.java)
                .songById(song.id!!)).withSelfRel())
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class SongNotFoundException(songId: Long) : RuntimeException("could not find song '$songId'")

@RestController
@RequestMapping("/songs")
class SongRestController(val songRepository: SongRepository) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun allSongs(): Resources<SongResource> =
            Resources(songRepository.findAll().map { SongResource(it) })

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/{songId}")
    fun songById(@PathVariable songId: Long): SongResource =
        SongResource(songRepository.findById(songId)
                .orElseThrow { SongNotFoundException(songId) })

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun createSong(@RequestBody input: Song): ResponseEntity<Any> {
        val song = songRepository.save(input)
        val link = SongResource(song).getLink("self")
        return ResponseEntity.created(URI.create(link.href)).build()
    }

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "/{songId}")
    fun deleteSong(@PathVariable songId: Long): ResponseEntity<Any> {
        songRepository.deleteById(songId)
        return ResponseEntity.noContent().build()
    }

}
