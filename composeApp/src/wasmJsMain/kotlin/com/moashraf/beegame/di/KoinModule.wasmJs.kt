package com.moashraf.beegame.di

import com.moashraf.beegame.domain.AudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> { AudioPlayer() }
}