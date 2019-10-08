package com.smiatek.myapplication.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder


class ApiClient {

    companion object {

        var gson = GsonBuilder()
            .setLenient()
            .create()

        fun getClient(): Retrofit =
            Retrofit.Builder()
                .baseUrl("http://192.168.1.1")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getOkHttp())
                .build()

        fun getOkHttp(): OkHttpClient =
            OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build()
    }
}