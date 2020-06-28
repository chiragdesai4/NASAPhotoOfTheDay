package com.chirag.nasaphotooftheday.webservices

import android.content.Context
import com.chirag.covid19india.ui.base.CustomProgressDialog
import com.chirag.nasaphotooftheday.R
import com.chirag.nasaphotooftheday.misc.Logger
import com.chirag.nasaphotooftheday.misc.Utils
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class JSONCallback constructor(
    private val context: Context,
    private val dialog: CustomProgressDialog
) : Callback<ResponseBody?> {
    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
        var body: String? = null
        try { //Converting string to JSONObject
            if (response.isSuccessful) {
                body = response.body()!!.string()

                val jsonObject = JSONObject(body)
                Logger.e("Response", """${call.request().url}$jsonObject""".trimIndent())
                if (jsonObject.optString("date").isNotEmpty()) {
                    onSuccess(response.code(), jsonObject)
                } else {
                    onFailure(response.code(), jsonObject)
                }

            } else {
                body = response.errorBody()!!.string()
                if (body.isEmpty()) {
                    val message = response.raw().message
                    Logger.e("Response", """${call.request().url}$message""".trimIndent())
                    onFailed(response.code(), message)
                } else {
                    val `object` = JSONObject(body)
                    Logger.e("Response", """${call.request().url}$`object`""".trimIndent())
                    onFailure(response.code(), `object`)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            if (body != null) Logger.e(body)
            //            Utils.generateCrashReport(context, call, body);
            onFailed(response.code(), context.getString(R.string.something_went_wrong))
        } catch (e: IOException) {
            e.printStackTrace()
            if (body != null) Logger.e(body)
            onFailed(response.code(), context.getString(R.string.something_went_wrong))
        }
    }

    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
        Logger.e("Response", """${call.request().url}$t""".trimIndent())
        if (!Utils.isConnectingToInternet(context)) {
            onFailed(0, context.getString(R.string.no_internet_connection))
        } else if (t is ConnectException
            || t is SocketTimeoutException
            || t is UnknownHostException
        ) {
            onFailed(0, context.getString(R.string.failed_to_connect_with_server))
        } else if (t is IOException) {
            onFailed(0, context.getString(R.string.no_internet_connection))
        } else {
            onFailed(0, t.message)
        }
    }

    private fun onFailure(statusCode: Int, `object`: JSONObject) {
        if (statusCode == 401) {
            if (dialog.isShowing) dialog.dismiss()
            /*   new MessageDialog(context)
                    .setMessage(object.optString("message"))
                    .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            SessionManager manager = new SessionManager(context);
                            manager.clearSession(context, WalkThroughActivity.class);
                        }
                    }).show();*/
        } else {
            onFailed(statusCode, `object`.optString("Message"))
        }
    }

    protected abstract fun onFailed(statusCode: Int, message: String?)

    @Throws(JSONException::class)
    protected abstract fun onSuccess(statusCode: Int, jsonObject: JSONObject?)
}