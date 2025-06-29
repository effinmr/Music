fun Song.toSongEntity(playlistCreatorId: Long, songPrimaryKey: Long = 0L): SongEntity {
    return SongEntity(
        songPrimaryKey = songPrimaryKey,
        playlistCreatorId = playlistCreatorId,
        id = this.id,
        title = this.title,
        trackNumber = this.trackNumber,
        year = this.year,
        duration = this.duration,
        data = this.data,
        dateModified = this.dateModified,
        albumId = this.albumId,
        albumName = this.albumName,
        artistId = this.artistId,
        artistName = this.artistName,
        composer = this.composer,
        albumArtist = this.albumArtist
    )
}
