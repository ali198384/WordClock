package com.ghazarian.wordclock.feature.control.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ghazarian.wordclock.core.model.WordClockState

@Composable
fun ControlRoute(
    viewModel: ControlViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ControlScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
    )
}

@Composable
private fun ControlScreen(
    state: ControlUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            TextButton(onClick = onBack) {
                Text("بازگشت به دستگاه‌ها")
            }
            Spacer(Modifier.height(8.dp))
            Text("وضعیت Word Clock", style = MaterialTheme.typography.headlineMedium)
            state.deviceId?.let { deviceId ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = deviceId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))

            when {
                state.isLoading && state.deviceState == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("در حال خواندن وضعیت دستگاه...")
                    }
                }

                state.errorMessage != null && state.deviceState == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRefresh) {
                            Text("تلاش دوباره")
                        }
                    }
                }

                state.deviceState != null -> {
                    state.errorMessage?.let { message ->
                        Text(
                            text = "به‌روزرسانی ناموفق بود: $message",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    DeviceStateCard(state.deviceState)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRefresh, enabled = !state.isLoading) {
                        Text(if (state.isLoading) "در حال دریافت..." else "به‌روزرسانی وضعیت")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceStateCard(state: WordClockState) {
    val colorHex = listOf(state.color.red, state.color.green, state.color.blue)
        .joinToString(separator = "") { it.toString(16).padStart(2, '0') }
        .uppercase()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StateRow(label = "برق", value = if (state.isOn) "روشن" else "خاموش")
            StateRow(label = "روشنایی", value = "${state.brightness} از 255")
            StateRow(label = "رنگ", value = "#$colorHex")
        }
    }
}

@Composable
private fun StateRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
