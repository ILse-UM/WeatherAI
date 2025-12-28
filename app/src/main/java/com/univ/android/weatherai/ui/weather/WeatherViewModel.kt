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

    fun fetchWeatherWithSummary(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                repository.getWeatherForecast(latitude, longitude)
                    .onSuccess { weather ->
                        _uiState.value = WeatherUiState.Success(weather, "Loading")

                        repository.generateSummary(weather)
                            .onSuccess { summary ->
                                if(_uiState.value is WeatherUiState.Success) {
                                    _uiState.value = WeatherUiState.Success(weather, summary)
                                }
                            }
                            .onFailure { throwable ->
                                _uiState.value = WeatherUiState.Success(weather, "gagal generate ringkasan : \n ${throwable.message} ")
                            }
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
    data class Success(val weather: Weather, val aiSummary: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}