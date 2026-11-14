package com.ghazarian.wordclock.feature.devices.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ghazarian.wordclock.resources.MR
import com.ghazarian.wordclock.resources.cancel
import com.ghazarian.wordclock.resources.device_name_label
import com.ghazarian.wordclock.resources.remove_action
import com.ghazarian.wordclock.resources.remove_dialog_message
import com.ghazarian.wordclock.resources.remove_dialog_title
import com.ghazarian.wordclock.resources.removing
import com.ghazarian.wordclock.resources.rename_dialog_title
import com.ghazarian.wordclock.resources.save
import com.ghazarian.wordclock.resources.saving

@Composable
internal fun RenameDeviceDialog(
    name: String,
    errorMessage: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.rename_dialog_title.resourceId)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(MR.strings.device_name_label.resourceId)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                )
                errorMessage?.let { ErrorNotice(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isSaving) {
                Text(stringResource(if (isSaving) MR.strings.saving.resourceId else MR.strings.save.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(MR.strings.cancel.resourceId))
            }
        },
    )
}

@Composable
internal fun RemoveDeviceDialog(
    deviceName: String,
    isRemoving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(MR.strings.remove_dialog_title.resourceId)) },
        text = { Text(stringResource(MR.strings.remove_dialog_message.resourceId, deviceName)) },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isRemoving) {
                Text(stringResource(if (isRemoving) MR.strings.removing.resourceId else MR.strings.remove_action.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isRemoving) {
                Text(stringResource(MR.strings.cancel.resourceId))
            }
        },
    )
}
