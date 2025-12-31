package com.univ.android.weatherai.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.AcUnit  // snow
import androidx.compose.material.icons.filled.Grass   // fog/drizzle placeholder
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp)
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
                textAlign = TextAlign.Justify
            )
        }

        //TODO Fix hourly forecast data mapping and display
        // Hourly Forecast
        if (weather.hourly != null) {
            item {
                Text(
                    text = "Hourly Forecast (Next 24 Hours)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                HourlyForecastList(currentTime = getHour(weather.current?.time), hourly = weather.hourly)
            }
        }

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
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
        ){
            Icon(
                imageVector = weatherIcon(weather.current?.weatherCode ?: 0),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 32.dp)
                    .wrapContentWidth(Alignment.End)
                    .defaultMinSize(minWidth = 200.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
                ,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${weather.current?.temperature?.toInt() ?: "-"}°C",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = weatherCodeToDescription(weather.current?.weatherCode ?: 0),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Feels like ${weather.current?.apparentTemperature?.toInt() ?: "-"}°C",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f)
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastList(currentTime: Int, hourly: Hourly) {
    //Filter jam
    val upcomingHours = remember(hourly.time, currentTime) {
        hourly.time.indices
            .take(24)
            .filter { getHour(hourly.time[it]) >= currentTime }
            .map { index ->
                Triple(
                    getHour(hourly.time[index]),
                    hourly.temperature[index],
                    hourly.weatherCode[index]
                )
            }
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(upcomingHours) { (hour, temperature, weatherCode) ->
            HourlyItem(
                hour = hour,
                temperature = temperature,
                weatherCode = weatherCode
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
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    imageVector = weatherIcon(daily.weatherCode[dayIndex]),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = dayNameFromIndex(dayIndex),  // "Today", "Tomorrow", "Wednesday", dll.
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = weatherCodeToDescription(daily.weatherCode[dayIndex]),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("↑ ${daily.sunrise.getOrNull(dayIndex)?.substring(11, 16) ?: "-"}", fontSize = 12.sp)
                Text("↓ ${daily.sunset.getOrNull(dayIndex)?.substring(11, 16) ?: "-"}", fontSize = 12.sp)
            }
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
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, index)
            SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        }
    }
}

@Composable
private fun weatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Filled.WbSunny
        1, 2, 3 -> Icons.Filled.Cloud
        45, 48 -> Icons.Filled.Grass  // fog
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Filled.Cloud
        71, 73, 75 -> Icons.Filled.AcUnit
        95, 96, 99 -> Icons.Filled.Thunderstorm
        else -> Icons.Filled.Cloud
    }
}

private fun getHour(time: String?): Int {
    val hour = time?.substring(11, 13)?.toInt()?.plus(1) ?: 0
    if (hour == 24) return 0
    return hour
}

@Preview
@Composable
private fun WeatherScreenPreview() {
    WeatherSuccessContent(dummyWeather, "") { }

}

val dummyWeather = Weather(
    latitude = -6.2,
    longitude = 106.8,
    current = Current(
        time = "2025-12-27T12:00",
        temperature = 28.5,
        humidity = 75.0,
        precipitation = 0.0,
        rain = 0.0,
        apparentTemperature = 32.0,
        weatherCode = 1  // partly cloudy
    ),
    hourly = Hourly(            // 7 hari ke depan
        time = listOf("2025-12-29T00:00",
            "2025-12-29T01:00",
            "2025-12-29T02:00",
            "2025-12-29T03:00",
            "2025-12-29T04:00",
            "2025-12-29T05:00",
            "2025-12-29T06:00",
            "2025-12-29T07:00",
            "2025-12-29T08:00",
            "2025-12-29T09:00",
            "2025-12-29T10:00",
            "2025-12-29T11:00",
            "2025-12-29T12:00",
            "2025-12-29T13:00",
            "2025-12-29T14:00",
            "2025-12-29T15:00",
            "2025-12-29T16:00",
            "2025-12-29T17:00",
            "2025-12-29T18:00",
            "2025-12-29T19:00",
            "2025-12-29T20:00",
            "2025-12-29T21:00",
            "2025-12-29T22:00",
            "2025-12-29T23:00",
            "2025-12-30T00:00",
            "2025-12-30T01:00",
            "2025-12-30T02:00",
            "2025-12-30T03:00",
            "2025-12-30T04:00",
            "2025-12-30T05:00",
            "2025-12-30T06:00",
            "2025-12-30T07:00",
            "2025-12-30T08:00",
            "2025-12-30T09:00",
            "2025-12-30T10:00",
            "2025-12-30T11:00",
            "2025-12-30T12:00",
            "2025-12-30T13:00",
            "2025-12-30T14:00",
            "2025-12-30T15:00",
            "2025-12-30T16:00",
            "2025-12-30T17:00",
            "2025-12-30T18:00",
            "2025-12-30T19:00",
            "2025-12-30T20:00",
            "2025-12-30T21:00",
            "2025-12-30T22:00",
            "2025-12-30T23:00",
            "2025-12-31T00:00",
            "2025-12-31T01:00",
            "2025-12-31T02:00",
            "2025-12-31T03:00",
            "2025-12-31T04:00",
            "2025-12-31T05:00",
            "2025-12-31T06:00",
            "2025-12-31T07:00",
            "2025-12-31T08:00",
            "2025-12-31T09:00",
            "2025-12-31T10:00",
            "2025-12-31T11:00",
            "2025-12-31T12:00",
            "2025-12-31T13:00",
            "2025-12-31T14:00",
            "2025-12-31T15:00",
            "2025-12-31T16:00",
            "2025-12-31T17:00",
            "2025-12-31T18:00",
            "2025-12-31T19:00",
            "2025-12-31T20:00",
            "2025-12-31T21:00",
            "2025-12-31T22:00",
            "2025-12-31T23:00",
            "2026-01-01T00:00",
            "2026-01-01T01:00",
            "2026-01-01T02:00",
            "2026-01-01T03:00",
            "2026-01-01T04:00",
            "2026-01-01T05:00",
            "2026-01-01T06:00",
            "2026-01-01T07:00",
            "2026-01-01T08:00",
            "2026-01-01T09:00",
            "2026-01-01T10:00",
            "2026-01-01T11:00",
            "2026-01-01T12:00",
            "2026-01-01T13:00",
            "2026-01-01T14:00",
            "2026-01-01T15:00",
            "2026-01-01T16:00",
            "2026-01-01T17:00",
            "2026-01-01T18:00",
            "2026-01-01T19:00",
            "2026-01-01T20:00",
            "2026-01-01T21:00",
            "2026-01-01T22:00",
            "2026-01-01T23:00",
            "2026-01-02T00:00",
            "2026-01-02T01:00",
            "2026-01-02T02:00",
            "2026-01-02T03:00",
            "2026-01-02T04:00",
            "2026-01-02T05:00",
            "2026-01-02T06:00",
            "2026-01-02T07:00",
            "2026-01-02T08:00",
            "2026-01-02T09:00",
            "2026-01-02T10:00",
            "2026-01-02T11:00",
            "2026-01-02T12:00",
            "2026-01-02T13:00",
            "2026-01-02T14:00",
            "2026-01-02T15:00",
            "2026-01-02T16:00",
            "2026-01-02T17:00",
            "2026-01-02T18:00",
            "2026-01-02T19:00",
            "2026-01-02T20:00",
            "2026-01-02T21:00",
            "2026-01-02T22:00",
            "2026-01-02T23:00",
            "2026-01-03T00:00",
            "2026-01-03T01:00",
            "2026-01-03T02:00",
            "2026-01-03T03:00",
            "2026-01-03T04:00",
            "2026-01-03T05:00",
            "2026-01-03T06:00",
            "2026-01-03T07:00",
            "2026-01-03T08:00",
            "2026-01-03T09:00",
            "2026-01-03T10:00",
            "2026-01-03T11:00",
            "2026-01-03T12:00",
            "2026-01-03T13:00",
            "2026-01-03T14:00",
            "2026-01-03T15:00",
            "2026-01-03T16:00",
            "2026-01-03T17:00",
            "2026-01-03T18:00",
            "2026-01-03T19:00",
            "2026-01-03T20:00",
            "2026-01-03T21:00",
            "2026-01-03T22:00",
            "2026-01-03T23:00",
            "2026-01-04T00:00",
            "2026-01-04T01:00",
            "2026-01-04T02:00",
            "2026-01-04T03:00",
            "2026-01-04T04:00",
            "2026-01-04T05:00",
            "2026-01-04T06:00",
            "2026-01-04T07:00",
            "2026-01-04T08:00",
            "2026-01-04T09:00",
            "2026-01-04T10:00",
            "2026-01-04T11:00",
            "2026-01-04T12:00",
            "2026-01-04T13:00",
            "2026-01-04T14:00",
            "2026-01-04T15:00",
            "2026-01-04T16:00",
            "2026-01-04T17:00",
            "2026-01-04T18:00",
            "2026-01-04T19:00",
            "2026-01-04T20:00",
            "2026-01-04T21:00",
            "2026-01-04T22:00",
            "2026-01-04T23:00"),
        temperature = listOf(
            25.8,
            25.5,
            25.1,
            24.8,
            24.6,
            24.4,
            24.8,
            26.9,
            28.7,
            30.2,
            31.1,
            32.0,
            33.1,
            32.8,
            32.3,
            31.6,
            30.4,
            29.0,
            27.6,
            26.7,
            26.1,
            26.0,
            25.9,
            25.6,
            25.3,
            24.9,
            24.4,
            24.1,
            23.6,
            23.1,
            23.3,
            26.1,
            27.8,
            30.0,
            31.2,
            31.9,
            31.0,
            29.9,
            30.6,
            30.2,
            28.5,
            27.6,
            26.9,
            26.5,
            26.3,
            26.2,
            26.2,
            25.9,
            25.7,
            25.5,
            25.4,
            25.5,
            25.2,
            25.0,
            25.2,
            26.0,
            27.2,
            28.5,
            29.8,
            31.1,
            31.0,
            29.6,
            28.8,
            28.2,
            27.3,
            26.8,
            26.4,
            26.0,
            25.9,
            25.8,
            25.7,
            25.7,
            25.5,
            25.2,
            25.1,
            24.9,
            24.9,
            24.7,
            24.7,
            25.9,
            26.7,
            27.5,
            28.2,
            29.4,
            31.2,
            33.0,
            33.4,
            30.9,
            28.9,
            28.0,
            27.1,
            26.4,
            25.9,
            25.6,
            25.3,
            25.1,
            24.9,
            24.7,
            24.5,
            24.4,
            24.4,
            24.8,
            25.4,
            26.2,
            27.6,
            29.3,
            30.4,
            30.8,
            30.7,
            30.4,
            29.7,
            28.7,
            27.9,
            27.4,
            26.9,
            26.6,
            26.3,
            26.1,
            26.0,
            25.8,
            25.6,
            25.5,
            25.5,
            25.5,
            25.7,
            25.9,
            26.1,
            26.6,
            27.6,
            28.7,
            29.8,
            30.6,
            31.3,
            31.8,
            30.0,
            29.2,
            28.3,
            27.4,
            26.4,
            25.6,
            25.2,
            25.0,
            24.8,
            24.6,
            24.5,
            24.4,
            24.1,
            23.8,
            23.8,
            24.1,
            24.7,
            25.7,
            27.3,
            29.3,
            30.9,
            31.8,
            32.3,
            32.4,
            31.7,
            30.6,
            29.5,
            28.5,
            27.5,
            26.7,
            26.3,
            26.1,
            25.9,
            25.6
        ),
        weatherCode = listOf(
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            2,
            2,
            3,
            2,
            2,
            3,
            3,
            3,
            3,
            2,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            45,
            45,
            3,
            3,
            3,
            2,
            3,
            3,
            3,
            3,
            3,
            3,
            80,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            95,
            95,
            95,
            95,
            95,
            80,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            80,
            96,
            95,
            95,
            95,
            80,
            80,
            80,
            45,
            45,
            45,
            45,
            45,
            45,
            3,
            3,
            3,
            3,
            3,
            3,
            96,
            96,
            96,
            95,
            95,
            95,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            3,
            80,
            80,
            80,
            3,
            3,
            3,
            45,
            45,
            45,
            45,
            45,
            45,
            45,
            45,
            45,
            2,
            2,
            2,
            2,
            2,
            2,
            3,
            3,
            3,
            80,
            80,
            80,
            3,
            3,
            3,
            3,
            3,
            3,
            3
        )
    ),
    daily = Daily(
        time = listOf(
            "2025-12-29",
            "2025-12-30",
            "2025-12-31",
            "2026-01-01",
            "2026-01-02",
            "2026-01-03",
            "2026-01-04"
        ),
        weatherCode = listOf(1, 3, 61, 0, 2, 80, 1),  // 7 hari
        sunrise = listOf(
            "2025-12-27T06:00:00",
            "2025-12-28T06:01:00",
            "2025-12-29T06:02:00",
            "2025-12-30T06:03:00",
            "2025-12-31T06:04:00",
            "2026-01-01T06:05:00",
            "2026-01-02T06:06:00"
        ),
        sunset = listOf(
            "2025-12-27T18:30:00",
            "2025-12-28T18:31:00",
            "2025-12-29T18:32:00",
            "2025-12-30T18:33:00",
            "2025-12-31T18:34:00",
            "2026-01-01T18:35:00",
            "2026-01-02T18:36:00"
        )
    ),
    timezone = "Asia/Jakarta",
    timezoneAbbreviation = "WIB"
)