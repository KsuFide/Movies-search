package com.example.moviesearch

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TopSpacingItemDecoration (private val paddingInDp: Int): RecyclerView.ItemDecoration() {

    // Конвертация dp в пиксели
    private val Int.convertPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        // Установка отступов для элементов:
        outRect.top = paddingInDp.convertPx    // Верхний отступ
        outRect.right = paddingInDp.convertPx   // Правый отступ
        outRect.left = paddingInDp.convertPx    // Левый отступ
    }
}