package com.mauro.offlinefirst

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OfflineFirstApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("OfflineFirstApp", "Uncaught exception on thread=${thread.name}", throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
