package com.chirag.nasaphotooftheday.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.chirag.covid19india.ui.base.CustomProgressDialog
import com.chirag.nasaphotooftheday.MyApp
import com.chirag.nasaphotooftheday.R
import com.chirag.nasaphotooftheday.misc.C
import com.chirag.nasaphotooftheday.misc.GlideUtils
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {
    private var shouldPerformDispatchTouch = true
    private var lastClickTime: Long = 0
    private var dialogCustom: CustomProgressDialog? = null
    private var snackbar: Snackbar? = null
    private lateinit var glideUtils: GlideUtils

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onStart() {
        super.onStart()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(C.TAG, this.javaClass.simpleName + " created")
        setupDisplayMetrics()
        setBackground(R.color.white)
        glideUtils = GlideUtils()
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        if (snackbar != null && snackbar!!.isShown) snackbar!!.dismiss()
    }

    fun showShortToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @JvmOverloads
    fun showProgressBar(message: String? = null): CustomProgressDialog {
        if (dialogCustom == null) dialogCustom = CustomProgressDialog(this, message)
        return dialogCustom!!
    }

    fun hideProgressBar() {
        if (dialogCustom != null && dialogCustom!!.isShowing) {
            dialogCustom!!.dismiss()
        }
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var ret = false
        return try {
            val view = currentFocus
            ret = super.dispatchTouchEvent(event)
            if (shouldPerformDispatchTouch) {
                if (view is EditText) {
                    val w = currentFocus
                    val scrCords = IntArray(2)
                    if (w != null) {
                        w.getLocationOnScreen(scrCords)
                        val x = event.rawX + w.left - scrCords[0]
                        val y = event.rawY + w.top - scrCords[1]
                        if (event.action == MotionEvent.ACTION_UP
                            && (x < w.left || x >= w.right || y < w.top || y > w.bottom)
                        ) {
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                        }
                    }
                }
            }
            ret
        } catch (e: Exception) {
            ret
        }
    }

    open fun setupDisplayMetrics() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        MyApp.DISPLAY_WIDTH = displayMetrics.widthPixels
        MyApp.DISPLAY_HEIGHT = displayMetrics.heightPixels
    }

    open fun setBackground(color: Int) {
        setStatusBarColor(color)
        window.decorView.rootView.setBackgroundColor(resources.getColor(color))
    }

    open fun setStatusBarColor(color: Int) {
        window.statusBarColor = ContextCompat.getColor(this, color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

}