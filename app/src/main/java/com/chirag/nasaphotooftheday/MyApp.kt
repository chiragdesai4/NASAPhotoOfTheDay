package com.chirag.nasaphotooftheday

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import java.net.CookieHandler
import java.net.CookieManager


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        myApp = this

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        CookieHandler.setDefault(CookieManager())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        private var myApp: MyApp? = null
        var DISPLAY_WIDTH = 0
        var DISPLAY_HEIGHT = -1

        @Synchronized
        fun get(): MyApp? {
            return myApp
        }
    }
}