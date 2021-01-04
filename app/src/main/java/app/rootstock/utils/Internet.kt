package app.rootstock.utils


import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.lifecycle.LiveData

object InternetUtil : LiveData<Boolean>() {

    private lateinit var application: Application

    fun init(application: Application) {
        this.application = application
    }

    fun isInternetOn(): Boolean {
        if (!::application.isInitialized) return true
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

}