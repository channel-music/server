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
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

// FIXME: Probably a terrible idea
fun Song.toJson(): String = jacksonObjectMapper().writeValueAsString(this)

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
                album = "Insomnia: The Best Of Faithless"
        )))
        songs.add(songRepository.save(Song(
                title = "Teardrop",
                artist = "Massive Attack",
                album = "Mezzanine"
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
    fun `create a song using valid JSON`() {
        val newSong = Song(
                title = "At the River",
                artist = "Groove Armada",
                album = "The best of"
        )
        mockMvc.perform(post("/songs").content(newSong.toJson()).contentType(contentType))
                .andExpect(status().isCreated)
                .andExpect(content().string(""))
        assertEquals(songRepository.count().toInt(), songs.size + 1)
    }

    @Test
    fun `fail at creating a song with invalid JSON`() {
        mockMvc.perform(post("/songs").content("{\"title\": \"Some title\"}").contentType(contentType))
                .andExpect(status().isBadRequest)
        assertEquals(songRepository.count().toInt(), songs.size)
    }
}
