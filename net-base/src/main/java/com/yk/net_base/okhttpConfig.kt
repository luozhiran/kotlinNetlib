package com.yk.net_base

import com.yk.net_base.interceptors.HttpLoggingInterceptor
import okhttp3.Interceptor


//这里可以添加 一些公用的header
fun getHeaderInterceptor(): Interceptor {
    return Interceptor { chain: Interceptor.Chain ->
        val origin = chain.request()
        val builder = origin.newBuilder()
        val request = builder.build()
        chain.proceed(request)
    }
}


fun getLogInterceptor(level: HttpLoggingInterceptor.Level): HttpLoggingInterceptor {
    val logInterceptor = HttpLoggingInterceptor()
    logInterceptor.level = level
    return logInterceptor
}





