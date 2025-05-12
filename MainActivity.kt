package com.example.gameathon

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import kotlin.random.Random
import java.util.Timer
import java.util.TimerTask

class MainActivity : Activity() {

    private lateinit var basket: ImageView
    private var score = 0
    private var missedNormalEggs = 0
    private lateinit var eggTimer: Timer
    private lateinit var scoreText: TextView
    private lateinit var playButton: Button
    private lateinit var gameOverLayout: LinearLayout
    private lateinit var highScoreText: TextView
    private lateinit var overlay: View
    private lateinit var gameNameImage: ImageView
    private var highScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        basket = findViewById(R.id.basket)
        scoreText = findViewById(R.id.scoreText)
        playButton = findViewById(R.id.playButton)
        gameOverLayout = findViewById(R.id.gameOverLayout)
        highScoreText = findViewById(R.id.highScoreText)
        overlay = findViewById(R.id.overlay)
        gameNameImage = findViewById(R.id.gameNameImage)

        // Move the basket with touch
        basket.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX - v.width / 2
                }
            }
            true
        }

        // Start or restart the game when play button is clicked
        playButton.setOnClickListener {
            startGame()
        }
    }

    private fun startGame() {
        score = 0
        missedNormalEggs = 0
        updateScore()
        playButton.visibility = View.GONE
        gameOverLayout.visibility = View.GONE
        overlay.visibility = View.GONE
        gameNameImage.visibility = View.GONE
        startEggDrop()
    }

    private fun startEggDrop() {
        eggTimer = Timer()
        eggTimer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    dropEgg()
                }
            }
        }, 0, 1000) // Drop an egg or rotten egg every second
    }

    private fun dropEgg() {
        val egg = ImageView(this).apply {
            val isRotten = Random.nextInt(10) < 2 // 20% chance for rotten egg, 80% chance for normal egg
            val drawableRes = if (isRotten) R.drawable.rotten_egg else R.drawable.egg
            setImageResource(drawableRes)
            layoutParams = RelativeLayout.LayoutParams(100, 100)
            x = Random.nextFloat() * (basket.parent as View).width
            y = 0f
        }

        (basket.parent as RelativeLayout).addView(egg)

        egg.animate()
            .y(basket.y)
            .setDuration(3000)
            .withEndAction {
                checkCollision(egg)
            }
            .start()
    }

    private fun checkCollision(egg: ImageView) {
        val eggRect = Rect()
        egg.getHitRect(eggRect)

        val basketRect = Rect()
        basket.getHitRect(basketRect)

        val isRotten = egg.drawable.constantState == getDrawable(R.drawable.rotten_egg)?.constantState

        if (Rect.intersects(eggRect, basketRect)) {
            if (isRotten) {
                score -= 2
            } else {
                score += 1
            }
            updateScore()
        } else {
            if (isRotten) {
                // Rotten eggs don't affect the missed normal egg count
            } else {
                missedNormalEggs += 1
                if (missedNormalEggs >= 3) {
                    endGame()
                    return
                }
            }
        }

        (basket.parent as RelativeLayout).removeView(egg)
    }

    private fun updateScore() {
        scoreText.text = "Score: $score"
    }

    private fun endGame() {
        eggTimer.cancel()

        // Remove all remaining eggs from the screen
        val parentLayout = basket.parent as RelativeLayout
        for (i in parentLayout.childCount - 1 downTo 0) {
            val view = parentLayout.getChildAt(i)
            if (view is ImageView && view != basket) {
                parentLayout.removeView(view)
            }
        }

        // Show the overlay, Game Over layout, and high score
        highScore = maxOf(highScore, score)
        highScoreText.text = "High Score: $highScore"
        overlay.visibility = View.VISIBLE
        gameOverLayout.visibility = View.VISIBLE
        playButton.visibility = View.VISIBLE
        gameNameImage.visibility = View.GONE // Hide game name image on game over
    }
}
