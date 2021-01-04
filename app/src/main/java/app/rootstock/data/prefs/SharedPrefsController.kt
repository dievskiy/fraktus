package app.rootstock.data.prefs

import android.content.SharedPreferences
import javax.inject.Singleton

sealed class CacheClass {
    class Workspace(val id: String) : CacheClass() {
        override fun toString(): String {
            return "workspace-$id"
        }
    }

    class Channel(val id: Long) : CacheClass() {
        override fun toString(): String {
            return "channel-$id"
        }
    }
}

@Singleton
class SharedPrefsController constructor(
    private val sp: SharedPreferences
) {

    fun shouldUpdateCache(cacheClass: CacheClass): Boolean {
        return sp.getBoolean(cacheClass.toString(), true)
    }

    fun updateCacheSettings(cacheClass: CacheClass, shouldUpdate: Boolean) {
        sp.edit().putBoolean(cacheClass.toString(), shouldUpdate).apply()
    }
}