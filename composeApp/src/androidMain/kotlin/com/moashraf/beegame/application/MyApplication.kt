package com.moashraf.beegame.application

import android.app.Application
import com.moashraf.beegame.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin(
            config = { androidContext(this@MyApplication) })
    }
}