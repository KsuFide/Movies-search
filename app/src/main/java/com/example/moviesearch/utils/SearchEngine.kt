import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.moviesearch.domain.Film

object SearchEngine {

    /**
     * Умный поиск фильмов по названию
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun smartFilmSearch(films: List<Film>, query: String): List<Film> {
        if (query.length < 2) return films
        if (films.isEmpty()) return emptyList()

        val normalizedQuery = normalizeQuery(query)

        Log.d("SearchEngine", "🔍 Поиск '$normalizedQuery' в ${films.size} фильмах")

        val results = films.filter { film ->
            isFilmMatchingQuery(film, normalizedQuery)
        }.sortedByDescending { film ->
            calculateRelevanceScore(film, normalizedQuery)
        }

        Log.d("SearchEngine", "✅ Найдено ${results.size} результатов")
        results.take(5).forEachIndexed { index, film ->
            Log.d(
                "SearchEngine",
                "   ${index + 1}. '${film.title}' (релевантность: ${
                    calculateRelevanceScore(
                        film,
                        normalizedQuery
                    )
                })"
            )
        }

        return results
    }

    private fun normalizeQuery(query: String): String {
        return query.trim()
            .lowercase()
            .replace("[^а-яёa-z0-9]".toRegex(), "") // Убираем спецсимволы
    }

    private fun isFilmMatchingQuery(film: Film, query: String): Boolean {
        // Все названия для поиска
        val searchFields = listOf(
            film.title,
            film.originalTitle,
            film.alternativeName
        ).filterNotNull()

        return searchFields.any { field ->
            val normalizedField = normalizeField(field)

            // Разные стратегии поиска
            normalizedField.contains(query) || // Частичное совпадение
                    field.contains(query, ignoreCase = true) || // Простой поиск без нормализации
                    startsWithAnyWord(field, query) || // Начинается с любого слова
                    containsAsSubstring(field, query) // Содержит как подстроку
        }
    }

    private fun normalizeField(field: String): String {
        return field.lowercase()
            .replace("[^а-яёa-z0-9]".toRegex(), "") // Убираем спецсимволы
    }

    private fun startsWithAnyWord(field: String, query: String): Boolean {
        val words = field.split(" ", "-", ":", ".", ",", "!", "?")
        return words.any { word ->
            word.lowercase().startsWith(query.lowercase()) && word.length >= 2
        }
    }

    private fun containsAsSubstring(field: String, query: String): Boolean {
        // Убираем пробелы и спецсимволы для поиска подстрок
        val cleanField = field.replace("[^а-яёa-z0-9]".toRegex(), "").lowercase()
        val cleanQuery = query.replace("[^а-яёa-z0-9]".toRegex(), "").lowercase()

        return cleanField.contains(cleanQuery)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateRelevanceScore(film: Film, query: String): Int {
        var score = 0
        val normalizedQuery = query.lowercase()

        // Проверяем все названия
        val titles = listOf(
            film.title to 100,
            film.originalTitle to 80,
            film.alternativeName to 60
        )

        titles.forEach { (title, baseScore) ->
            title?.let {
                val normalizedTitle = it.lowercase()

                when {
                    // Точное совпадение названия
                    normalizedTitle == normalizedQuery -> score += baseScore + 100
                    // Начинается с запроса
                    normalizedTitle.startsWith(normalizedQuery) -> score += baseScore + 50
                    // Содержит запрос
                    normalizedTitle.contains(normalizedQuery) -> score += baseScore + 20
                    // Начинается с любого слова
                    startsWithAnyWord(it, normalizedQuery) -> score += baseScore + 30
                    // Содержит как подстроку (без учета пробелов)
                    containsAsSubstring(it, normalizedQuery) -> score += baseScore + 10
                }

                // Дополнительные бонусы
                if (it.equals(normalizedQuery, ignoreCase = true)) {
                    score += 50 // Точное совпадение с учетом регистра
                }
            }
        }

        // Бонус за рейтинг
        film.rating?.let { rating ->
            when {
                rating > 8.0 -> score += 40
                rating > 7.0 -> score += 25
                rating > 6.0 -> score += 15
                rating > 5.0 -> score += 5
            }
        }

        // Бонус за актуальность
        film.year?.let { year ->
            val currentYear = java.time.Year.now().value
            when {
                year >= currentYear -> score += 30
                year >= currentYear - 2 -> score += 20
                year >= currentYear - 5 -> score += 10
            }
        }

        return score
    }

    /**
     * Детальная отладка совпадений
     */
    fun debugFilmSearch(film: Film, query: String): String {
        val normalizedQuery = query.lowercase()
        val matches = mutableListOf<String>()

        film.title?.let {
            val normalizedTitle = it.lowercase()
            when {
                normalizedTitle.contains(normalizedQuery) -> matches.add("title: '$it'")
                containsAsSubstring(it, normalizedQuery) -> matches.add("title(sub): '$it'")
                else -> {} // добавляем else ветку
            }
        }

        film.originalTitle?.let {
            val normalizedTitle = it.lowercase()
            when {
                normalizedTitle.contains(normalizedQuery) -> matches.add("originalTitle: '$it'")
                containsAsSubstring(it, normalizedQuery) -> matches.add("originalTitle(sub): '$it'")
                else -> {}
            }
        }

        film.alternativeName?.let {
            val normalizedTitle = it.lowercase()
            when {
                normalizedTitle.contains(normalizedQuery) -> matches.add("alternativeName: '$it'")
                containsAsSubstring(
                    it,
                    normalizedQuery
                ) -> matches.add("alternativeName(sub): '$it'")

                else -> {}
            }
        }

        return if (matches.isNotEmpty()) {
            "Совпадения: ${matches.joinToString(", ")}"
        } else {
            "Нет совпадений"
        }
    }
}