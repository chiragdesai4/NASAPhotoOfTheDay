package com.chirag.nasaphotooftheday.entities

import com.google.gson.annotations.SerializedName


data class DailyPhoto(
    @SerializedName("copyright") val copyright: String,
    @SerializedName("date") val date: String,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("hdurl") val hdurl: String,
    @SerializedName("media_type") val media_type: String,
    @SerializedName("service_version") val service_version: String,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String


) {
    override fun toString(): String {
        return "DailyPhoto(copyright='$copyright', date='$date', explanation='$explanation', hdurl='$hdurl', media_type='$media_type', service_version='$service_version', title='$title', url='$url')"
    }
}