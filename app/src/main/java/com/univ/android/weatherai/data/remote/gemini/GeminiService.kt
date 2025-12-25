package com.univ.android.weatherai.data.remote.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.univ.android.weatherai.BuildConfig
import com.univ.android.weatherai.domain.model.Weather

object GeminiService {
    private const val MODEL_NAME = "gemini-2.5-flash"

    val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    suspend fun generateWeatherSummary(weather: Weather): Result<String> = runCatching {
        val prompt = """
            Buat kesimpulan cuaca hari ini dalam bahasa Indonesia, maksimal 2-3 kalimat saja.
            Gunakan tone santai dan informatif.
            Data cuaca:
            - Suhu: ${weather.current?.temperature}°C
            - Humidity: ${weather.current?.humidity}
            - WeatherCode : ${weather.current?.weatherCode}
            - Rain: ${weather.current?.rain}
            - Precipitation: ${weather.current?.precipitation}
            - Lokasi: Lat ${weather.latitude}, Lon ${weather.longitude}
            
            Contoh output:
            "Hari ini cuaca cerah dengan suhu 28°C, cocok untuk jalan-jalan!"
        """.trimIndent()

        val response = model.generateContent(content { text(prompt) })
        response.text ?: throw Exception("Empty response from Gemini")
    }

}