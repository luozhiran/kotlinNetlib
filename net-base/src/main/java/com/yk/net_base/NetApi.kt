package com.yk.net_base

import androidx.annotation.NonNull
import com.yk.net_base.gson.GsonFactory
import com.yk.net_base.interceptors.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//双重检测单例模式

var BASE_URL: String = "https://www.baidu.com"

class NetApi private constructor() {

    companion object {
        val instance = Holder.holder
    }

    private object Holder {
        val holder = NetApi()
    }


    private val okHttpClient: OkHttpClient
    private val retrofit: Retrofit
    private val logInterceptor: HttpLoggingInterceptor = getLogInterceptor(HttpLoggingInterceptor.Level.BODY)

    init {
        okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(getHeaderInterceptor())
            .addInterceptor(logInterceptor)
            .retryOnConnectionFailure(true).build()
        retrofit = Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.buildGson()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    fun setLogLevel(level: HttpLoggingInterceptor.Level) {
        logInterceptor.level = level
    }



    fun <T> create(services: Class<T>?): T {
        return retrofit.create(services)
    }
}


