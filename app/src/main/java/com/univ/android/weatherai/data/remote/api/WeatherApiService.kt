package com.univ.android.weatherai.data.remote.api

import com.univ.android.weatherai.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String? = "temperature_2m,relative_humidity_2m,precipitation,rain,apparent_temperature,weather_code",
        @Query("hourly") hourly: String? = "temperature_2m,weather_code",
        @Query("daily") daily: String? = "weather_code,sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int? = 7
    ): WeatherResponseDto

}
