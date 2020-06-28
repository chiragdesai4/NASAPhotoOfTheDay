package com.chirag.covid19india.ui.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.chirag.nasaphotooftheday.R
import com.chirag.nasaphotooftheday.databinding.DialogProgressBinding

class CustomProgressDialog @JvmOverloads constructor(
    context: Context?,
    private val message: String? = null
) : AlertDialog(context!!, R.style.ProgressDialog) {
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        val mBinder: DialogProgressBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_progress,
            null,
            false
        )
        setContentView(mBinder.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        if (message != null) mBinder.tvMessage.text = message
    }

    override fun onStart() {
        super.onStart()
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}