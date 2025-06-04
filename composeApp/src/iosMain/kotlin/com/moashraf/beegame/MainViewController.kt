package com.moashraf.beegame

import androidx.compose.ui.window.ComposeUIViewController
import com.moashraf.beegame.di.initializeKoin

fun MainViewController() = ComposeUIViewController(configure = { initializeKoin() }) { App() }