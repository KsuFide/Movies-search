package com.example.moviesearch.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.moviesearch.data.db.DatabaseHelper
import com.example.moviesearch.domain.Film

class MainRepository {
    private var databaseHelper: DatabaseHelper? = null
    private var sqlDb: SQLiteDatabase? = null

    private val lock = Any()

    var filmsDataBase = mutableListOf<Film>()
        private set

    // Правильная инициализация БД
    private fun initDatabase(context: android.content.Context) {
        synchronized(lock) {
            if (databaseHelper == null) {
                Log.d("MainRepository", "🔄 Инициализация DatabaseHelper")
                databaseHelper = DatabaseHelper(context.applicationContext)
            }
            if (sqlDb == null || !sqlDb!!.isOpen) {
                Log.d("MainRepository", "🔄 Открытие базы данных")
                sqlDb = databaseHelper!!.writableDatabase
            }
        }
    }

    // Закрытие БД
    fun closeDatabase() {
        synchronized(lock) {
            sqlDb?.close()
            sqlDb = null
            databaseHelper?.close()
            databaseHelper = null
            Log.d("MainRepository", "🔒 База данных закрыта")
        }
    }

    fun updateFilms(newFilms: List<Film>) {
        filmsDataBase.clear()
        filmsDataBase.addAll(newFilms)
    }

    fun getFilms(): List<Film> = filmsDataBase.toList()

    // Добавляем фильмы в БД
    fun putToDb(films: List<Film>, context: android.content.Context) {
        synchronized(lock) {
            try {
                initDatabase(context)
                films.forEach { film ->
                    val cv = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_ID, film.id)
                        put(DatabaseHelper.COLUMN_TITLE, film.title)
                        put(DatabaseHelper.COLUMN_ORIGINAL_TITLE, film.originalTitle)
                        put(DatabaseHelper.COLUMN_ALTERNATIVE_NAME, film.alternativeName)
                        put(DatabaseHelper.COLUMN_YEAR, film.year)
                        put(DatabaseHelper.COLUMN_DESCRIPTION, film.description)
                        put(DatabaseHelper.COLUMN_RATING, film.rating)
                        put(DatabaseHelper.COLUMN_POSTER_URL, film.posterUrl)
                        put(DatabaseHelper.COLUMN_GENRES, film.genres.joinToString(","))
                    }
                    sqlDb?.insertWithOnConflict(
                        DatabaseHelper.TABLE_NAME,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
                Log.d("MainRepository", "✅ Сохранено ${films.size} фильмов в БД")
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при сохранении в БД: ${e.message}", e)
            }
        }
    }

    // Получаем все фильмы из БД
    fun getAllFromDB(context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_NAME}", null)
                val result = mutableListOf<Film>()

                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                Log.d("MainRepository", "📥 Загружено ${result.size} фильмов из БД")
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при загрузке из БД: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Получаем количество фильмов в БД
    fun getFilmsCount(context: android.content.Context): Int {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_NAME}", null)
                var count = 0

                cursor?.use {
                    if (it.moveToFirst()) {
                        count = it.getInt(0)
                    }
                }
                Log.d("MainRepository", "📊 Количество фильмов в БД: $count")
                count
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при подсчете фильмов: ${e.message}", e)
                0
            }
        }
    }

    // Получаем фильм по ID
    fun getFilmById(id: Int, context: android.content.Context): Film? {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_ID} = ?",
                    arrayOf(id.toString())
                )

                var film: Film? = null
                cursor?.use {
                    if (it.moveToFirst()) {
                        film = createFilmFromCursor(it)
                    }
                }
                film
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске фильма по ID: ${e.message}", e)
                null
            }
        }
    }

    // Обновляем фильм в БД
    fun updateFilmInDb(film: Film, context: android.content.Context) {
        synchronized(lock) {
            try {
                initDatabase(context)
                val cv = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_TITLE, film.title)
                    put(DatabaseHelper.COLUMN_ORIGINAL_TITLE, film.originalTitle)
                    put(DatabaseHelper.COLUMN_ALTERNATIVE_NAME, film.alternativeName)
                    put(DatabaseHelper.COLUMN_YEAR, film.year)
                    put(DatabaseHelper.COLUMN_DESCRIPTION, film.description)
                    put(DatabaseHelper.COLUMN_RATING, film.rating)
                    put(DatabaseHelper.COLUMN_POSTER_URL, film.posterUrl)
                    put(DatabaseHelper.COLUMN_GENRES, film.genres.joinToString(","))
                }

                sqlDb?.update(
                    DatabaseHelper.TABLE_NAME,
                    cv,
                    "${DatabaseHelper.COLUMN_ID} = ?",
                    arrayOf(film.id.toString())
                )
                Log.d("MainRepository", "✅ Фильм ${film.id} обновлен в БД")
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при обновлении фильма: ${e.message}", e)
            }
        }
    }

    // Удаляем фильм из БД по ID
    fun deleteFilmFromDb(filmId: Int, context: android.content.Context) {
        synchronized(lock) {
            try {
                initDatabase(context)
                sqlDb?.delete(
                    DatabaseHelper.TABLE_NAME,
                    "${DatabaseHelper.COLUMN_ID} = ?",
                    arrayOf(filmId.toString())
                )
                Log.d("MainRepository", "🗑️ Фильм $filmId удален из БД")
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при удалении фильма: ${e.message}", e)
            }
        }
    }

    // Удаляем все фильмы из БД
    fun deleteAllFilmsFromDb(context: android.content.Context) {
        synchronized(lock) {
            try {
                initDatabase(context)
                sqlDb?.delete(DatabaseHelper.TABLE_NAME, null, null)
                Log.d("MainRepository", "🗑️ Все фильмы удалены из БД")
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при удалении всех фильмов: ${e.message}", e)
            }
        }
    }

    // Получаем фильмы по жанру
    fun getFilmsByGenre(genre: String, context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_GENRES} LIKE ?",
                    arrayOf("%$genre%")
                )

                val result = mutableListOf<Film>()
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске по жанру: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Получаем фильмы с высоким рейтингом
    fun getHighRatedFilms(minRating: Double = 7.0, context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_RATING} >= ? ORDER BY ${DatabaseHelper.COLUMN_RATING} DESC",
                    arrayOf(minRating.toString())
                )

                val result = mutableListOf<Film>()
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске по рейтингу: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Получаем последние добавленные фильмы
    fun getRecentFilms(limit: Int = 20, context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} ORDER BY ${DatabaseHelper.COLUMN_YEAR} DESC LIMIT ?",
                    arrayOf(limit.toString())
                )

                val result = mutableListOf<Film>()
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске последних фильмов: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Получаем фильмы по диапазону годов
    fun getFilmsByYearRange(startYear: Int, endYear: Int, context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_YEAR} BETWEEN ? AND ? ORDER BY ${DatabaseHelper.COLUMN_YEAR} DESC",
                    arrayOf(startYear.toString(), endYear.toString())
                )

                val result = mutableListOf<Film>()
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                Log.d("MainRepository", "📅 Найдено ${result.size} фильмов с $startYear по $endYear год")
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске по годам: ${e.message}", e)
                emptyList()
            }
        }
    }

    // Получаем фильмы по названию (поиск)
    fun getFilmsByTitle(title: String, context: android.content.Context): List<Film> {
        synchronized(lock) {
            return try {
                initDatabase(context)
                val cursor = sqlDb?.rawQuery(
                    "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_TITLE} LIKE ? OR ${DatabaseHelper.COLUMN_ORIGINAL_TITLE} LIKE ? OR ${DatabaseHelper.COLUMN_ALTERNATIVE_NAME} LIKE ?",
                    arrayOf("%$title%", "%$title%", "%$title%")
                )

                val result = mutableListOf<Film>()
                cursor?.use {
                    if (it.moveToFirst()) {
                        do {
                            result.add(createFilmFromCursor(it))
                        } while (it.moveToNext())
                    }
                }
                Log.d("MainRepository", "🔍 Найдено ${result.size} фильмов по запросу '$title'")
                result
            } catch (e: Exception) {
                Log.e("MainRepository", "❌ Ошибка при поиске по названию: ${e.message}", e)
                emptyList()
            }
        }
    }

    private fun createFilmFromCursor(cursor: Cursor): Film {
        val genresString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENRES))
        val genres = if (genresString.isNotEmpty()) {
            genresString.split(",")
        } else {
            emptyList()
        }

        return Film(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
            originalTitle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORIGINAL_TITLE)),
            alternativeName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALTERNATIVE_NAME)),
            year = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_YEAR))) null
            else cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_YEAR)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
            rating = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATING))) null
            else cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATING)),
            posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POSTER_URL)),
            genres = genres
        )
    }
}