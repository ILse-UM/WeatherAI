package com.univ.android.weatherai.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.univ.android.weatherai.domain.model.Current
import com.univ.android.weatherai.domain.model.Daily
import com.univ.android.weatherai.domain.model.Hourly
import com.univ.android.weatherai.domain.model.Weather

data class WeatherResponseDto(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("current")
    val current: CurrentDto?,
    @SerializedName("hourly")
    val hourly: HourlyDto?,
    @SerializedName("daily")
    val daily: DailyDto?,
    @SerializedName("generationtime_ms")
    val generationTimeMs: Double,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    @SerializedName("forecast_days")
    val forecastDays: Int?
)

data class CurrentDto(
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("relative_humidity_2m")
    val humidity: Double,
    @SerializedName("precipitation")
    val precipitation: Double,
    @SerializedName("rain")
    val rain: Double,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    @SerializedName("weather_code")
    val weatherCode: Int
)

data class HourlyDto(
    @SerializedName("temperature_2m")
    val temperature: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>
)

data class DailyDto(
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("sunrise")
    val sunrise: List<String>,
    @SerializedName("sunset")
    val sunset: List<String>
)

// extension

fun WeatherResponseDto.toUI() = Weather(
    latitude = latitude,
    longitude = longitude,
    current = current?.toUI(),
    hourly = hourly?.toUI(),
    daily = daily?.toUI(),
    timezone = timezone,
    timezoneAbbreviation = timezoneAbbreviation
)

private fun CurrentDto.toUI() = Current(
    temperature = temperature,
    humidity = humidity,
    precipitation = precipitation,
    rain = rain,
    apparentTemperature = apparentTemperature,
    weatherCode = weatherCode
)

private fun HourlyDto.toUI() = Hourly(
    temperature = temperature,
    weatherCode = weatherCode
)

private fun DailyDto.toUI() = Daily(
    weatherCode = weatherCode,
    sunrise = sunrise,
    sunset = sunset
)
