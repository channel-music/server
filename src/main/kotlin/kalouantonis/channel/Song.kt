package kalouantonis.channel

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Song(val title: String,
                val artist: String,
                val album: String) {
    @Id
    @GeneratedValue
    var id: Long? = null
}


interface SongRepository : JpaRepository<Song, Long>

@ResponseStatus(HttpStatus.NOT_FOUND)
class SongNotFoundException(songId: Long) : RuntimeException("could not find song '$songId'")

@RestController
@RequestMapping("/songs")
class SongRestController(val songRepository: SongRepository) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun allSongs(): Collection<Song> = songRepository.findAll()

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/{songId}")
    fun songById(@PathVariable songId: Long): Song? =
        songRepository.findById(songId).orElseThrow { SongNotFoundException(songId) }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun createSong(@RequestBody song: Song): ResponseEntity<Any> {
        val result = songRepository.save(song)
        val location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(result.id).toUri()
        return ResponseEntity.created(location).build()
    }

    @RequestMapping(method = arrayOf(RequestMethod.DELETE), value = "/{songId}")
    fun deleteSong(@PathVariable songId: Long): ResponseEntity<Any> {
        songRepository.deleteById(songId)
        return ResponseEntity.noContent().build()
    }

}
