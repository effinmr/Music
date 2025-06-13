package code.name.monkey.retromusic.model

import code.name.monkey.retromusic.R

enum class MetadataField(val id: Int, val labelRes: Int) {
    ALBUM(0, R.string.album),
    ARTIST(1, R.string.artist),
    YEAR(2, R.string.year),
    BITRATE(3, R.string.label_bit_rate),
    FORMAT(4, R.string.label_file_format),
    TRACK_LENGTH(5, R.string.label_track_length),
    FILE_NAME(6, R.string.label_file_name),
    FILE_PATH(7, R.string.label_file_path),
    FILE_SIZE(8, R.string.label_file_size),
    SAMPLING_RATE(9, R.string.label_sampling_rate),
    LAST_MODIFIED(10, R.string.label_last_modified);

    companion object {
        fun fromId(id: Int): MetadataField? = values().find { it.id == id }
    }
}
