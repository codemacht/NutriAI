package com.example.nutriai.ui.mealplan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nutriai.data.api.GeminiRepository
import com.example.nutriai.databinding.FragmentMealPlanBinding
import kotlinx.coroutines.launch

class MealPlanFragment : Fragment() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!
    private val geminiRepository = GeminiRepository()
    private var selectedDiet = "balanced"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBalanced.setOnClickListener { selectDiet("balanced", it) }
        binding.btnHealthy.setOnClickListener { selectDiet("healthy", it) }
        binding.btnBulk.setOnClickListener { selectDiet("bulk", it) }
        binding.btnCut.setOnClickListener { selectDiet("cut / weight loss", it) }

        binding.btnGeneratePlan.setOnClickListener {
            generateMealPlan()
        }
    }

    private fun selectDiet(diet: String, clickedBtn: View) {
        selectedDiet = diet
        // Dark theme colors: Violet for active, Dark Gray for inactive
        val activeColor = Color.parseColor("#7C3AED")
        val inactiveColor = Color.parseColor("#1A1A26")
        val inactiveTextColor = Color.parseColor("#6B6B80")
        val activeTextColor = Color.WHITE

        listOf(binding.btnBalanced, binding.btnHealthy, binding.btnBulk, binding.btnCut)
            .forEach { 
                it.backgroundTintList = ColorStateList.valueOf(inactiveColor)
                it.setTextColor(inactiveTextColor)
            }
        
        (clickedBtn as Button).apply {
            backgroundTintList = ColorStateList.valueOf(activeColor)
            setTextColor(activeTextColor)
        }
    }

    private fun generateMealPlan() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvMealPlan.text = ""
        binding.btnGeneratePlan.isEnabled = false

        lifecycleScope.launch {
            val result = geminiRepository.getMealPlan(selectedDiet)
            binding.progressBar.visibility = View.GONE
            binding.btnGeneratePlan.isEnabled = true
            binding.tvMealPlan.text = result
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}