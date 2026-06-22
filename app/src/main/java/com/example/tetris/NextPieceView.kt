package com.example.tetris

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Small preview view that displays the upcoming Tetris piece.
 */
class NextPieceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var game: TetrisGame? = null

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bgPaint = Paint().apply { color = 0xFF16213E.toInt() }
    private val cellRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val g = game ?: return

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val shape = g.nextShape
        val color = g.nextColor

        // Find bounding box of the piece within the 4×4 matrix
        var minRow = shape.size; var maxRow = 0
        var minCol = shape[0].size; var maxCol = 0
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    if (r < minRow) minRow = r
                    if (r > maxRow) maxRow = r
                    if (c < minCol) minCol = c
                    if (c > maxCol) maxCol = c
                }
            }
        }
        val pieceRows = maxRow - minRow + 1
        val pieceCols = maxCol - minCol + 1
        if (pieceRows <= 0 || pieceCols <= 0) return

        val cellSize = minOf(width.toFloat() / (pieceCols + 2), height.toFloat() / (pieceRows + 2))
        val pieceW = pieceCols * cellSize
        val pieceH = pieceRows * cellSize
        val ox = (width - pieceW) / 2f
        val oy = (height - pieceH) / 2f

        for (r in minRow..maxRow) {
            for (c in minCol..maxCol) {
                if (shape[r][c] == 0) continue
                val px = ox + (c - minCol) * cellSize
                val py = oy + (r - minRow) * cellSize
                drawCell(canvas, px, py, cellSize, color)
            }
        }
    }

    private fun drawCell(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val pad = size * 0.06f
        cellRect.set(x + pad, y + pad, x + size - pad, y + size - pad)

        fillPaint.color = color
        fillPaint.alpha = 255
        canvas.drawRect(cellRect, fillPaint)

        val hiW = size * 0.15f
        fillPaint.color = Color.WHITE
        fillPaint.alpha = 90
        canvas.drawRect(cellRect.left, cellRect.top, cellRect.right, cellRect.top + hiW, fillPaint)
        canvas.drawRect(cellRect.left, cellRect.top, cellRect.left + hiW, cellRect.bottom, fillPaint)

        fillPaint.color = Color.BLACK
        fillPaint.alpha = 80
        canvas.drawRect(cellRect.left, cellRect.bottom - hiW, cellRect.right, cellRect.bottom, fillPaint)
        canvas.drawRect(cellRect.right - hiW, cellRect.top, cellRect.right, cellRect.bottom, fillPaint)

        fillPaint.alpha = 255
    }
}
