package com.smiatek.myapplication.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherClient {
    companion object {

        var BaseUrl = "http://api.openweathermap.org/"
        var gson = GsonBuilder()
            .setLenient()
            .create()

        fun getClient(): Retrofit =
            Retrofit.Builder()
                .baseUrl(BaseUrl)
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