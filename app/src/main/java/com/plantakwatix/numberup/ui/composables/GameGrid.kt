package com.plantakwatix.numberup.ui.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.plantakwatix.numberup.R
import com.plantakwatix.numberup.viewmodel.GameViewModel
import com.plantakwatix.numberup.viewmodel.PlacementAnimationInfo
import com.plantakwatix.numberup.viewmodel.ValueChangeAnimationInfo
import kotlin.math.roundToInt

@Composable
fun getDrawableResourceForValue(value: Int?): Int? {
    return when (value) {
        1 -> R.drawable.tile_1
        2 -> R.drawable.tile_2
        3 -> R.drawable.tile_3
        4 -> R.drawable.tile_4
        5 -> R.drawable.tile_5
        6 -> R.drawable.tile_6
        7 -> R.drawable.tile_7
        8 -> R.drawable.tile_8
        9 -> R.drawable.tile_9
        else -> null
    }
}

@Composable
fun Square(
    value: Int?,
    modifier: Modifier = Modifier
) {
    val imageResId = getDrawableResourceForValue(value)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imageResId != null) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Numéro $value",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AnimatedPlacementSquare(
    animationInfo: PlacementAnimationInfo,
    squareSizePx: Float,
    onAnimationComplete: () -> Unit
) {
    val startYActualOffset = animationInfo.startYFraction * squareSizePx
    val targetYActualOffset = animationInfo.targetYFraction * squareSizePx

    val animatedYOffset = remember { Animatable(initialValue = startYActualOffset) }

    LaunchedEffect(animationInfo.id) {
        animatedYOffset.animateTo(
            targetValue = targetYActualOffset,
            animationSpec = tween(durationMillis = 300, easing = LinearEasing)
        )
        onAnimationComplete()
    }

    Square(
        value = animationInfo.value,
        modifier = Modifier
            .size(with(LocalDensity.current) { squareSizePx.toDp() })
            .offset { IntOffset(x = 0, y = animatedYOffset.value.roundToInt()) }
    )
}

@Composable
fun AnimatedValueChangeSquare(
    animationInfo: ValueChangeAnimationInfo,
    squareSizePx: Float,
    onAnimationComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150),
        finishedListener = {
            if (!startAnimation) { // Animation de retour terminée
                onAnimationComplete()
            }
        }
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.7f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    LaunchedEffect(animationInfo.id) {
        startAnimation = true
    }
    LaunchedEffect(scale) {
        if (scale == 1.1f && startAnimation) { // Pic de l'animation atteint
            startAnimation = false // Déclenche le retour à l'échelle normale
        }
    }

    Square(
        value = animationInfo.newValue,
        modifier = Modifier
            .size(with(LocalDensity.current) { squareSizePx.toDp() })
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = alpha
            )
    )
}

@Composable
fun GameGrid(
    modifier: Modifier = Modifier,
    gridData: Array<IntArray>,
    onColumnClick: (columnIndex: Int) -> Unit,
    activePlacementAnimations: List<PlacementAnimationInfo>,
    onPlacementAnimationFinished: (PlacementAnimationInfo) -> Unit,
    activeValueChangeAnimations: List<ValueChangeAnimationInfo>,
    onValueChangeAnimationFinished: (ValueChangeAnimationInfo) -> Unit,
    externalBorderColor: Color = MaterialTheme.colorScheme.outline,
    externalBorderWidth: Dp = 8.dp,
    externalCornerRadius: Dp = 16.dp,
    internalLineColor: Color = MaterialTheme.colorScheme.outline,
    internalLineWidth: Dp = 0.dp
) {
    val mainGridSize = GameViewModel.GRID_SIZE

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(mainGridSize.toFloat() / (mainGridSize + 1).toFloat())
    ) {
        val squareSizePx = constraints.maxWidth.toFloat() / mainGridSize
        val squareSizeDp = with(LocalDensity.current) { squareSizePx.toDp() }
        val grid5x5HeightPx = squareSizePx * mainGridSize

        // --- Grille de fond ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { grid5x5HeightPx.toDp() })
                .align(Alignment.BottomCenter)
                .border(
                    BorderStroke(externalBorderWidth, externalBorderColor),
                    shape = RoundedCornerShape(externalCornerRadius)
                )
                .clip(RoundedCornerShape(externalCornerRadius))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center
            ) {
                gridData.forEachIndexed { columnIndex, _ ->
                    Column(
                        modifier = Modifier
                            .width(squareSizeDp)
                            .fillMaxHeight()
                            .clickable { onColumnClick(columnIndex) }
                            .then(
                                if (columnIndex < mainGridSize - 1 && internalLineWidth > 0.dp) {
                                    Modifier.drawWithContent {
                                        drawContent()
                                        val xLine = size.width - internalLineWidth.toPx() / 2
                                        drawLine(
                                            color = internalLineColor,
                                            start = Offset(xLine, 0f),
                                            end = Offset(xLine, size.height),
                                            strokeWidth = internalLineWidth.toPx()
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        for (displayRowIn5x5Grid in 0 until mainGridSize) {
                            val gridRowIndex = mainGridSize - 1 - displayRowIn5x5Grid
                            val cellValue = gridData[columnIndex][gridRowIndex]

                            val isChangingValue = activeValueChangeAnimations.any {
                                it.columnGrid == columnIndex && it.rowGrid == gridRowIndex
                            }
                            val isTargetOfNormalPlacement = activePlacementAnimations.any {
                                !it.isSixthSquarePlacement && it.columnIndex == columnIndex && it.targetRowGrid == gridRowIndex
                            }
                            val showPlaceholder = isChangingValue || isTargetOfNormalPlacement

                            Square(
                                value = if (showPlaceholder || cellValue == GameViewModel.EMPTY_CELL) null else cellValue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .then(
                                        if (displayRowIn5x5Grid < mainGridSize - 1 && internalLineWidth > 0.dp) {
                                            Modifier.drawWithContent {
                                                drawContent()
                                                val yLine =
                                                    size.height - internalLineWidth.toPx() / 2
                                                drawLine(
                                                    color = internalLineColor,
                                                    start = Offset(0f, yLine),
                                                    end = Offset(size.width, yLine),
                                                    strokeWidth = internalLineWidth.toPx()
                                                )
                                            }
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }

        // --- Couche pour les animations ---
        val animationLayerModifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(with(LocalDensity.current) { grid5x5HeightPx.toDp() })

        activePlacementAnimations.forEach { animInfo ->
            key("placement-${animInfo.id}") {
                val xPosPx = animInfo.columnIndex * squareSizePx

                val yPosContainerPx = if (animInfo.isSixthSquarePlacement) {
                    0f
                } else {
                    val displayRowIn5x5Grid = mainGridSize - 1 - animInfo.targetRowGrid
                    (displayRowIn5x5Grid + 1) * squareSizePx
                }

                Box(
                    modifier = Modifier
                        .size(squareSizeDp)
                        .offset {
                            IntOffset(
                                xPosPx.roundToInt(),
                                yPosContainerPx.roundToInt()
                            )
                        }
                ) {
                    AnimatedPlacementSquare(
                        animationInfo = animInfo,
                        squareSizePx = squareSizePx,
                        onAnimationComplete = { onPlacementAnimationFinished(animInfo) }
                    )
                }
            }
        }

        Box(modifier = animationLayerModifier) {
            activeValueChangeAnimations.forEach { animInfo ->
                key("value-change-${animInfo.id}") {
                    val xPosPx = animInfo.columnGrid * squareSizePx
                    val yPosPx = (mainGridSize - 1 - animInfo.rowGrid) * squareSizePx
                    Box(
                        modifier = Modifier.offset { IntOffset(xPosPx.roundToInt(), yPosPx.roundToInt()) }
                    ) {
                        AnimatedValueChangeSquare(
                            animationInfo = animInfo,
                            squareSizePx = squareSizePx,
                            onAnimationComplete = { onValueChangeAnimationFinished(animInfo) }
                        )
                    }
                }
            }
        }
    }
}