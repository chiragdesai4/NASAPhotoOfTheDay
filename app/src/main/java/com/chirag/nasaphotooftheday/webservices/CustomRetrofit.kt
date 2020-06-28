package com.chirag.nasaphotooftheday.webservices

import android.util.Log
import com.chirag.covid19india.webservice.ApiInterface
import com.chirag.nasaphotooftheday.misc.C.BASE_URL
import com.chirag.nasaphotooftheday.misc.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class CustomRetrofit {
    private var baseURL: String? = null
    private var endPoint: String? = null
    private var endPointExtra = ""

    fun setAPI(endPoint: String): CustomRetrofit {
        this.endPoint = endPoint
        Logger.e("URL", BASE_URL + endPoint)
        return this
    }

    fun setGetParameters(params: HashMap<String, String>?): CustomRetrofit {
        if (params != null && params.isNotEmpty()) {
            for ((key, value) in params) {
                Logger.e("params", key + "\t" + value)
                endPointExtra =
                    if (endPointExtra.contains("?")) {
                        "$endPointExtra&$key=$value"
                    } else {
                        "$endPointExtra?$key=$value"
                    }
            }
            Logger.e("EndpointExtra: ", endPointExtra)
        }
        return this
    }

    fun setCallBackListener(listener: JSONCallback?) {
        makeCall().enqueue(listener)
    }

    private fun makeCall(): Call<ResponseBody> {
        val call: Call<ResponseBody>
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(provideHttpLoggingInterceptor())
            .addInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val original = chain.request()
                    val request = chain.request()
                        .newBuilder() //.header("Authorization", "token " + generateToken(original.url().toString(), original.method()))
                        .method(original.method, original.body)
                        .build()
                    return chain.proceed(request)
                }
            })
            .build()
        val APIInterface = Retrofit.Builder()
            .baseUrl(if (baseURL != null) baseURL else BASE_URL)
            .client(client)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiInterface::class.java)

        call = APIInterface.callGetMethod(endPoint + endPointExtra)
        return call
    }

    companion object {
        fun with(): CustomRetrofit {
            return CustomRetrofit()
        }

        private fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d("Log", message)
                    }
                })
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return httpLoggingInterceptor
        }
    }
}