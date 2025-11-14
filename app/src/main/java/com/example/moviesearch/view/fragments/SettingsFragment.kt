package com.example.moviesearch.view.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.moviesearch.R
import com.example.moviesearch.databinding.FragmentSettingsBinding
import com.example.moviesearch.domain.Interactor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var interactor: Interactor

    private var isCategoryChanging = false
    private var lastSelectedCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySelection()
        setupClearCacheButton()
        setupFilmCountDisplay()
        setupCacheInfoDisplay()
    }

    private fun setupCategorySelection() {
        val currentCategory = interactor.getDefaultCategoryFromPreferences()
        lastSelectedCategory = currentCategory

        binding.radioGroup.removeAllViews()

        val categories = interactor.getAvailableCategories()
        categories.forEach { (key, name) ->
            val radioButton = android.widget.RadioButton(requireContext()).apply {
                text = name
                id = View.generateViewId()
                tag = key
                isChecked = (key == currentCategory)

                // ТЕМНЫЙ ТЕКСТ ДЛЯ КАТЕГОРИЙ
                setTextColor(Color.parseColor("#2D1B16")) // Очень темный коричневый
                textSize = 16f

                // ТЕМНЫЕ КРУЖКИ РАДИОКНОПОК
                val colorStateList = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked), // Невыбранное состояние
                        intArrayOf(android.R.attr.state_checked)   // Выбранное состояние
                    ),
                    intArrayOf(
                        Color.parseColor("#5D4037"), // Темно-коричневый для невыбранного
                        Color.parseColor("#3E2723")  // Очень темный коричневый для выбранного
                    )
                )
                buttonTintList = colorStateList
            }
            binding.radioGroup.addView(radioButton)
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (isCategoryChanging) {
                Log.d("SettingsFragment", "⏸️ Игнорируем множественное нажатие")
                return@setOnCheckedChangeListener
            }

            val selectedRadio = group.findViewById<android.widget.RadioButton>(checkedId)
            val category = selectedRadio.tag as String
            val categoryName = selectedRadio.text.toString()

            if (category == lastSelectedCategory) {
                Log.d("SettingsFragment", "ℹ️ Категория не изменилась: $category")
                return@setOnCheckedChangeListener
            }

            isCategoryChanging = true
            lastSelectedCategory = category

            binding.categoryChangeProgress.visibility = View.VISIBLE
            binding.clearCacheButton.isEnabled = false

            Log.d("SettingsFragment", "🔄 Начинаем смену категории на: $category")

            interactor.saveDefaultCategoryToPreferences(category)
            Toast.makeText(requireContext(), "Категория изменена: $categoryName", Toast.LENGTH_SHORT).show()
            Log.d("SettingsFragment", "✅ Категория изменена на: $category ($categoryName)")

            binding.radioGroup.postDelayed({
                isCategoryChanging = false
                binding.categoryChangeProgress.visibility = View.GONE
                binding.clearCacheButton.isEnabled = true
                Log.d("SettingsFragment", "🔄 Сброс защиты от множественных нажатий")
            }, 1000)
        }
    }

    private fun setupClearCacheButton() {
        binding.clearCacheButton.setOnClickListener {
            if (isCategoryChanging) {
                Toast.makeText(requireContext(), "Подождите, идет смена категории...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    interactor.deleteAllFilmsFromDb()
                    Toast.makeText(requireContext(), "Кэш очищен", Toast.LENGTH_SHORT).show()
                    updateFilmCountDisplay()
                    updateCacheInfoDisplay()
                    Log.d("SettingsFragment", "🗑️ Кэш успешно очищен")
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Ошибка при очистке кэша: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SettingsFragment", "Ошибка при очистке кэша", e)
                }
            }
        }
    }

    private fun setupFilmCountDisplay() {
        updateFilmCountDisplay()
    }

    private fun setupCacheInfoDisplay() {
        updateCacheInfoDisplay()
    }

    private fun updateFilmCountDisplay() {
        lifecycleScope.launch {
            try {
                val count = interactor.getFilmsCount()
                binding.filmCountText.text = "Фильмов в кэше: $count"
                Log.d("SettingsFragment", "📊 Количество фильмов в кэше: $count")
            } catch (e: Exception) {
                binding.filmCountText.text = "Ошибка загрузки счетчика"
                Log.e("SettingsFragment", "Ошибка при получении количества фильмов", e)
            }
        }
    }

    private fun updateCacheInfoDisplay() {
        val cacheInfo = interactor.getCacheInfo()
        binding.cacheInfoText.text = cacheInfo
        Log.d("SettingsFragment", "💾 Информация о кэше: $cacheInfo")
    }

    override fun onResume() {
        super.onResume()
        updateFilmCountDisplay()
        updateCacheInfoDisplay()
        isCategoryChanging = false
        binding.categoryChangeProgress.visibility = View.GONE
        binding.clearCacheButton.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        isCategoryChanging = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}