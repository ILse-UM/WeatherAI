package com.univ.android.weatherai.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univ.android.weatherai.data.repository.WeatherRepository
import com.univ.android.weatherai.domain.model.Weather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository): ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                repository.getWeatherForecast(latitude, longitude)
                    .onSuccess { weather ->
                        _uiState.value = WeatherUiState.Success(weather)
                    }
                    .onFailure { exception ->
                        _uiState.value = WeatherUiState.Error(exception.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.message ?: "Crash di ViewModel")
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: Weather) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}