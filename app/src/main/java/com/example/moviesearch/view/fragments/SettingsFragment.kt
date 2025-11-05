package com.example.moviesearch.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.moviesearch.R
import com.example.moviesearch.databinding.FragmentSettingsBinding
import com.example.moviesearch.utils.AnimationHelper
import com.example.moviesearch.viewmodel.SettingsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnimationHelper.performFragmentCircularRevealAnimation(binding.settingsFragmentRoot, requireActivity(), 5)
        binding.settingsFragmentRoot.visibility = View.VISIBLE

        // Слушаем текущую категорию
        viewModel.categoryPropertyLiveData.observe(viewLifecycleOwner) { category ->
            Log.d("SettingsFragment", "📊 Текущая категория: $category")
            when(category) {
                POPULAR_CATEGORY -> {
                    binding.radioGroup.check(R.id.radio_popular)
                    showCategoryDescription("Популярные фильмы последних лет")
                }
                TOP_RATED_CATEGORY -> {
                    binding.radioGroup.check(R.id.radio_top_rated)
                    showCategoryDescription("Фильмы с высоким рейтингом")
                }
                RECENT_CATEGORY -> {
                    binding.radioGroup.check(R.id.radio_recent)
                    showCategoryDescription("Самые новые фильмы")
                }
            }
        }

        // Слушатель для смены категории
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.radio_popular -> {
                    Log.d("SettingsFragment", "🔄 Устанавливаем категорию: популярные")
                    viewModel.putCategoryProperty(POPULAR_CATEGORY)
                    showToast("Установлена категория: Популярные")
                }
                R.id.radio_top_rated -> {
                    Log.d("SettingsFragment", "🔄 Устанавливаем категорию: высокий рейтинг")
                    viewModel.putCategoryProperty(TOP_RATED_CATEGORY)
                    showToast("Установлена категория: Высокий рейтинг")
                }
                R.id.radio_recent -> {
                    Log.d("SettingsFragment", "🔄 Устанавливаем категорию: новинки")
                    viewModel.putCategoryProperty(RECENT_CATEGORY)
                    showToast("Установлена категория: Новинки")
                }
            }
        }
    }

    private fun showCategoryDescription(description: String) {
        // Можно добавить отображение описания
        Log.d("SettingsFragment", "📝 Описание категории: $description")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.d("SettingsFragment", "🔔 Toast: $message")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val POPULAR_CATEGORY = "popular"
        const val TOP_RATED_CATEGORY = "top_rated"
        const val RECENT_CATEGORY = "recent"
    }
}