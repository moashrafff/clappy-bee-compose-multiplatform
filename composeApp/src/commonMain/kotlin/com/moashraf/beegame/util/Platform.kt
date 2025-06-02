package com.moashraf.beegame.util

enum class Platform {
    Android,
    iOS,
    Desktop,
    Wasm
}

expect fun getPlatform(): Platform