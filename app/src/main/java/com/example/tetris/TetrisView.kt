package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom view that renders the Tetris game board, the active piece,
 * and a ghost (drop-shadow) piece.
 */
class TetrisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var game: TetrisGame? = null

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = 0xFF2A2A4A.toInt()
    }
    private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f }
    private val overlayPaint = Paint().apply { color = 0xCC000000.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFCCCCCC.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val cellRect = RectF()
    private val highlightRect = RectF()
    private val shadowRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val g = game ?: return

        val cellSize = minOf(
            width.toFloat() / TetrisGame.BOARD_WIDTH,
            height.toFloat() / TetrisGame.BOARD_HEIGHT
        )
        val boardPxW = cellSize * TetrisGame.BOARD_WIDTH
        val boardPxH = cellSize * TetrisGame.BOARD_HEIGHT
        val ox = (width - boardPxW) / 2f
        val oy = (height - boardPxH) / 2f

        // Background
        fillPaint.color = 0xFF16213E.toInt()
        canvas.drawRect(ox, oy, ox + boardPxW, oy + boardPxH, fillPaint)

        // Draw locked cells on the board
        for (row in 0 until TetrisGame.BOARD_HEIGHT) {
            for (col in 0 until TetrisGame.BOARD_WIDTH) {
                val color = g.board[row][col]
                val cx = ox + col * cellSize
                val cy = oy + row * cellSize
                if (color != 0) {
                    drawCell(canvas, cx, cy, cellSize, color)
                }
                // grid line
                canvas.drawRect(cx, cy, cx + cellSize, cy + cellSize, gridPaint)
            }
        }

        if (!g.isGameOver) {
            // Draw ghost piece
            val ghostY = g.getGhostY()
            if (ghostY != g.currentY) {
                ghostPaint.color = Color.argb(
                    70,
                    Color.red(g.currentColor),
                    Color.green(g.currentColor),
                    Color.blue(g.currentColor)
                )
                val ghostInset = cellSize * 0.08f
                for (row in g.currentShape.indices) {
                    for (col in g.currentShape[row].indices) {
                        if (g.currentShape[row][col] == 0) continue
                        val bx = g.currentX + col
                        val by = ghostY + row
                        if (by < 0) continue
                        val cx = ox + bx * cellSize + ghostInset
                        val cy2 = oy + by * cellSize + ghostInset
                        canvas.drawRect(cx, cy2, cx + cellSize - ghostInset * 2, cy2 + cellSize - ghostInset * 2, ghostPaint)
                    }
                }
            }

            // Draw active piece
            for (row in g.currentShape.indices) {
                for (col in g.currentShape[row].indices) {
                    if (g.currentShape[row][col] == 0) continue
                    val bx = g.currentX + col
                    val by = g.currentY + row
                    if (by < 0) continue
                    drawCell(canvas, ox + bx * cellSize, oy + by * cellSize, cellSize, g.currentColor)
                }
            }
        }

        // Game-over overlay
        if (g.isGameOver) {
            canvas.drawRect(ox, oy, ox + boardPxW, oy + boardPxH, overlayPaint)
            textPaint.textSize = cellSize * 1.3f
            canvas.drawText("GAME OVER", ox + boardPxW / 2f, oy + boardPxH / 2f - cellSize * 0.6f, textPaint)
            subTextPaint.textSize = cellSize * 0.75f
            canvas.drawText("Tap to play again", ox + boardPxW / 2f, oy + boardPxH / 2f + cellSize * 0.6f, subTextPaint)
        }

        // Paused overlay
        if (g.isPaused && !g.isGameOver) {
            canvas.drawRect(ox, oy, ox + boardPxW, oy + boardPxH, overlayPaint)
            textPaint.textSize = cellSize * 1.3f
            canvas.drawText("PAUSED", ox + boardPxW / 2f, oy + boardPxH / 2f, textPaint)
        }
    }

    /** Draws a single filled cell at (x, y) with a 3-D bevel effect. */
    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val pad = size * 0.06f
        cellRect.set(x + pad, y + pad, x + size - pad, y + size - pad)

        // Base fill
        fillPaint.color = color
        fillPaint.alpha = 255
        canvas.drawRect(cellRect, fillPaint)

        // Top-left highlight
        fillPaint.color = Color.WHITE
        fillPaint.alpha = 90
        val hiW = size * 0.15f
        highlightRect.set(cellRect.left, cellRect.top, cellRect.right, cellRect.top + hiW)
        canvas.drawRect(highlightRect, fillPaint)
        highlightRect.set(cellRect.left, cellRect.top, cellRect.left + hiW, cellRect.bottom)
        canvas.drawRect(highlightRect, fillPaint)

        // Bottom-right shadow
        fillPaint.color = Color.BLACK
        fillPaint.alpha = 80
        shadowRect.set(cellRect.left, cellRect.bottom - hiW, cellRect.right, cellRect.bottom)
        canvas.drawRect(shadowRect, fillPaint)
        shadowRect.set(cellRect.right - hiW, cellRect.top, cellRect.right, cellRect.bottom)
        canvas.drawRect(shadowRect, fillPaint)

        fillPaint.alpha = 255
    }
}
