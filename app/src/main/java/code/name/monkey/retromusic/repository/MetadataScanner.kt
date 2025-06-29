package code.name.monkey.retromusic.repository

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.metadata.id3.Id3Frame
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame

class MetadataScanner(private val context: Context) {
    interface ScanListener {
        fun onDateScanned(path: String, recordDate: String?)
    }

    fun scan(path: String, listener: ScanListener) {
        val player = ExoPlayer.Builder(context).build()

        player.addMetadataOutput { metadata ->
            for (i in 0 until metadata.length()) {
                val entry = metadata[i]
                if (entry is Id3Frame && (entry.id == "TDRC" || entry.id == "TYER")) {
                    val date = (entry as TextInformationFrame).value
                    player.release()
                    listener.onDateScanned(path, date)
                    return@addMetadataOutput
                }
            }
            player.release()
            listener.onDateScanned(path, null)
        }

        player.setMediaItem(MediaItem.fromUri(path))
        player.prepare()
        player.playWhenReady = false
    }
}
