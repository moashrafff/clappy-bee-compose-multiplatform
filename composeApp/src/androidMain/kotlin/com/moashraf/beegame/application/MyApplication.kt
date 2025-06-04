package com.moashraf.beegame.application

import android.app.Application
import com.moashraf.beegame.di.initializeKoin

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
    }
}