package com.moashraf.beegame

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.moashraf.beegame.di.initializeKoin

fun main() = application {
    initializeKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Bee Game",
    ) {
        App()
    }
}