package code.name.monkey.retromusic.repository

import android.util.Log
import code.name.monkey.retromusic.db.SongMetadataDao
import code.name.monkey.retromusic.db.SongMetadataEntity
import code.name.monkey.retromusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

class MetadataScanner(
    private val dao: SongMetadataDao,
) {

    suspend fun scanIfNotExists(
        songList: List<Song>,
        onProgress: (songTitle: String, index: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        songList.forEachIndexed { idx, song ->
            if (dao.getMetadataById(song.id) != null) return@forEachIndexed

            onProgress(song.title, idx + 1, songList.size)

            val audioFile = AudioFileIO.read(java.io.File(song.data))
            val tag = audioFile.tagOrCreateAndSetDefault

            val entity = SongMetadataEntity(
                id = song.id,
                title = song.title,
                albumName = song.albumName,
                artistName = song.artistName, // For display
                composer = song.composer,
                year = tag.getFirst(FieldKey.YEAR).ifBlank { null },
                trackNumber = song.trackNumber,
                duration = song.duration,
                albumArtist = song.albumArtist,
                data = song.data,
                dateModified = song.dateModified,
                albumId = song.albumId,
                artistId = song.artistId,
                allArtists = song.allArtists
            )
            dao.insert(entity)
        }
    }

}
