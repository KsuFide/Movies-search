package com.example.moviesearch.view

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.moviesearch.R
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.databinding.ActivityMainBinding
import com.example.moviesearch.domain.Film
import com.example.moviesearch.domain.Interactor
import com.example.moviesearch.view.fragments.CollectionsFragment
import com.example.moviesearch.view.fragments.FavoritesFragment
import com.example.moviesearch.view.fragments.HomeFragment
import com.example.moviesearch.view.fragments.WatchLaterFragment

class MainActivity : AppCompatActivity() {
    private val repo = MainRepository()
    private val interactor = Interactor(repo)

    private lateinit var binding: ActivityMainBinding
    private var lastSelectedItemId: Int = R.id.home

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Установка стартового фрагмента
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment(), "home")
                .commit()
        }

        binding.bottomNavigation.selectedItemId = R.id.home
        lastSelectedItemId = R.id.home

        // Устанавливаем цвет статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        }

        // Находим AppBarLayout по ID
        val appBarLayout = findViewById<View>(R.id.app_bar_layout)

        // СПОСОБ 1: Установка цвета через Color.parseColor (прямой HEX-код)
        appBarLayout.setBackgroundColor(Color.parseColor("#128A8D"))



        // Обработчик навигации
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Анимация иконки
            animateIcon(item)

            // Определение анимации перехода (как в старом коде)
            val (enterAnim, exitAnim) = when {
                // Home -> Favorites: вход справа, выход влево
                item.itemId == R.id.favorites && lastSelectedItemId == R.id.home ->
                    Pair(R.anim.slide_in_right, R.anim.slide_out_left)

                // Favorites -> Home: вход слева, выход вправо
                item.itemId == R.id.home && lastSelectedItemId == R.id.favorites ->
                    Pair(R.anim.slide_in_left, R.anim.slide_out_right)

                // Home -> Watch Later: вход слева, выход вправо
                item.itemId == R.id.watch_later && lastSelectedItemId == R.id.home ->
                    Pair(R.anim.slide_in_left, R.anim.slide_out_right)

                // Watch Later -> Home: вход справа, выход влево
                item.itemId == R.id.home && lastSelectedItemId == R.id.watch_later ->
                    Pair(R.anim.slide_in_right, R.anim.slide_out_left)

                // Home -> Selections: вход слева, выход вправо
                item.itemId == R.id.selections && lastSelectedItemId == R.id.home ->
                    Pair(R.anim.slide_in_left, R.anim.slide_out_right)

                // Selections -> Home: вход справа, выход влево
                item.itemId == R.id.home && lastSelectedItemId == R.id.selections ->
                    Pair(R.anim.slide_in_right, R.anim.slide_out_left)

                // Для других случаев - плавное появление
                else -> Pair(R.anim.fade_in, R.anim.fade_out)
            }

            lastSelectedItemId = item.itemId

            // Обработка выбора пункта меню
            when (item.itemId) {
                R.id.favorites -> {
                    val tag = "favorites"
                    val fragment = checkFragmentExistence(tag) ?: FavoritesFragment()
                    changeFragment(fragment, tag, enterAnim, exitAnim)
                    true
                }

                R.id.home -> {
                    val tag = "home"
                    val fragment = checkFragmentExistence(tag) ?: HomeFragment()
                    changeFragment(fragment, tag, enterAnim, exitAnim)
                    true
                }

                R.id.watch_later -> {
                    val tag = "watch_later"
                    val fragment = checkFragmentExistence(tag) ?: WatchLaterFragment()
                    changeFragment(fragment, tag, enterAnim, exitAnim)
                    true
                }

                R.id.selections -> {
                    val tag = "selections"
                    val fragment = checkFragmentExistence(tag) ?: CollectionsFragment()
                    changeFragment(fragment, tag, enterAnim, exitAnim)
                    true
                }

                else -> false
            }
        }
    }

    // Ищем фрагмент по тегу, если он есть то возвращаем его, если нет, то null
    private fun checkFragmentExistence(tag: String): Fragment? =
        supportFragmentManager.findFragmentByTag(tag)

    // Сам запуск фрагмента с анимациями
    private fun changeFragment(fragment: Fragment, tag: String, enterAnim: Int, exitAnim: Int) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enterAnim, exitAnim)
            .replace(R.id.main_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    // Запуск экрана деталей фильма
    fun launchDetailsActivity(film: Film) {
        startActivity(Intent(this, DetailsActivity::class.java).apply {
            putExtra("film", film)
        })
    }



    // Анимация иконки при нажатии
    private fun animateIcon(item: MenuItem) {
        val view = binding.bottomNavigation.findViewById<View>(item.itemId)
        view?.animate()
            ?.scaleX(0.8f)
            ?.scaleY(0.8f)
            ?.translationYBy(-20f)
            ?.setDuration(500)
            ?.withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationYBy(20f)
                    .setDuration(500)
                    .start()
            }
            ?.start()
    }
}