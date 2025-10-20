package com.example.moviesearch.domain

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Film(
    val id: Int,
    val title: String,
    val originalTitle: String? = null,
    val alternativeName: String? = null,
    val year: Int? = null,
    val description: String? = null,
    val rating: Double? = null,
    val posterUrl: String? = null,
    val genres: List<String> = emptyList(),
    var isInFavorites: Boolean = false
) : Parcelable