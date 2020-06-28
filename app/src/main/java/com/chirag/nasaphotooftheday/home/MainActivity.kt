package com.chirag.nasaphotooftheday.home

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.chirag.nasaphotooftheday.R
import com.chirag.nasaphotooftheday.base.BaseActivity
import com.chirag.nasaphotooftheday.databinding.ActivityMainBinding
import com.chirag.nasaphotooftheday.dialogs.MessageDialog
import com.chirag.nasaphotooftheday.entities.DailyPhoto
import com.chirag.nasaphotooftheday.misc.C.API
import com.chirag.nasaphotooftheday.misc.C.API_KEY
import com.chirag.nasaphotooftheday.misc.C.MEDIA_TYPE
import com.chirag.nasaphotooftheday.misc.C.MEDIA_URL
import com.chirag.nasaphotooftheday.misc.C.TYPE_IMAGE
import com.chirag.nasaphotooftheday.misc.C.TYPE_VIDEO
import com.chirag.nasaphotooftheday.misc.C.YOUTUBE_API_KEY
import com.chirag.nasaphotooftheday.misc.GlideUtils
import com.chirag.nasaphotooftheday.misc.Logger
import com.chirag.nasaphotooftheday.misc.Utils
import com.chirag.nasaphotooftheday.webservices.CustomRetrofit
import com.chirag.nasaphotooftheday.webservices.JSONCallback
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubeThumbnailLoader
import com.google.android.youtube.player.YouTubeThumbnailLoader.OnThumbnailLoadedListener
import com.google.android.youtube.player.YouTubeThumbnailView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : BaseActivity(), YouTubeThumbnailView.OnInitializedListener {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var chosenDate: String
    private lateinit var dailyPhoto: DailyPhoto
    private var cal = Calendar.getInstance()
    lateinit var videoUrl: String
    private var doubleBackToExitPressedOnce: Boolean = false

    private var youTubeThumbnailLoader: YouTubeThumbnailLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        getDailyPhoto(false)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        mBinding.ivDateOption.setOnClickListener {

            val dialog =
                DatePickerDialog(
                    this, dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
            dialog.datePicker.maxDate = Date().time
            dialog.show()
        }

        mBinding.ivViewOrPlay.setOnClickListener {
            if (dailyPhoto.media_type == TYPE_IMAGE)
                startActivity(
                    Intent(this, ImageFullScreenActivity::class.java)
                        .putExtra(MEDIA_TYPE, dailyPhoto.media_type)
                        .putExtra(MEDIA_URL, dailyPhoto.url)
                )
            else
                startActivity(
                    Intent(this, VideoPlayerActivity::class.java)
                        .putExtra(MEDIA_URL, videoUrl)
                )
        }
    }

    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        chosenDate = sdf.format(cal.time)
        getDailyPhoto(true)
    }

    private fun getDailyPhoto(isDateSelected: Boolean) {
        try {
            val params = HashMap<String, String>()

            if (isDateSelected)
                params["date"] = chosenDate
            params["api_key"] = API_KEY

            CustomRetrofit.with()
                .setAPI(API)
                .setGetParameters(params)
                .setCallBackListener(object : JSONCallback(this, showProgressBar()) {
                    override fun onSuccess(statusCode: Int, jsonObject: JSONObject?) {

                        try {
                            val gson = Gson()
                            dailyPhoto = gson.fromJson(
                                jsonObject.toString(),
                                object : TypeToken<DailyPhoto?>() {}.type
                            )
                            mBinding.item = dailyPhoto
                            mBinding.executePendingBindings()
                            if (dailyPhoto.media_type == TYPE_IMAGE) {
                                mBinding.ytvBackground.visibility = GONE
                                mBinding.ivBackground.visibility = VISIBLE
                                mBinding.ivViewOrPlay.setImageDrawable(getDrawable(R.drawable.ic_vector_zoom))
                                if (jsonObject != null) {
                                    if (jsonObject.has("hdurl"))
                                        GlideUtils.get()
                                            ?.loadImageFromURL(
                                                dailyPhoto.hdurl,
                                                mBinding.ivBackground
                                            )
                                    else
                                        GlideUtils.get()
                                            ?.loadImageFromURL(
                                                dailyPhoto.url,
                                                mBinding.ivBackground
                                            )
                                }
                            } else if (dailyPhoto.media_type == TYPE_VIDEO) {
                                mBinding.ytvBackground.visibility = VISIBLE
                                mBinding.ivBackground.visibility = GONE
                                mBinding.ivViewOrPlay.setImageDrawable(getDrawable(R.drawable.ic_vector_play))
                                if (jsonObject != null) {

                                    if (jsonObject.has("hdurl")) {
                                        videoUrl = dailyPhoto.hdurl
                                        videoUrl.replace("embed/", "watch?v=")
                                        videoUrl.replace("?rel=0", "")
                                        Logger.e("Response HD Url: $videoUrl")
                                    } else {
                                        videoUrl = dailyPhoto.url
                                        val replaceEmbed = videoUrl.replace("embed/", "watch?v=")
                                        val replaceEnd = replaceEmbed.replace("?rel=0", "")
                                        Logger.e("Response Url: $replaceEnd")
                                        videoUrl = replaceEnd
                                    }
                                    mBinding.ytvBackground.initialize(
                                        YOUTUBE_API_KEY,
                                        this@MainActivity
                                    )
                                }
                            }
                            hideProgressBar()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideProgressBar()
                        }
                    }

                    override fun onFailed(statusCode: Int, message: String?) {
                        showShortToast(message)
                        hideProgressBar()
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            hideProgressBar()
            MessageDialog(this)
                .setMessage(e.message)
                .setPositiveButton(
                    getString(R.string.retry), DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        getDailyPhoto(false)
                    })
                .setNegativeButton(
                    getString(R.string.cancel),
                    DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }).show()
        }
    }

    override fun onInitializationSuccess(
        thumbnailView: YouTubeThumbnailView?,
        thumbnailLoader: YouTubeThumbnailLoader?
    ) {
        youTubeThumbnailLoader = thumbnailLoader
        thumbnailLoader?.setOnThumbnailLoadedListener(ThumbnailLoadedListener())

        youTubeThumbnailLoader?.setVideo(Utils.getYouTubeId(videoUrl))
    }

    override fun onInitializationFailure(
        thumbnailView: YouTubeThumbnailView?,
        error: YouTubeInitializationResult?
    ) {
        Logger.e("YouTubeThumbnailView.onInitializationFailure()")
    }

    private class ThumbnailLoadedListener : OnThumbnailLoadedListener {
        override fun onThumbnailError(
            arg0: YouTubeThumbnailView?,
            arg1: YouTubeThumbnailLoader.ErrorReason?
        ) {
            Logger.e("ThumbnailLoadedListener.onThumbnailError()")
        }

        override fun onThumbnailLoaded(
            arg0: YouTubeThumbnailView,
            arg1: String
        ) {
            Logger.e("ThumbnailLoadedListener.onThumbnailLoaded()")
        }
    }


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}