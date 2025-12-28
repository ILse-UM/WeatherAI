package com.univ.android.weatherai.ui.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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

@Preview
@Composable
private fun WeatherScreenPreview() {
    WeatherSuccessContent(dummyWeather, "") { }

}

val dummyWeather = Weather(
    latitude = -6.2,
    longitude = 106.8,
    current = Current(
        temperature = 28.5,
        humidity = 75.0,
        precipitation = 0.0,
        rain = 0.0,
        apparentTemperature = 32.0,
        weatherCode = 1  // partly cloudy
    ),
    hourly = Hourly(
        temperature = listOf(28.0, 29.0, 30.0, 31.0, 30.5, 29.0, 28.0, 27.5) + // 24 jam
                List(16) { 27.0 + it * 0.5 },  // isi sisanya
        weatherCode = listOf(1, 2, 3, 3, 2, 1, 0, 0) +
                List(16) { 1 }
    ),
    daily = Daily(
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