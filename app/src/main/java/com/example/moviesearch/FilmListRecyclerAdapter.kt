package com.example.moviesearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moviesearch.databinding.FilmItemBinding

// В параметр передаём слушатель, чтобы мы потом могли обрабатывать нажатия из класса Activity
class FilmListRecyclerAdapter(
    private val clickListener: (Film) -> Unit,
) : ListAdapter<Film, FilmListRecyclerAdapter.FilmViewHolder>(FilmDiffCallback()) {

    // ViewHolder для элемента списка
    inner class FilmViewHolder(val binding: FilmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(film: Film) {
            with(binding) {
                // Установка текстовых данных
                title.text = film.title
                description.text = film.description

                // Используем Glide для загрузки изображений
                Glide.with(itemView)
                    .load(film.poster) // Загружаем ресурс
                    .centerCrop()      // Центрируем и обрезаем
                    .into(poster)      // Указываем ImageView

                // Обработка клика на весь элемент
                root.setOnClickListener { clickListener(film) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        // Создание ViewHolder с помощью ViewBinding
        val binding = FilmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        // Привязка данных к ViewHolder
        holder.bind(getItem(position))

    }

    // Обновление списка фильмов
    fun addItems(items: List<Film>) {
        submitList(items)
    }
}
