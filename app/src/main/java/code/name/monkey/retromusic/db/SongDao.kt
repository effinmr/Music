@Dao
interface SongDao {

    @Update
    fun updateSong(song: SongEntity)

    @Query("SELECT * FROM SongEntity WHERE id = :id LIMIT 1")
    fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM SongEntity WHERE year IS NULL OR year = ''")
    fun getUnscannedSongs(): List<SongEntity>
}
