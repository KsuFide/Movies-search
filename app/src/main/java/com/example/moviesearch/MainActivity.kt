package com.example.moviesearch

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Вызываем метод для инициализации кнопок
        initMenuButtons()
    }

    private fun initMenuButtons() {
        // Находим кнопку по id
        val button1 = findViewById<Button>(R.id.button1)
        // Устанавливаем обработчик нажатий
        button1.setOnClickListener {
            // Показываем Toast-сообщение
            Toast.makeText(this, "Меню", Toast.LENGTH_SHORT).show()
        }

        // Находим кнопку по id
        val button2 = findViewById<Button>(R.id.button2)
        // Устанавливаем обработчик нажатий
        button2.setOnClickListener {
            // Показываем Toast-сообщение
            Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()


            // Находим кнопку по id
            val button3 = findViewById<Button>(R.id.button3)
            // Устанавливаем обработчик нажатий
            button3.setOnClickListener {
                // Показываем Toast-сообщение
                Toast.makeText(this, "Посмотреть позже", Toast.LENGTH_SHORT).show()

            }

            // Находим кнопку по id
            val button4 = findViewById<Button>(R.id.button4)
            // Устанавливаем обработчик нажатий
            button4.setOnClickListener {
                // Показываем Toast-сообщение
                Toast.makeText(this, "Подборки", Toast.LENGTH_SHORT).show()

            }

            // Находим кнопку по id
            val button5 = findViewById<Button>(R.id.button5)
            // Устанавливаем обработчик нажатий
            button5.setOnClickListener {
                // Показываем Toast-сообщение
                Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()

            }
        }
    }
}