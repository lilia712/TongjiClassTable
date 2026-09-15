package com.example.courseschedule.data

import android.webkit.CookieManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://1.tongji.edu.cn/"

    private val cookieInterceptor = Interceptor { chain ->
        val original = chain.request()
        val cookie = CookieManager.getInstance().getCookie(BASE_URL)
        val request = if (!cookie.isNullOrEmpty()) {
            original.newBuilder()
                .header("Cookie", cookie)
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(cookieInterceptor)
        .build()

    val api: TongjiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TongjiApiService::class.java)
    }
}
