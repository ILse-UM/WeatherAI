package com.univ.android.weatherai.data.repository

import com.univ.android.weatherai.data.remote.api.WeatherApiService
import com.univ.android.weatherai.data.remote.dto.toUI
import com.univ.android.weatherai.data.remote.gemini.GeminiService
import com.univ.android.weatherai.domain.model.Weather

class WeatherRepository(
    private val api: WeatherApiService
) {


    suspend fun getWeatherForecast(lat: Double, lon: Double): Result<Weather> = runCatching {
        api.getWeatherForecast(lat, lon).toUI()
    }

    suspend fun generateSummary(weather: Weather): Result<String> = runCatching {
        GeminiService.generateWeatherSummary(weather).getOrThrow()
    }

    suspend fun getWeatherWithSummary(lat: Double, lon: Double): Result<Pair<Weather, String>> = runCatching {
        val weather = api.getWeatherForecast(lat, lon).toUI()
        val summary = GeminiService.generateWeatherSummary(weather).getOrThrow()

        // For Debugging Purpose
//        val summary = "Lorem Ipsum"
        weather to summary
    }

}