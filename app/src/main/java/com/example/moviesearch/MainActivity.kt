package com.example.moviesearch

import android.R.attr.fragment
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.moviesearch.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    // View Binding для доступа к элементам интерфейса
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupToolbar()


        // Добавляем HomeFragment при старте
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, HomeFragment())
                .commit()
        }
    }

    fun launchDetailsFragment(film: Film) {
        // Создаём "посылку"
        // Кладём фрагмент с деталями в переменную
        val fragment = DetailsFragment().apply {
            // Прикрепляем нашу "посылку" к фрагменту
            arguments = Bundle().apply {
                // Кладём наш фильм в "посылку"
                putParcelable("film", film)
            }
        }

        //Запускаем фрагмент
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_placeholder, fragment)
            .addToBackStack(null) // Клавиша назад
            .commit()
    }

    // Настройка BottomNavigationView
    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            // Установка активного и неактивного цветов программно
            itemIconTintList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.white),
                    ContextCompat.getColor(context, R.color.button)
                )
            )

            // Обработка выбора пунктов меню
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.favorites -> showSnackbar("Избранное")
                    R.id.watch_later -> showSnackbar("Посмотреть позже")
                    R.id.selections -> showSnackbar("Подборки")
                }
                true // Возвращаем true для подтверждения выбора
            }
        }
    }

    // Инициализация ToolBar
    private fun setupToolbar() {
        binding.appBarLayout.findViewById<Toolbar>(R.id.toolbar).apply {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.settings -> Toast.makeText(context, "Настройки", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }

    // Показ всплывающих уведомлений
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Если фрагменты в стеке, стандартная обработка
            super.onBackPressed()
        } else {
            // Если стек пуст, показать диалог выхода
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(ContextThemeWrapper(this, R.style.MyDialog))
            .setTitle("Вы хотите выйти?")
            .setIcon(R.drawable.ic_menu_gallery)
            .setMessage("Нам не хотелось бы, чтобы вы уходили")
            .setPositiveButton("Да") { _, _ ->
                finish()
            }
            .setNegativeButton("Нет") { _, _ ->

            }
            .setNeutralButton("Не знаю") { _, _ ->
                Toast.makeText(this, "Решайся", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}






