package com.example.moviesearch.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "Создание таблицы films_table")
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY," +
                    "$COLUMN_TITLE TEXT," +
                    "$COLUMN_ORIGINAL_TITLE TEXT," +
                    "$COLUMN_ALTERNATIVE_NAME TEXT," +
                    "$COLUMN_YEAR INTEGER," +
                    "$COLUMN_DESCRIPTION TEXT," +
                    "$COLUMN_RATING REAL," +
                    "$COLUMN_POSTER_URL TEXT," +
                    "$COLUMN_GENRES TEXT);"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "Обновление БД с $oldVersion на $newVersion")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        Log.d("DatabaseHelper", "БД открыта")
    }

    companion object {
        private const val DATABASE_NAME = "films.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "films_table"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ORIGINAL_TITLE = "original_title"
        const val COLUMN_ALTERNATIVE_NAME = "alternative_name"
        const val COLUMN_YEAR = "year"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_RATING = "rating"
        const val COLUMN_POSTER_URL = "poster_url"
        const val COLUMN_GENRES = "genres"
    }
}