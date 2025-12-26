package com.univ.android.weatherai.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.univ.android.weatherai.data.remote.ApiClient
import com.univ.android.weatherai.data.repository.WeatherRepository
import com.univ.android.weatherai.domain.model.Current
import com.univ.android.weatherai.domain.model.Daily
import com.univ.android.weatherai.domain.model.Hourly
import com.univ.android.weatherai.domain.model.Weather
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(){
    val api = remember { ApiClient.createWeatherApi }
    val repository = remember { WeatherRepository(api) }
    val viewModel: WeatherViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WeatherViewModel(repository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Weather Forecast") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is WeatherUiState.Success -> {
                    WeatherSuccessContent(
                        weather = (uiState as WeatherUiState.Success).weather,
                        summary = (uiState as WeatherUiState.Success).aiSummary,
                        onRefresh = { viewModel.fetchWeatherWithSummary(-6.2, 106.8)}
                    )
                }
                is WeatherUiState.Error -> {
                    ErrorContent(
                        message = (uiState as WeatherUiState.Error).message,
                        onRetry = { viewModel.fetchWeatherWithSummary(-6.2, 106.8) },
                        onSnackbarShown = {
                            scope.launch {
                                snackbarHostState.showSnackbar((uiState as WeatherUiState.Error).message)
                            }
                        }
                    )
                }
            }

            LaunchedEffect(Unit) {
                viewModel.fetchWeatherWithSummary(-6.2, 106.8)
            }
        }
    }
}

@Composable
private fun WeatherSuccessContent(
    weather: Weather,
    summary: String,
    onRefresh: () -> Unit
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        item {
            Text(
                text = "Current Weather",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            CurrentWeatherCard(weather = weather)
        }

        item {
            Text(
                text = summary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        //TODO Fix hourly forecast data mapping and display
        // Hourly Forecast
//        if (weather.hourly != null) {
//            item {
//                Text(
//                    text = "Hourly Forecast (Next 24 Hours)",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//            item {
//                HourlyForecastList(hourly = weather.hourly)
//            }
//        }

        // Daily Forecast
        if (weather.daily != null) {
            item {
                Text(
                    text = "Daily Forecast (Next 7 Days)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(weather.daily.weatherCode.size) { index ->
                DailyForecastItem(
                    dayIndex = index,
                    daily = weather.daily
                )
            }
        }

        item {
            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onSnackbarShown: () -> Unit) {
    LaunchedEffect(message) {
        onSnackbarShown()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to load weather",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun CurrentWeatherCard(weather: Weather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${weather.current?.temperature?.toInt() ?: "-"}°C",
                fontSize = 64.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = weatherCodeToDescription(weather.current?.weatherCode ?: 0),
                fontSize = 20.sp
            )
            Text(
                text = "Feels like ${weather.current?.apparentTemperature?.toInt() ?: "-"}°C",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun HourlyForecastList(hourly: Hourly) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(hourly.temperature.size) { index ->
            HourlyItem(
                hour = index,  // bisa format jadi "12:00", "13:00", dll. nanti
                temperature = hourly.temperature[index],
                weatherCode = hourly.weatherCode[index]
            )
        }
    }
}

@Composable
private fun HourlyItem(hour: Int, temperature: Double, weatherCode: Int) {
    Card(
        modifier = Modifier.width(80.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "${hour}:00",
                fontSize = 14.sp
            )
            // Bisa tambah icon kecil nanti
            Text(
                text = "${temperature.toInt()}°",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = weatherCodeToDescription(weatherCode),
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DailyForecastItem(dayIndex: Int, daily: Daily) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dayNameFromIndex(dayIndex),  // "Today", "Tomorrow", "Wednesday", dll.
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = weatherCodeToDescription(daily.weatherCode[dayIndex]),
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Sunrise: ${daily.sunrise.getOrNull(dayIndex)?.split("T")?.get(1) ?: "-"}",
                fontSize = 12.sp
            )
            Text(
                text = "Sunset: ${daily.sunset.getOrNull(dayIndex)?.split("T")?.get(1) ?: "-"}",
                fontSize = 12.sp
            )
        }
    }
}

private fun weatherCodeToDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }
}

private fun dayNameFromIndex(index: Int): String {
    return when (index) {
        0 -> "Today"
        1 -> "Tomorrow"
        else -> {
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, index)
            java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
        }
    }
}