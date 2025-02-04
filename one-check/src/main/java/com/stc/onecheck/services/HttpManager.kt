package com.stc.onecheck.services

import com.google.gson.GsonBuilder
import com.stc.onecheck.modules.MainActivity
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpManager private constructor() {
    private val apiService: ApiService

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).build()
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder() //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl("http://${MainActivity.keyAddress}/api/").client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    val api: ApiService
        get() = apiService

    companion object {
        private var ourInstance: HttpManager? = null
        val instance: HttpManager?
            get() {
                if (ourInstance == null) {
                    ourInstance = HttpManager()
                }
                return ourInstance
            }
    }
}