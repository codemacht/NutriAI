package com.example.nutriai.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.nutriai.data.db.AppDatabase
import com.example.nutriai.data.db.RecipeEntity
import com.example.nutriai.ui.home.AccentAmber
import com.example.nutriai.ui.home.AccentViolet
import com.example.nutriai.ui.home.BgCard
import com.example.nutriai.ui.home.BgDark
import com.example.nutriai.ui.home.TagGreenBg
import com.example.nutriai.ui.home.TagGreenText
import com.example.nutriai.ui.home.TagVioletBg
import com.example.nutriai.ui.home.TagVioletText
import com.example.nutriai.ui.home.TextPrimary
import com.example.nutriai.ui.home.TextSecondary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dao = AppDatabase.getDatabase(requireContext()).recipeDao()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FavoritesScreen(
                        recipesFlow = dao.getAllRecipes(),
                        onDelete = { recipe ->
                            lifecycleScope.launch { dao.deleteRecipe(recipe) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    recipesFlow: kotlinx.coroutines.flow.Flow<List<RecipeEntity>>,
    onDelete: (RecipeEntity) -> Unit
) {
    val recipes by recipesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Favorites",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No saved recipes yet",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                    Text(
                        "Tap ♡ on a recipe to save it",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes, key = { it.id }) { recipe ->
                    FavoriteCard(recipe = recipe, onDelete = onDelete)
                }
            }
        }
    }
}

@Composable
fun FavoriteCard(
    recipe: RecipeEntity,
    onDelete: (RecipeEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header + buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (recipe.calories > 0) {
                            Box(
                                modifier = Modifier
                                    .background(TagVioletBg, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${recipe.calories} kcal",
                                    fontSize = 11.sp,
                                    color = TagVioletText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .background(TagGreenBg, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                recipe.mealType,
                                fontSize = 11.sp,
                                color = TagGreenText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row {
                    // Expand/Collapse
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp
                            else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Delete
                    IconButton(onClick = { onDelete(recipe) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE24B4A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Instructions — shown when expanded
            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFF2A2A3A),
                    thickness = 0.5.dp
                )
                Text(
                    text = recipe.instructions,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
