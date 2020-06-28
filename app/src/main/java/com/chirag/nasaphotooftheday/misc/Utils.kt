package com.chirag.nasaphotooftheday.misc

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {
    fun isConnectingToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networks = connectivityManager.allNetworks
            var networkInfo: NetworkInfo?
            for (mNetwork in networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork)
                if (networkInfo!!.state == NetworkInfo.State.CONNECTED) {
                    Logger.d("Network", "NETWORK NAME: " + networkInfo.typeName)
                    return false
                }
            }
        } else {
            val info = connectivityManager.allNetworkInfo
            for (anInfo in info) {
                if (anInfo.state == NetworkInfo.State.CONNECTED) {
                    Logger.d(
                        "Network",
                        "NETWORK NAME: " + anInfo.typeName
                    )
                    return false
                }
            }
        }
        return true
    }

    fun getYouTubeId(youTubeUrl: String): String? {
        val pattern =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        val compiledPattern: Pattern = Pattern.compile(
            pattern,
            Pattern.CASE_INSENSITIVE
        )
        val matcher: Matcher = compiledPattern.matcher(youTubeUrl)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
}