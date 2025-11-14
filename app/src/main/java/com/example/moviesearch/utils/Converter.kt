package com.example.moviesearch.utils

import com.example.moviesearch.data.dto.KinopoiskFilmDto
import com.example.moviesearch.data.entity.Film

object Converter {
    fun convertApiListToDtoList(kinopoiskFilms: List<KinopoiskFilmDto>?): List<Film> {
        return kinopoiskFilms?.map { kinopoiskFilm ->
            Film(
                id = kinopoiskFilm.id,
                title = kinopoiskFilm.name ?: kinopoiskFilm.alternativeName ?: "Неизвестно",
                originalTitle = kinopoiskFilm.alternativeName,
                alternativeName = kinopoiskFilm.alternativeName,
                year = kinopoiskFilm.year,
                description = kinopoiskFilm.description ?: "Описание отсутствует",
                rating = kinopoiskFilm.rating?.kp,
                posterUrl = kinopoiskFilm.poster?.url,
                genres = kinopoiskFilm.genres?.map { it.name } ?: emptyList()
            )
        } ?: emptyList()
    }
}