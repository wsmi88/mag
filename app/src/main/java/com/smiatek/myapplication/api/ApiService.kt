package com.smiatek.myapplication.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {

    @GET
    fun getData(@Url url: String): Call<List<Double>>
}