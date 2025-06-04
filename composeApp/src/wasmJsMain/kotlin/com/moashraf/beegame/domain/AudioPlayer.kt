package com.moashraf.beegame.domain

actual class AudioPlayer() {
    actual fun playGameOverSound(){}
    actual fun playJumpSound() {}
    actual fun playFallingSound() {}
    actual fun stopFallingSound() {}
    actual fun playGameSoundInLoop() {}
    actual fun stopGameSound() {}
    actual fun release() {}
}