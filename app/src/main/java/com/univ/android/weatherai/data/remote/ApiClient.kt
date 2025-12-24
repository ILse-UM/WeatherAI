package com.univ.android.weatherai.data.remote

import com.univ.android.weatherai.data.remote.api.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val createWeatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    // TODO tambah api untuk cari latitude dan longitude dari alamat
}