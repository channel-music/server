package kalouantonis.channel

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagException
import java.io.File

/**
 * Represents metadata for a media file.
 */
data class MediaMetadata(
        val title: String,
        val album: String,
        val artist: String,
        val genre: String,
        val track: Int?
)

/**
 * Thrown when parsing a media file for metadata fails.
 */
class MetadataParserException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Generate media metadata using the given file.
 */
fun parseMetadata(file: File): MediaMetadata {
    val audioTag = try {
        AudioFileIO.read(file).tag
    } catch (e: CannotReadException) {
        throw MetadataParserException("Failed to read audio file", e)
    } catch (e: TagException) {
        throw MetadataParserException("File contains invalid audio tag", e)
    } catch (e: InvalidAudioFrameException) {
        throw MetadataParserException("File contains invalid audio frame", e)
    }
    return MediaMetadata(
            title = audioTag.getFirst(FieldKey.TITLE),
            album = audioTag.getFirst(FieldKey.ALBUM),
            artist = audioTag.getFirst(FieldKey.ARTIST),
            genre = audioTag.getFirst(FieldKey.GENRE),
            // TODO: Add total tracks too
            track = audioTag.getFirst(FieldKey.TRACK).toIntOrNull()
    )
}

