package com.mauro.deezcover

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DeezcoverApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("DeezcoverApp", "Uncaught exception on thread=${thread.name}", throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
