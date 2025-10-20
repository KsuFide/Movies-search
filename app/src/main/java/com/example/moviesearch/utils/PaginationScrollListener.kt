package com.example.moviesearch.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PaginationScrollListener(
    private val layoutManager: LinearLayoutManager,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {

    private val visibleThreshold = 5
    private var isLoading = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy <= 0) return // Прокрутка вверх - не загружаем

        val totalItemCount = layoutManager.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        if (!isLoading && totalItemCount <= (lastVisibleItemPosition + visibleThreshold)) {
            isLoading = true
            loadMore()
        }
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
    }
}