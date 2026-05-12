package com.example.nutriai.ui.calories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nutriai.databinding.FragmentCaloriesBinding

class CaloriesFragment : Fragment() {

    private var _binding: FragmentCaloriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaloriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCalculate.setOnClickListener {
            val weight = binding.etWeight.text.toString().toDoubleOrNull()
            val height = binding.etHeight.text.toString().toDoubleOrNull()
            val age = binding.etAge.text.toString().toIntOrNull()

            if (weight == null || height == null || age == null) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val goalId = binding.radioGoal.checkedRadioButtonId
            if (goalId == -1) {
                Toast.makeText(requireContext(), "Please select a goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bmr = 10 * weight + 6.25 * height - 5 * age + 5
            val tdee = bmr * 1.55

            val calories = when (goalId) {
                binding.rbLose.id -> tdee - 500
                binding.rbGain.id -> tdee + 500
                else -> tdee
            }

            val protein = (calories * 0.30 / 4).toInt()
            val carbs = (calories * 0.45 / 4).toInt()
            val fat = (calories * 0.25 / 9).toInt()

            binding.tvCalorieResult.text = """
                Daily Target: ${calories.toInt()} kcal
                
                Protein: ${protein}g
                Carbohydrates: ${carbs}g
                Fat: ${fat}g
            """.trimIndent()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}