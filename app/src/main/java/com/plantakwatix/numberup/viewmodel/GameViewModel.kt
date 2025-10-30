package com.plantakwatix.numberup.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import java.util.LinkedList
import java.util.UUID
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

data class PlacementAnimationInfo(
    val id: String = UUID.randomUUID().toString(),
    val columnIndex: Int,
    val startYFraction: Float,
    val targetYFraction: Float,
    val targetRowGrid: Int,
    val value: Int,
    val isSixthSquarePlacement: Boolean = false
)

data class ValueChangeAnimationInfo(
    val id: String = UUID.randomUUID().toString(),
    val columnGrid: Int,
    val rowGrid: Int,
    val oldValue: Int,
    val newValue: Int
)

data class MatchGroup(val value: Int, val cells: List<Pair<Int, Int>>)

class GameViewModel : ViewModel() {
    companion object {
        const val GRID_SIZE = 5
        const val EMPTY_CELL = 0
        const val MIN_MATCH_COUNT = 3
        private const val PROBABILITY_WEIGHT_FACTOR = 1.8
        const val SIXTH_SQUARE_ROW_INDEX = GRID_SIZE
    }

    private val _gridStateCompose = mutableStateOf(Array(GRID_SIZE) { IntArray(GRID_SIZE) })
    val gridStateCompose: State<Array<IntArray>> = _gridStateCompose

    private val _highestUnlockedNumber = mutableIntStateOf(1)

    private val _currentNumberToPlaceCompose = mutableIntStateOf(1)
    val currentNumberToPlaceCompose: State<Int> = _currentNumberToPlaceCompose

    private val _isGameOver = mutableStateOf(false)
    val isGameOver: State<Boolean> = _isGameOver

    private val _score = mutableIntStateOf(0)
    val score: State<Int> = _score

    private val _activePlacementAnimations = mutableStateListOf<PlacementAnimationInfo>()
    val activePlacementAnimations: List<PlacementAnimationInfo> = _activePlacementAnimations

    private val _activeValueChangeAnimations = mutableStateListOf<ValueChangeAnimationInfo>()
    val activeValueChangeAnimations: List<ValueChangeAnimationInfo> = _activeValueChangeAnimations

    private val _gridClearing = mutableStateOf(false)
    val gridClearing: State<Boolean> = _gridClearing

    private var _pendingSixthSquareResolutionInfo: PlacementAnimationInfo? = null

    init {
        _currentNumberToPlaceCompose.intValue = generateNewNumberWithWeightedProbability()
    }

    fun resetGame() {
        _gridStateCompose.value = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
        _highestUnlockedNumber.intValue = 1
        _currentNumberToPlaceCompose.intValue = generateNewNumberWithWeightedProbability()
        _score.intValue = 0
        _activePlacementAnimations.clear()
        _activeValueChangeAnimations.clear()
        _gridClearing.value = false
        _isGameOver.value = false
        _pendingSixthSquareResolutionInfo = null
    }

    private fun generateNewNumberWithWeightedProbability(): Int {
        val maxUnlocked = _highestUnlockedNumber.intValue
        if (maxUnlocked <= 0) return 1
        if (maxUnlocked == 1) return 1

        val weightedNumbers = mutableListOf<Pair<Int, Double>>()
        var totalWeight = 0.0
        for (number in 1..maxUnlocked) {
            val weight = 1.0 / number.toDouble().pow(PROBABILITY_WEIGHT_FACTOR)
            weightedNumbers.add(Pair(number, weight)); totalWeight += weight
        }

        if (totalWeight <= 0) return Random.nextInt(1, maxUnlocked + 1)

        val randomValue = Random.nextDouble() * totalWeight
        var cumulativeWeight = 0.0
        for ((number, weight) in weightedNumbers) {
            cumulativeWeight += weight
            if (randomValue <= cumulativeWeight) {
                return number
            }
        }
        return weightedNumbers.lastOrNull()?.first ?: Random.nextInt(1, maxUnlocked + 1)
    }

    private fun setGridCellValue(col: Int, row: Int, value: Int) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            return
        }
        val currentGridCopy = _gridStateCompose.value.map { it.clone() }.toTypedArray()
        currentGridCopy[col][row] = value
        _gridStateCompose.value = currentGridCopy
    }

    fun placeSquare(columnIndex: Int) {
        if (_isGameOver.value || _gridClearing.value || _activePlacementAnimations.isNotEmpty() || _activeValueChangeAnimations.isNotEmpty() || _pendingSixthSquareResolutionInfo != null) {
            return
        }

        val currentGrid = _gridStateCompose.value
        var targetRowGrid = -1
        var isSixthSquare = false
        for (rowIndex in 0 until GRID_SIZE) {
            if (currentGrid[columnIndex][rowIndex] == EMPTY_CELL) {
                targetRowGrid = rowIndex; break
            }
        }
        if (targetRowGrid == -1) {
            targetRowGrid = SIXTH_SQUARE_ROW_INDEX; isSixthSquare = true
        }

        val valueToPlace = _currentNumberToPlaceCompose.intValue
        val placementInfo = PlacementAnimationInfo(columnIndex = columnIndex, startYFraction = -1.5f, targetYFraction = 0f, targetRowGrid = targetRowGrid, value = valueToPlace, isSixthSquarePlacement = isSixthSquare)
        _activePlacementAnimations.add(placementInfo)
        if (isSixthSquare) {
            _pendingSixthSquareResolutionInfo = placementInfo
        }
    }

    fun onPlacementAnimationFinished(animationInfo: PlacementAnimationInfo) {
        _activePlacementAnimations.removeAll { it.id == animationInfo.id }
        if (!animationInfo.isSixthSquarePlacement) {
            setGridCellValue(animationInfo.columnIndex, animationInfo.targetRowGrid, animationInfo.value)
        }
        if (_activePlacementAnimations.isEmpty() && _activeValueChangeAnimations.isEmpty()) {
            processGridChanges()
        }
    }

    fun onValueChangeAnimationFinished(animationInfo: ValueChangeAnimationInfo) {
        _activeValueChangeAnimations.removeAll { it.id == animationInfo.id }
        if (_activePlacementAnimations.isEmpty() && _activeValueChangeAnimations.isEmpty()) {
            processGridChanges()
        }
    }

    fun clearGridAndContinue() {
        _gridStateCompose.value = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
    }

    fun onGridClearAnimationFinished() {
        _gridClearing.value = false
        processGridChanges()
    }

    private fun processGridChanges() {
        if (_activePlacementAnimations.isNotEmpty() || _activeValueChangeAnimations.isNotEmpty()) return

        val sixthSquareInfo = _pendingSixthSquareResolutionInfo
        val matchGroups = findAllMatchesConsideringSixthSquare(sixthSquareInfo)

        if (matchGroups.isNotEmpty()) {
            prepareValueChangeAnimations(matchGroups, sixthSquareInfo)
        } else {
            if (sixthSquareInfo != null) {
                if (!_isGameOver.value) {
                    _isGameOver.value = true
                }
                _pendingSixthSquareResolutionInfo = null
            }
            _currentNumberToPlaceCompose.intValue = generateNewNumberWithWeightedProbability()

            if (!isAnyColumnAvailable()) {
                _isGameOver.value = true
            }
        }
    }

    private fun isAnyColumnAvailable(): Boolean {
        for (col in 0 until GRID_SIZE) {
            if (_gridStateCompose.value[col][GRID_SIZE - 1] == EMPTY_CELL) {
                return true
            }
        }
        return false
    }

    // CORRECTION : `applyGravity` est appelée à la fin de cette fonction.
    private fun prepareValueChangeAnimations(matchGroups: List<MatchGroup>, sixthSquareInfo: PlacementAnimationInfo?) {
        val currentGridCopy = _gridStateCompose.value.map { it.clone() }.toTypedArray()
        var sixthSquareConsumedInMerge = false

        val combo9Group = matchGroups.find { it.value == 9 }
        if (combo9Group != null) {
            _gridClearing.value = true
            _score.intValue += 1000
            _highestUnlockedNumber.intValue = max(_highestUnlockedNumber.intValue, 9)
            if (sixthSquareInfo != null) _pendingSixthSquareResolutionInfo = null
            return
        }

        matchGroups.forEach { group ->
            if (group.cells.size < MIN_MATCH_COUNT) return@forEach

            val targetCell = group.cells.minWithOrNull(compareBy({ it.second }, { it.first })) ?: return@forEach
            val targetCol = targetCell.first
            val targetRow = targetCell.second
            val valueToIncrement = group.value
            val mergedValueResult = valueToIncrement + 1

            group.cells.forEach { (col, row) ->
                val finalRow = if (row == SIXTH_SQUARE_ROW_INDEX) GRID_SIZE - 1 else row
                if (col == targetCol && finalRow == targetRow) {
                    currentGridCopy[col][finalRow] = mergedValueResult
                } else {
                    if (col in 0 until GRID_SIZE && finalRow in 0 until GRID_SIZE) {
                        currentGridCopy[col][finalRow] = EMPTY_CELL
                    }
                }
            }

            _activeValueChangeAnimations.add(
                ValueChangeAnimationInfo(
                    columnGrid = targetCol,
                    rowGrid = targetRow,
                    oldValue = valueToIncrement,
                    newValue = mergedValueResult
                )
            )

            _highestUnlockedNumber.intValue = max(_highestUnlockedNumber.intValue, mergedValueResult)
            _score.intValue += (valueToIncrement.toDouble().pow(2.0) * group.cells.size).toInt()

            if (sixthSquareInfo != null && group.cells.any { it.first == sixthSquareInfo.columnIndex && it.second == SIXTH_SQUARE_ROW_INDEX }) {
                sixthSquareConsumedInMerge = true
            }
        }

        // Appliquer la gravité après avoir traité les fusions
        val gravityApplied = applyGravity(currentGridCopy)
        _gridStateCompose.value = currentGridCopy

        // Si la gravité a modifié la grille, il faut relancer une vérification
        if (gravityApplied) {
            // Un delay court pour laisser l'UI se mettre à jour avant de relancer
            // le traitement, ce qui peut créer une boucle d'animations.
            // Sans ce delay, on pourrait avoir des bugs visuels.
            processGridChanges() // On relance immédiatement la vérification
        }


        if (sixthSquareInfo != null) {
            if (!sixthSquareConsumedInMerge && !_isGameOver.value) {
                _isGameOver.value = true
            }
            _pendingSixthSquareResolutionInfo = null
        }
    }

    // CORRECTION : Réintroduction de la fonction `applyGravity`
    private fun applyGravity(grid: Array<IntArray>): Boolean {
        var gravityApplied = false
        for (col in 0 until GRID_SIZE) {
            val column = grid[col]
            val newColumn = IntArray(GRID_SIZE) { EMPTY_CELL }
            var newColumnIndex = 0
            for (row in 0 until GRID_SIZE) {
                if (column[row] != EMPTY_CELL) {
                    newColumn[newColumnIndex++] = column[row]
                }
            }
            // Si la colonne a changé, on met à jour la grille et on marque que la gravité a agi.
            if (!column.contentEquals(newColumn)) {
                grid[col] = newColumn
                gravityApplied = true
            }
        }
        return gravityApplied
    }

    private fun findAllMatchesConsideringSixthSquare(sixthSquareInfo: PlacementAnimationInfo?): List<MatchGroup> {
        fun getValueAt(col: Int, row: Int): Int {
            if (sixthSquareInfo != null && col == sixthSquareInfo.columnIndex && row == SIXTH_SQUARE_ROW_INDEX) {
                return sixthSquareInfo.value
            }
            if (row in 0 until GRID_SIZE && col in 0 until GRID_SIZE) { return _gridStateCompose.value[col][row] }
            return EMPTY_CELL
        }

        val visited = Array(GRID_SIZE) { BooleanArray(GRID_SIZE + 1) { false } }
        val allMatchGroups = mutableListOf<MatchGroup>()
        for (startCol in 0 until GRID_SIZE) {
            val maxRowToScan = if (sixthSquareInfo != null && startCol == sixthSquareInfo.columnIndex) SIXTH_SQUARE_ROW_INDEX else GRID_SIZE - 1
            for (startRow in 0..maxRowToScan) {
                val currentValue = getValueAt(startCol, startRow)
                if (currentValue != EMPTY_CELL && !visited[startCol][startRow]) {
                    val currentGroupCells = LinkedList<Pair<Int, Int>>()
                    val queue = LinkedList<Pair<Int, Int>>()
                    queue.add(Pair(startCol, startRow)); visited[startCol][startRow] = true; currentGroupCells.add(Pair(startCol, startRow))
                    while (queue.isNotEmpty()) {
                        val (col, row) = queue.poll()!!
                        val dr = intArrayOf(-1, 1, 0, 0); val dc = intArrayOf(0, 0, -1, 1)
                        for (i in 0..3) {
                            val nextCol = col + dc[i]; val nextRow = row + dr[i]
                            val maxRowForNext = if (sixthSquareInfo != null && nextCol == sixthSquareInfo.columnIndex) SIXTH_SQUARE_ROW_INDEX else GRID_SIZE - 1
                            if (nextCol in 0 until GRID_SIZE && nextRow in 0..maxRowForNext) {
                                if (!visited[nextCol][nextRow] && getValueAt(nextCol, nextRow) == currentValue) {
                                    visited[nextCol][nextRow] = true
                                    queue.add(Pair(nextCol, nextRow))
                                    currentGroupCells.add(Pair(nextCol, nextRow))
                                }
                            }
                        }
                    }
                    if (currentGroupCells.size >= MIN_MATCH_COUNT) {
                        allMatchGroups.add(MatchGroup(currentValue, currentGroupCells.toList()))
                    }
                }
            }
        }
        return allMatchGroups.sortedByDescending { it.value }
    }
}
