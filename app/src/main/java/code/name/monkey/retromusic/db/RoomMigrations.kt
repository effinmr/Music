package code.name.monkey.retromusic.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE LyricsEntity")
        database.execSQL("DROP TABLE BlackListStoreEntity")
    }
}

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PlaylistEntity ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS song_metadata (
                id INTEGER PRIMARY KEY NOT NULL,
                title TEXT,
                trackNumber INTEGER,
                year TEXT,
                duration INTEGER,
                data TEXT,
                dateModified INTEGER,
                artistId INTEGER,
                albumId INTEGER,
                albumName TEXT,
                artistName TEXT,
                composer TEXT,
                albumArtist TEXT,
                allArtists TEXT
            )
        """.trimIndent())
    }
}

val allMigrations = arrayOf(
    MIGRATION_23_24,
    MIGRATION_25_26,
    MIGRATION_26_27
)
