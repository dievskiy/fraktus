package app.rootstock

import android.app.Application
import app.rootstock.utils.InternetUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        InternetUtil.init(this)
    }
}