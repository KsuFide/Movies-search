package com.example.moviesearch

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Film(
    val title: String,
    @DrawableRes val poster: Int,
    val description: String,
    var isInFavorites: Boolean = false
) : Parcelable