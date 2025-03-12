package com.example.moviesearch

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.databinding.FilmItemBinding

// В параметр передаём слушатель, чтобы мы потом могли обрабатывать нажатия из класса Activity
class FilmListRecyclerAdapter(
    private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<FilmListRecyclerAdapter.FilmViewHolder>() {

        // Здесь у нас хранится список элементов для RV
    private val items = mutableListOf<Film>()

    // Интерфейс для обработки кликов
    interface OnItemClickListener {
        fun click(film: Film)
    }

    // В этом методе мы привязываем наш ViewHolder и передаём туда "надутую" верстку нашего фильма
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
       val binding = FilmItemBinding.inflate(
           LayoutInflater.from(parent.context),
           parent,
           false
       )
        return FilmViewHolder(binding)
    }

    // В этом методе будет привязка полей из объекта Film к View из film.item.xml
    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        // Проверяем какой у нас ViewHolder
        when (holder) {
            is FilmViewHolder -> {
                // Вызываем метод bind(), который мы создали, и передаём туда объект
                // из нашей базы данных с указанием позиции
                holder.bind(items[position])
                // Обрабатываем нажатие на весь элемент целиком(можно сделать на отдельный элемент
                // например, картинку) и вызываем метод нашего листенера, который мы получаем из
                // конструктора адаптера
                holder.itemView.setOnClickListener {
                    clickListener.click(items[position])
                }
            }
        }
    }

    override fun getItemCount() = items.size

    // Метод для добавления объектов в наш список
    fun addItems(list: List<Film>) {
        // Сначала очищаем (если не реализовать DiffUtils)
        items.clear()
        // Добавляем
        items.addAll(list)
        // Уведомляем RV, что пришёл новый список, и ему нужно заново всё "привязывать"
        notifyDataSetChanged()
    }

    // В конструктор класс передаётся layout, который мы создали(film_item.xml)
    inner class FilmViewHolder(private val binding: FilmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // В этом методе кладём данные из Film в наш View
        fun bind(film: Film) {
            with(binding) {
                // Устанавливаем заголовок
                title.text = film.title
                // Устанавливаем постер
                poster.setImageResource(film.poster)
                // Устанавливаем описание
                description.text = film.description
            }
        }
    }
}