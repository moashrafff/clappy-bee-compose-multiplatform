package com.moashraf.beegame.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Game(
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val gravity: Float = 0.8f,
    val beeRadius: Float = 30f,
    val beeJumpImpulse: Float = -12f,
    val beeMaxVelocity: Float = 25f
) {
    var status by mutableStateOf(GameStatus.IDLE)
        private set

    var beeVelocity by mutableStateOf(0f)
        private set

    var bee by mutableStateOf(
        Bee(
            x = (screenWidth / 4).toFloat(),
            y = (screenHeight / 2).toFloat(),
            radius = beeRadius
        )
    )
        private set

    fun startGame() {
        status = GameStatus.STARTED
    }

    fun stopGame() {
        status = GameStatus.OVER
    }

    fun restartGame() {
        resetBeePosition()
        startGame()
    }

    fun jump() {
        beeVelocity = beeJumpImpulse
    }

    fun updateGameProgress() {

        if (bee.y < 0) {
            stopTheBee()
            return
        } else if (bee.y > screenHeight) {
            stopGame()
            return
        }

        beeVelocity = (beeVelocity + gravity).coerceIn(-beeMaxVelocity, beeMaxVelocity)
        bee = bee.copy(
            y = bee.y + beeVelocity
        )
    }

    fun stopTheBee() {
        beeVelocity = 0f
        bee = bee.copy(y = 0f)
    }

    fun resetBeePosition(){
        bee = bee.copy(
            y = (screenWidth / 2).toFloat(),
        )
        beeVelocity = 0f
    }
}