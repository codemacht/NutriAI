package com.example.nutriai.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Theme Colors ─────────────────────────────────────────────────────────────
val BgDark      = Color(0xFF0F0F14)
val BgCard      = Color(0xFF1A1A26)
val BgInput     = Color(0xFF1A1A26)
val AccentViolet = Color(0xFF7C3AED)
val AccentAmber  = Color(0xFFEF9F27)
val TextPrimary  = Color(0xFFEEEEFF)
val TextSecondary = Color(0xFF6B6B80)
val TagVioletBg  = Color(0xFF2D1A4A)
val TagGreenBg   = Color(0xFF1A2A1A)
val TagVioletText = Color(0xFFA78BFA)
val TagGreenText  = Color(0xFF6EE7B7)
// ─────────────────────────────────────────────────────────────────────────────

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedRecipeTitles by viewModel.savedRecipeTitles.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf(viewModel.ingredientsInput) }
    
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.resetState()
            delay(1000)
            pullToRefreshState.endRefresh()
        }
    }

    // Update inputText when state is reset
    LaunchedEffect(viewModel.ingredientsInput) {
        if (viewModel.ingredientsInput.isEmpty()) {
            inputText = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "What are we cooking?",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    viewModel.ingredientsInput = it
                },
                placeholder = { Text("Chicken, rice, tomato...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgInput,
                    unfocusedContainerColor = BgInput,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = AccentViolet,
                    unfocusedBorderColor = Color(0xFF3A3A50)
                ),
                singleLine = false,
                maxLines = 3
            )

            Button(
                onClick = {
                    if (inputText.isBlank()) return@Button
                    viewModel.fetchRecipes(inputText.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                enabled = state !is RecipeState.Loading
            ) {
                Text(
                    text = if (state is RecipeState.Loading) "Searching recipes..." else "Find recipes with AI",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (val s = state) {
                is RecipeState.Idle -> {
                    // This makes the Column scrollable even when idle so pull-to-refresh works
                    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Enter ingredients and press the button",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                is RecipeState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentViolet)
                    }
                }
                is RecipeState.Error -> {
                    Text(text = s.message, color = Color(0xFFE24B4A), fontSize = 14.sp)
                }
                is RecipeState.Success -> {
                    Text(
                        text = "AI Suggestions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                isSaved = savedRecipeTitles.contains(recipe.title),
                                onToggleSave = { viewModel.toggleSaveRecipe(recipe) }
                            )
                        }
                    }
                }
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
            containerColor = BgCard,
            contentColor = AccentViolet
        )
    }
}

@Composable
fun RecipeCard(
    recipe: ParsedRecipe,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from favorites" else "Add to favorites",
                        tint = if (isSaved) AccentAmber else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (recipe.calories > 0) {
                    TagChip("${recipe.calories} kcal", TagVioletBg, TagVioletText)
                }
                if (recipe.cookTime != "—") {
                    TagChip(recipe.cookTime, TagGreenBg, TagGreenText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recipe.instructions,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TagChip(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}
