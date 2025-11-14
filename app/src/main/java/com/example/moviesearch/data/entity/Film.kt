package com.example.moviesearch.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.moviesearch.data.db.Converters
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "films")
data class Film(
    @PrimaryKey
    val id: Int,
    val title: String,
    val originalTitle: String? = null,
    val alternativeName: String? = null,
    val year: Int? = null,
    val rating: Double? = null,
    val posterUrl: String? = null,
    val description: String? = null,

    @TypeConverters(Converters::class)
    val genres: List<String>? = null,

    @TypeConverters(Converters::class)
    val countries: List<String>? = null,

    val isFavorite: Boolean = false,
    val isInFavorites: Boolean = false,

    val isWatchLater: Boolean = false,
    val isWatched: Boolean = false,
    val watchDate: Long? = null
) : Parcelable