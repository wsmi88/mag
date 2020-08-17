package com.smiatek.myapplication.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("data/2.5/weather?")
    fun getCurrentWeatherData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("APPID") app_id: String
    ): Call<WeatherResponse>
    //api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={your api key}
}