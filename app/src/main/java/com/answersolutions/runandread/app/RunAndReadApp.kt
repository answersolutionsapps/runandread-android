package com.answersolutions.runandread.app

import android.app.Application
import com.answersolutions.runandread.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class RunAndReadApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}