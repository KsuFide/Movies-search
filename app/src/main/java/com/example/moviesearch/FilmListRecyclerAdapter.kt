package com.example.moviesearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.databinding.FilmItemBinding

// В параметр передаём слушатель, чтобы мы потом могли обрабатывать нажатия из класса Activity
class FilmListRecyclerAdapter(
    private val clickListener: (Film) -> Unit,
) : ListAdapter<Film, FilmListRecyclerAdapter.FilmViewHolder>(FilmDiffCallback()) {

    inner class FilmViewHolder(val binding: FilmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(film: Film) {
            with(binding) {
                // Устанавливаем заголовок
                title.text = film.title
                // Устанавливаем постер
                poster.setImageResource(film.poster)
                // Устанавливаем описание
                description.text = film.description

                root.setOnClickListener { clickListener(film) }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val binding = FilmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    fun addItems(items: List<Film>) {
        submitList(items)
    }
}
