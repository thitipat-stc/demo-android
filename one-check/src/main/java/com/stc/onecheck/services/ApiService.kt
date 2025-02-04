package com.stc.onecheck.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("control")
    fun send(@Query("alert") str: String): Call<String>
    
    @GET("control")
    fun clear(@Query("clear") str: String): Call<String>
}
