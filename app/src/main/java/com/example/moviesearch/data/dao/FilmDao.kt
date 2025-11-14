package com.example.moviesearch.data.db

import androidx.room.*
import com.example.moviesearch.data.entity.Film

@Dao
interface FilmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilms(films: List<Film>)

    @Query("SELECT * FROM films")
    suspend fun getAllFilms(): List<Film>

    @Query("SELECT * FROM films WHERE id = :filmId")
    suspend fun getFilmById(filmId: Int): Film?

    @Query("DELETE FROM films WHERE id = :filmId")
    suspend fun deleteFilm(filmId: Int)

    @Query("DELETE FROM films")
    suspend fun deleteAllFilms()

    // Обновите этот метод для работы с полем year
    @Query("SELECT * FROM films WHERE year BETWEEN :startYear AND :endYear")
    suspend fun getFilmsByYearRange(startYear: Int, endYear: Int): List<Film>

    // Обновите этот метод для работы с полями title, originalTitle, alternativeName
    @Query("""
        SELECT * FROM films 
        WHERE title LIKE '%' || :title || '%' 
        OR originalTitle LIKE '%' || :title || '%'
        OR alternativeName LIKE '%' || :title || '%'
    """)
    suspend fun getFilmsByTitle(title: String): List<Film>

    @Query("SELECT COUNT(*) FROM films")
    suspend fun getFilmsCount(): Int

    // Обновите этот метод для работы с genres (список строк)
    @Query("SELECT * FROM films WHERE genres LIKE '%' || :genre || '%'")
    suspend fun getFilmsByGenre(genre: String): List<Film>

    @Query("SELECT * FROM films WHERE rating >= :minRating ORDER BY rating DESC")
    suspend fun getHighRatedFilms(minRating: Double): List<Film>

    @Query("SELECT * FROM films ORDER BY year DESC LIMIT :limit")
    suspend fun getRecentFilms(limit: Int): List<Film>
}