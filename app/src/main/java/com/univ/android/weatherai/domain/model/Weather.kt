package com.univ.android.weatherai.domain.model

data class Weather(
    val latitude: Double,
    val longitude: Double,
    val current: Current?,
    val hourly: Hourly?,
    val daily: Daily?,
    val timezone: String,
    val timezoneAbbreviation: String
)

data class Current(
    val temperature: Double,
    val humidity: Double,
    val precipitation: Double,
    val rain: Double,
    val apparentTemperature: Double,
    val weatherCode: Int
)

data class Hourly(
    val temperature: List<Double>,
    val weatherCode: List<Int>
)

data class Daily(
    val weatherCode: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>
)