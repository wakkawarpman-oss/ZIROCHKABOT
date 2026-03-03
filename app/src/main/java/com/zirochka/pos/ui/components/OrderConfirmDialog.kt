package com.zirochka.pos.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun OrderConfirmDialog(
    total: Double,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Підтвердити") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Скасувати") }
        },
        title = { Text(text = "Підтвердити замовлення") },
        text = { Text(text = "Разом: ${"%.2f".format(total)} грн. Надіслати?") }
    )
}
