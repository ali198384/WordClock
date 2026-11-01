package com.ghazarian.wordclock.feature.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.model.WordClockDevice

@Composable
fun DevicesRoute(
    viewModel: DevicesViewModel,
    onDeviceSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DevicesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onDeviceSelected = onDeviceSelected
    )
}

@Composable
private fun DevicesScreen(
    state: DevicesUiState,
    onRefresh: () -> Unit,
    onDeviceSelected: (String) -> Unit,
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                text = "دستگاه‌های Word Clock",
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "دستگاه موردنظرت را برای مشاهدهٔ وضعیت انتخاب کن.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            when {
                state.isLoading && state.devices.isEmpty() -> LoadingContent()
                state.devices.isEmpty() && state.errorMessage != null -> ErrorContent(
                    message = state.errorMessage,
                    onRetry = onRefresh,
                )

                state.devices.isEmpty() -> EmptyDevicesContent()
                else -> {
                    state.errorMessage?.let { message ->
                        Text(
                            text = "به‌روزرسانی فهرست ناموفق بود: $message",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.devices, key = WordClockDevice::id) { device ->
                            DeviceCard(
                                device = device,
                                onClick = { onDeviceSelected(device.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyDevicesContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("هنوز دستگاهی اضافه نشده", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("تلاش دوباره")
        }
    }
}

@Composable
private fun DeviceCard(
    device: WordClockDevice,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(device.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = device.address.host,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
