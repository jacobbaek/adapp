package com.example.tetris

import android.graphics.Color
import kotlin.random.Random

/**
 * Core Tetris game logic: board state, piece management, movement, rotation,
 * line clearing, and scoring.
 */
class TetrisGame {

    companion object {
        const val BOARD_WIDTH = 10
        const val BOARD_HEIGHT = 20

        /** Piece shapes encoded as 4×4 bit matrices (row-major order). */
        private val SHAPES: Array<Array<IntArray>> = arrayOf(
            // I
            arrayOf(
                intArrayOf(0, 0, 0, 0),
                intArrayOf(1, 1, 1, 1),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // O
            arrayOf(
                intArrayOf(0, 1, 1, 0),
                intArrayOf(0, 1, 1, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // T
            arrayOf(
                intArrayOf(0, 1, 0, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // S
            arrayOf(
                intArrayOf(0, 1, 1, 0),
                intArrayOf(1, 1, 0, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // Z
            arrayOf(
                intArrayOf(1, 1, 0, 0),
                intArrayOf(0, 1, 1, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // J
            arrayOf(
                intArrayOf(1, 0, 0, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            ),
            // L
            arrayOf(
                intArrayOf(0, 0, 1, 0),
                intArrayOf(1, 1, 1, 0),
                intArrayOf(0, 0, 0, 0),
                intArrayOf(0, 0, 0, 0)
            )
        )

        val PIECE_COLORS: IntArray = intArrayOf(
            Color.CYAN,
            Color.YELLOW,
            0xFFAA00FF.toInt(),   // Purple  (T)
            0xFF00CC00.toInt(),   // Green   (S)
            Color.RED,            // Red     (Z)
            0xFF3399FF.toInt(),   // Blue    (J)
            0xFFFF8C00.toInt()    // Orange  (L)
        )
    }

    // Board: 0 = empty, any other value = ARGB color of the locked piece
    val board: Array<IntArray> = Array(BOARD_HEIGHT) { IntArray(BOARD_WIDTH) { 0 } }

    var currentShape: Array<IntArray> = emptyArray()
    var currentColor: Int = 0
    var currentX: Int = 0
    var currentY: Int = 0

    var nextShape: Array<IntArray> = emptyArray()
    var nextColor: Int = 0

    var score: Int = 0
    var level: Int = 1
    var linesCleared: Int = 0
    var isGameOver: Boolean = false
    var isPaused: Boolean = false

    init {
        generateNext()
        spawnPiece()
    }

    // -------------------------------------------------------------------------
    // Piece management
    // -------------------------------------------------------------------------

    private fun generateNext() {
        val idx = Random.nextInt(SHAPES.size)
        nextShape = SHAPES[idx].map { it.clone() }.toTypedArray()
        nextColor = PIECE_COLORS[idx]
    }

    fun spawnPiece() {
        currentShape = nextShape.map { it.clone() }.toTypedArray()
        currentColor = nextColor
        currentX = BOARD_WIDTH / 2 - 2
        currentY = 0
        generateNext()

        if (!isValidPosition(currentShape, currentX, currentY)) {
            isGameOver = true
        }
    }

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    fun moveLeft(): Boolean {
        if (isPaused || isGameOver) return false
        return if (isValidPosition(currentShape, currentX - 1, currentY)) {
            currentX--
            true
        } else false
    }

    fun moveRight(): Boolean {
        if (isPaused || isGameOver) return false
        return if (isValidPosition(currentShape, currentX + 1, currentY)) {
            currentX++
            true
        } else false
    }

    /**
     * Moves the piece one row down.
     * Returns `true` when the piece moved; `false` when it landed (piece locked).
     */
    fun moveDown(): Boolean {
        if (isPaused || isGameOver) return false
        return if (isValidPosition(currentShape, currentX, currentY + 1)) {
            currentY++
            true
        } else {
            lockPiece()
            false
        }
    }

    fun rotate() {
        if (isPaused || isGameOver) return
        val rotated = rotateCW(currentShape)
        // Basic wall-kick: try original position then nudge left/right
        for (kick in intArrayOf(0, -1, 1, -2, 2)) {
            if (isValidPosition(rotated, currentX + kick, currentY)) {
                currentShape = rotated
                currentX += kick
                return
            }
        }
    }

    fun hardDrop() {
        if (isPaused || isGameOver) return
        while (isValidPosition(currentShape, currentX, currentY + 1)) {
            currentY++
            score += 2
        }
        lockPiece()
    }

    // -------------------------------------------------------------------------
    // Ghost piece
    // -------------------------------------------------------------------------

    /** Returns the Y coordinate where the current piece would land. */
    fun getGhostY(): Int {
        var dropY = currentY
        while (isValidPosition(currentShape, currentX, dropY + 1)) {
            dropY++
        }
        return dropY
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun isValidPosition(shape: Array<IntArray>, x: Int, y: Int): Boolean {
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col] == 0) continue
                val bx = x + col
                val by = y + row
                if (bx < 0 || bx >= BOARD_WIDTH || by >= BOARD_HEIGHT) return false
                if (by >= 0 && board[by][bx] != 0) return false
            }
        }
        return true
    }

    private fun rotateCW(shape: Array<IntArray>): Array<IntArray> {
        val n = shape.size
        return Array(n) { row -> IntArray(n) { col -> shape[n - 1 - col][row] } }
    }

    private fun lockPiece() {
        for (row in currentShape.indices) {
            for (col in currentShape[row].indices) {
                if (currentShape[row][col] == 0) continue
                val by = currentY + row
                val bx = currentX + col
                if (by in 0 until BOARD_HEIGHT && bx in 0 until BOARD_WIDTH) {
                    board[by][bx] = currentColor
                }
            }
        }
        clearLines()
        spawnPiece()
    }

    private fun clearLines() {
        val surviving = (0 until BOARD_HEIGHT).filter { row ->
            board[row].any { it == 0 }
        }
        val cleared = BOARD_HEIGHT - surviving.size
        if (cleared == 0) return

        // Snapshot surviving rows first, then rebuild to avoid in-place aliasing
        val snapshot = surviving.map { board[it].clone() }
        for (i in 0 until (BOARD_HEIGHT - surviving.size)) {
            board[i] = IntArray(BOARD_WIDTH)
        }
        for (i in snapshot.indices) {
            board[BOARD_HEIGHT - snapshot.size + i] = snapshot[i]
        }

        linesCleared += cleared
        score += when (cleared) {
            1 -> 100 * level
            2 -> 300 * level
            3 -> 500 * level
            else -> 800 * level  // 4 lines (Tetris)
        }
        level = linesCleared / 10 + 1
    }

    // -------------------------------------------------------------------------
    // State reset
    // -------------------------------------------------------------------------

    fun reset() {
        for (row in board) row.fill(0)
        score = 0
        level = 1
        linesCleared = 0
        isGameOver = false
        isPaused = false
        generateNext()
        spawnPiece()
    }
}
