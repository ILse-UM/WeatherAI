package com.univ.android.weatherai.ui.weather

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.univ.android.weatherai.data.remote.ApiClient
import com.univ.android.weatherai.data.repository.WeatherRepository
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
fun WeatherSuccessContent(
    weather: Weather,
    summary: String? = null,
    onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Current Weather",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${weather.current?.temperature}°C",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "${weather.current?.weatherCode}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (!summary.isNullOrBlank()){
                    Text(
                        text = summary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                Text(
                    text = "Lat: ${weather.latitude}, Lon: ${weather.longitude}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onRefresh) {
            Text("Refresh")
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
