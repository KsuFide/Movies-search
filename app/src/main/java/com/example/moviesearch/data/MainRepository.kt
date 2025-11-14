package com.example.moviesearch.data

import com.example.moviesearch.data.db.FilmDao
import com.example.moviesearch.data.entity.Film
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val filmDao: FilmDao
) {

    suspend fun putToDb(films: List<Film>) {
        filmDao.insertFilms(films)
    }

    suspend fun getAllFromDB(): List<Film> {
        return filmDao.getAllFilms()
    }

    suspend fun updateFilmInDb(film: Film) {
        filmDao.insertFilms(listOf(film))
    }

    suspend fun deleteFilmFromDb(filmId: Int) {
        filmDao.deleteFilm(filmId)
    }

    suspend fun deleteAllFilmsFromDb() {
        filmDao.deleteAllFilms()
    }


    suspend fun getFilmsByYearRange(startYear: Int, endYear: Int): List<Film> {
        return filmDao.getFilmsByYearRange(startYear, endYear)
    }

    suspend fun getFilmsByTitle(title: String): List<Film> {
        return filmDao.getFilmsByTitle(title)
    }

    suspend fun getFilmsCount(): Int {
        return filmDao.getFilmsCount()
    }

    suspend fun getFilmsByGenre(genre: String): List<Film> {
        return filmDao.getFilmsByGenre(genre)
    }

    suspend fun getHighRatedFilms(minRating: Double = 7.0): List<Film> {
        return filmDao.getHighRatedFilms(minRating)
    }

    suspend fun getRecentFilms(limit: Int = 20): List<Film> {
        return filmDao.getRecentFilms(limit)
    }
}