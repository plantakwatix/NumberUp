package com.plantakwatix.numberup.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.plantakwatix.numberup.R
import com.plantakwatix.numberup.ui.composables.GameGrid
import com.plantakwatix.numberup.ui.composables.getDrawableResourceForValue
import com.plantakwatix.numberup.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    gameViewModel: GameViewModel = viewModel()
) {
    val gridData by gameViewModel.gridStateCompose
    val currentNumberValue by gameViewModel.currentNumberToPlaceCompose
    val isGameOver by gameViewModel.isGameOver
    val score by gameViewModel.score
    val isGridClearing by gameViewModel.gridClearing

    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { /* Forcer un choix */ },
            title = { Text(stringResource(id = R.string.game_over_title)) },
            text = { Text(stringResource(id = R.string.game_over_text, score)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        gameViewModel.resetGame()
                        navController.popBackStack()
                    }
                ) {
                    Text(stringResource(id = R.string.return_to_home_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        gameViewModel.resetGame()
                    }
                ) {
                    Text(stringResource(id = R.string.play_again_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Vide */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.small_title_numberup),
                    contentDescription = stringResource(id = R.string.logo_content_description),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(3f / 1f)
                        .padding(vertical = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.next_tile_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        val nextNumberImageResId = getDrawableResourceForValue(currentNumberValue)
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (nextNumberImageResId != null) {
                                Image(
                                    painter = painterResource(id = nextNumberImageResId),
                                    contentDescription = stringResource(
                                        id = R.string.next_tile_content_description,
                                        currentNumberValue
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(id = R.string.score_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                GameGrid(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 16.dp),
                    gridData = gridData,
                    onColumnClick = { selectedColumnIndex ->
                        if (!isGameOver && gameViewModel.activePlacementAnimations.isEmpty() && gameViewModel.activeValueChangeAnimations.isEmpty()) {
                            gameViewModel.placeSquare(selectedColumnIndex)
                        }
                    },
                    activePlacementAnimations = gameViewModel.activePlacementAnimations,
                    onPlacementAnimationFinished = { animationInfo ->
                        gameViewModel.onPlacementAnimationFinished(animationInfo)
                    },
                    activeValueChangeAnimations = gameViewModel.activeValueChangeAnimations,
                    onValueChangeAnimationFinished = { valueChangeAnimInfo ->
                        gameViewModel.onValueChangeAnimationFinished(valueChangeAnimInfo)
                    }
                )
            }

            if (isGridClearing) {
                val alpha = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    alpha.animateTo(1f, animationSpec = tween(150))
                    gameViewModel.clearGridAndContinue()
                    alpha.animateTo(0f, animationSpec = tween(400))
                    gameViewModel.onGridClearAnimationFinished()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = alpha.value))
                )
            }
        }
    }
}