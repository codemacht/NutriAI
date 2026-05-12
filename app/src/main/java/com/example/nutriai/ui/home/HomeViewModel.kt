package com.example.nutriai.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriai.data.api.GeminiRepository
import com.example.nutriai.data.db.AppDatabase
import com.example.nutriai.data.db.RecipeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ParsedRecipe(
    val title: String,
    val calories: Int,
    val cookTime: String,
    val instructions: String,
    val rawText: String
)

sealed class RecipeState {
    object Idle : RecipeState()
    object Loading : RecipeState()
    data class Success(val recipes: List<ParsedRecipe>, val ingredients: String) : RecipeState()
    data class Error(val message: String) : RecipeState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GeminiRepository()
    private val recipeDao = AppDatabase.getDatabase(application).recipeDao()

    var ingredientsInput: String = ""

    private val _state = MutableStateFlow<RecipeState>(RecipeState.Idle)
    val state: StateFlow<RecipeState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RecipeState.Idle
        )

    // Synchronized set of saved recipe titles
    val savedRecipeTitles: StateFlow<Set<String>> = recipeDao.getAllRecipes()
        .map { list -> list.map { it.title }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun resetState() {
        ingredientsInput = ""
        _state.value = RecipeState.Idle
    }

    fun toggleSaveRecipe(recipe: ParsedRecipe) {
        viewModelScope.launch {
            val isSaved = savedRecipeTitles.value.contains(recipe.title)
            if (isSaved) {
                recipeDao.deleteRecipeByTitle(recipe.title)
            } else {
                recipeDao.saveRecipe(
                    RecipeEntity(
                        title = recipe.title,
                        ingredients = ingredientsInput,
                        instructions = recipe.instructions,
                        calories = recipe.calories,
                        mealType = "custom"
                    )
                )
            }
        }
    }

    fun fetchRecipes(ingredients: String) {
        ingredientsInput = ingredients
        _state.value = RecipeState.Loading

        viewModelScope.launch {
            val raw = repository.getRecipes(ingredients)
            if (raw.startsWith("Error")) {
                _state.value = RecipeState.Error(raw)
            } else {
                val recipes = parseRecipes(raw)
                _state.value = RecipeState.Success(recipes, ingredients)
            }
        }
    }

    private fun parseRecipes(raw: String): List<ParsedRecipe> {
        val blocks = raw.split("RECIPE_START")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(3)

        if (blocks.isEmpty()) {
            return listOf(
                ParsedRecipe(
                    title = "AI Recipes",
                    calories = extractCalories(raw),
                    cookTime = extractCookTime(raw),
                    instructions = raw,
                    rawText = raw
                )
            )
        }

        return blocks.mapIndexed { index, block ->
            val titleLine = block.lines().firstOrNull { it.startsWith("Title:", ignoreCase = true) }
                ?.replace(Regex("Title:", RegexOption.IGNORE_CASE), "")
                ?.trim()
                ?: "Recipe ${index + 1}"

            ParsedRecipe(
                title = titleLine,
                calories = extractCalories(block),
                cookTime = extractCookTime(block),
                instructions = block.trim(),
                rawText = block.trim()
            )
        }
    }

    private fun extractCalories(text: String): Int {
        val regex = Regex("(\\d+)\\s*(?:kcal|calories|cal)", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun extractCookTime(text: String): String {
        val regex = Regex("(\\d+\\s*(?:min|minutes|hour|hours))", RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1) ?: "—"
    }
}
