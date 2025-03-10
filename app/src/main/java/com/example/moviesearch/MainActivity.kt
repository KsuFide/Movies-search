package com.example.moviesearch

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    //Создаём список фильмов
    private val filmsDataBase = listOf(
        Film(
            "Парк Юрского периода",
            R.drawable.jurassic_park,
            "Ученые клонируют динозавров для тематического парка, но что-то идет не так..."
        ),
        Film(
            "Человек-паук",
            R.drawable.spider_man,
            "Питер Паркер обретает суперспособности после укуса радиоактивного паука"
        ),
        Film(
            "Мумия",
            R.drawable.the_mummy,
            "Археологи пробуждают древнее проклятие во время раскопок в Египте"
        ),
        Film(
            "Человек-волк",
            R.drawable.wolf_man,
            "Проклятие превращает человека в оборотня во время полнолуния"
        ),
        Film(
            "Флэш",
            R.drawable.flash,
            "Барри Аллен получает сверхчеловеческую скорость после аварии с ускорителем частиц"
        ),
        Film(
            "Мстители",
            R.drawable.avengers,
            "Команда супергероев объединяется, чтобы спасти Землю от Локи и инопланетного вторжения"
        ),
        Film(
            "1+1",
            R.drawable.the_intouchables,
            "Неожиданная дружба между богатым инвалидом и уличным парнем из трущоб"
        ),
        Film(
            "Бойцовский клуб",
            R.drawable.fight_club,
            "Офисный работник и мыловар создают подпольную организацию для психологической разрядки"
        ),
        Film(
            "Зелёная миля",
            R.drawable.green_mile,
            "История надзирателя смертников и заключённого с необычным даром"
        ),
        Film(
            "Зелёная книга",
            R.drawable.green_book,
            "Путешествие афроамериканского пианиста и итальянского водителя по югу США 1960-х"
        ),
        Film(
            "Форрест Гамп",
            R.drawable.forrest_gump,
            "Простой парень становится невольным участником ключевых событий американской истории"
        ),
        Film(
            "Гарри Поттер",
            R.drawable.harry_potter,
            "Мальчик-сирота узнаёт, что он волшебник, и поступает в школу Хогвартс"
        ),
        Film(
            "Властелин колец",
            R.drawable.lord_of_rings,
            "Эпическое путешествие хоббита Фродо для уничтожения Кольца Всевластия"
        ),
        Film(
            "Интерстеллар",
            R.drawable.interstellar,
            "Космическая экспедиция сквозь червоточину в поисках нового дома для человечества"
        ),
        Film(
            "Остров проклятых",
            R.drawable.shutter_island,
            "Детектив расследует исчезновение пациентки психиатрической больницы на отдалённом острове"
        ),
        Film(
            "Джентльмены",
            R.drawable.gentlemen,
            "Криминальный босс пытается продать свой наркобизнес, но сталкивается с проблемами"
        ),
        Film(
            "История игрушек",
            R.drawable.toy_story,
            "Игрушки оживают, когда людей нет рядом, и переживают удивительные приключения"
        ),
        Film(
            "Побег из Шоушенка",
            R.drawable.shawshank_redemption,
            "Несправедливо осуждённый банкир Энди Дюфрейн 20 лет планирует побег из тюрьмы, сохраняя надежду и человечность"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = filmsAdapter
            addItemDecoration(TopSpacingItemDecoration(8))
        }


        // Инициализация BottomNavigationView
        val bottomNavigationView = binding.bottomNavigation
        // Установка активного и неактивного цветов программно
        val colorStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // Активное состояние
                intArrayOf(-android.R.attr.state_checked) // Неактивное
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.white), // Активный цвет
                ContextCompat.getColor(this, R.color.button) // Неактивный цвет
            )

        )
        bottomNavigationView.itemIconTintList = colorStates
        bottomNavigationView.itemTextColor = colorStates
        bottomNavigationView.setOnItemSelectedListener { item ->
            val view = findViewById<View>(item.itemId)
            view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationYBy(-20f)
                .setDuration(500)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationYBy(20f)
                        .setDuration(500)
                }
                .start()

            when (item.itemId) {
                R.id.favorites -> {
                    Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.watch_later -> {
                    Toast.makeText(this, "Посмотреть позже", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.selections -> {
                    Toast.makeText(this, "Подборки", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        // Инициализация ToolBar
        val topAppBar = binding.appBarLayout
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    // Настройка кликов и анимации для CardView
    private fun setupPosterClick(cardView: CardView) {
        // Добавляем обработчик клика
        cardView.setOnClickListener { poster ->
            applyComboAnimation(poster) // Комбо-анимация
            Toast.makeText(this, "Клик по карточке!", Toast.LENGTH_SHORT).show()
        }
    }

    // Реализация метода applyComboAnimation
    private fun applyComboAnimation(view: View) {
        // Создаём анимации
        // Анимация увеличения и затемнения
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f).apply { duration = 1000 }
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f).apply { duration = 1000 }
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f).apply { duration = 1000 }

        // Анимация уменьшения и восстановления яркости
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f).apply { duration = 1000 }
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f).apply { duration = 1000 }
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f).apply { duration = 1000 }

        AnimatorSet().apply {
            // Сначала запускаем увеличение и затемнение вместе
            play(scaleUpX).with(scaleUpY).with(fadeOut)
            // Затем, после их завершения, запускаем уменьшение и восстановление
            play(scaleDownX).with(scaleDownY).with(fadeIn).after(scaleUpX)
            start()
        }
    }
}
