package com.chirag.nasaphotooftheday.misc

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.chirag.nasaphotooftheday.MyApp
import com.chirag.nasaphotooftheday.R


class GlideUtils {
    private val context: Context
    fun loadImageFromURL(url: String, view: ImageView?) {
        if (view != null) {
            Glide.with(context)
                .load(url)
                .apply(
                    RequestOptions()
                        .placeholder(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_vector_broken_image
                            )
                        )
                        .error(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_vector_broken_image
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                )
                .transition(withCrossFade(300))
                .into(view)
        }
    }

    companion object {
        private var glide: GlideUtils? = null
        fun get(): GlideUtils? {
            if (glide == null) {
                glide = GlideUtils()
            }
            return glide
        }
    }

    init {
        context = MyApp.get()!!
    }
}