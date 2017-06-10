package kalouantonis.channel

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

// FIXME: Probably a terrible idea
fun Song.toJson(): String = jacksonObjectMapper().writeValueAsString(this)

// TODO: This is an integration test, move to a separate folder
@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class SongRestControllerTest {
    private val contentType: String = "application/hal+json;charset=UTF-8"

    @Autowired
    private lateinit var songRepository: SongRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    private var songs: MutableList<Song> = mutableListOf()

    @Before
    fun setup() {
        songs.add(songRepository.save(Song(
                title = "Insomnia",
                artist = "Faithless",
                album = "Insomnia: The Best Of Faithless",
                filePath = "not-a-file-path.mp3"
        )))
        songs.add(songRepository.save(Song(
                title = "Teardrop",
                artist = "Massive Attack",
                album = "Mezzanine",
                filePath = "not-a-file-path.mp3"
        )))
    }

    @After
    fun teardown() {
        // FIXME: find a better way of doing this
        songRepository.deleteAll()
    }

    @Test
    fun `request all songs`() {
        mockMvc.perform(get("/songs"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$._embedded.songResourceList[0].song.id")
                        .value(songs[0].id))
                .andExpect(jsonPath("$._embedded.songResourceList[0].song.title")
                        .value(songs[0].title))
                .andExpect(jsonPath("$._embedded.songResourceList[1].song.id")
                        .value(songs[1].id))
                .andExpect(jsonPath("$._embedded.songResourceList[1].song.title")
                        .value(songs[1].title))
    }

    @Test
    fun `request a specific song using an ID`() {
        val song = songs[0]
        mockMvc.perform(get("/songs/${song.id}"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(contentType))
                // Comparing with the plain JSON string just doesn't seem to
                // work at the moment, hence the repetition.
                .andExpect(jsonPath("$.song.id").value(song.id))
                .andExpect(jsonPath("$.song.title").value(song.title))
                .andExpect(jsonPath("$.song.artist").value(song.artist))
                .andExpect(jsonPath("$.song.album").value(song.album))
    }

    @Test
    fun `responds with not found when getting a song with an invalid ID`() {
        mockMvc.perform(get("/songs/${songs.last().id!! + 1}"))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `create a song using valid media file`() {
        val newSong = Song(
                title = "At the River",
                artist = "Groove Armada",
                album = "The best of",
                filePath = "not-a-file-path.mp3"
        )
        mockMvc.perform(post("/songs").content(newSong.toJson()).contentType(contentType))
                .andExpect(status().isCreated)
                .andExpect(content().string(""))
        assertEquals(songRepository.count().toInt(), songs.size + 1)
    }

    @Test
    fun `fail at creating a song with invalid media file`() {
        val multipartFile = MockMultipartFile("file", "test.txt", "text/plain",
                "This isn't a media file".byteInputStream())
        mockMvc.perform(multipart("/songs").file(multipartFile))
                .andExpect(status().isBadRequest)
        assertEquals(songRepository.count().toInt(), songs.size)
    }

    @Test
    fun `fail at creating a duplicate song`() {

    }
}
