package com.example.moviesearch.data.dto

data class KinopoiskResponse(
    val docs: List<KinopoiskFilmDto>,
    val total: Int,
    val limit: Int,
    val page: Int,
    val pages: Int
)

data class KinopoiskFilmDto(
    val id: Int,
    val name: String?,
    val alternativeName: String?,
    val year: Int?,
    val description: String?,
    val rating: RatingDto?,
    val poster: PosterDto?, // Объект с постерами
    val genres: List<GenreDto>?
)

data class PosterDto(
    val url: String?,       // Полный URL полноразмерного постера
    val previewUrl: String? // URL постера меньшего размера
)

data class RatingDto(
    val kp: Double?,
    val imdb: Double?,
    val filmCritics: Double?,
    val russianFilmCritics: Double?,
    val await: Double?
)

data class GenreDto(
    val name: String
)