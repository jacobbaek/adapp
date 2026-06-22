package com.example.tetris

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tetrisView: TetrisView
    private lateinit var nextPieceView: NextPieceView
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvLines: TextView

    private val game = TetrisGame()
    private val handler = Handler(Looper.getMainLooper())

    private val gameLoop = object : Runnable {
        override fun run() {
            if (!game.isGameOver && !game.isPaused) {
                game.moveDown()
                refreshUI()
            }
            handler.postDelayed(this, dropInterval())
        }
    }

    /** Drop interval in ms, decreasing as the level rises (minimum 100 ms). */
    private fun dropInterval(): Long = maxOf(100L, 1000L - (game.level - 1) * 90L)

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tetrisView = findViewById(R.id.tetrisView)
        nextPieceView = findViewById(R.id.nextPieceView)
        tvScore = findViewById(R.id.tvScoreValue)
        tvLevel = findViewById(R.id.tvLevelValue)
        tvLines = findViewById(R.id.tvLinesValue)

        tetrisView.game = game
        nextPieceView.game = game

        setupControls()
        refreshUI()
        handler.post(gameLoop)
    }

    override fun onPause() {
        super.onPause()
        if (!game.isGameOver) game.isPaused = true
    }

    override fun onResume() {
        super.onResume()
        // Do not auto-resume; let the user tap Pause button to un-pause
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameLoop)
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private fun refreshUI() {
        tvScore.text = game.score.toString()
        tvLevel.text = game.level.toString()
        tvLines.text = game.linesCleared.toString()
        tetrisView.invalidate()
        nextPieceView.invalidate()
    }

    // -------------------------------------------------------------------------
    // Controls
    // -------------------------------------------------------------------------

    private fun setupControls() {
        // Pause / Resume button
        val btnPause: Button = findViewById(R.id.btnPause)
        btnPause.setOnClickListener {
            if (game.isGameOver) {
                game.reset()
                btnPause.text = getString(R.string.btn_pause)
            } else {
                game.isPaused = !game.isPaused
                btnPause.text = if (game.isPaused) getString(R.string.btn_resume) else getString(R.string.btn_pause)
            }
            refreshUI()
        }

        // Game board touch: tap to restart when game over
        tetrisView.setOnClickListener {
            if (game.isGameOver) {
                game.reset()
                val pause: Button = findViewById(R.id.btnPause)
                pause.text = getString(R.string.btn_pause)
                refreshUI()
            }
        }

        // Left
        setRepeatAction(findViewById(R.id.btnLeft)) {
            game.moveLeft()
            refreshUI()
        }

        // Right
        setRepeatAction(findViewById(R.id.btnRight)) {
            game.moveRight()
            refreshUI()
        }

        // Soft drop
        setRepeatAction(findViewById(R.id.btnDown)) {
            game.moveDown()
            refreshUI()
        }

        // Rotate
        findViewById<Button>(R.id.btnRotate).setOnClickListener {
            game.rotate()
            refreshUI()
        }

        // Hard drop
        findViewById<Button>(R.id.btnHardDrop).setOnClickListener {
            game.hardDrop()
            refreshUI()
        }
    }

    /**
     * Attaches a press-and-hold action to [button]:
     * fires immediately on down, then repeats every [repeatMs] ms while held.
     */
    private fun setRepeatAction(button: Button, repeatMs: Long = 120L, action: () -> Unit) {
        val repeatHandler = Handler(Looper.getMainLooper())
        val repeatRunnable = object : Runnable {
            override fun run() {
                action()
                repeatHandler.postDelayed(this, repeatMs)
            }
        }

        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    action()
                    repeatHandler.postDelayed(repeatRunnable, repeatMs * 2)
                    button.isPressed = true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    repeatHandler.removeCallbacks(repeatRunnable)
                    button.isPressed = false
                    button.performClick()
                }
            }
            true
        }
    }
}
