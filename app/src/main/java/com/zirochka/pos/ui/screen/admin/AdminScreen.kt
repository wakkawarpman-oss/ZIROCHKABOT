package com.zirochka.pos.ui.screen.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val syncing by viewModel.syncing.collectAsState()
    val lastSync by viewModel.lastSync.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Адмін-панель",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Останнє оновлення: $lastSync",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(
            onClick = viewModel::syncMenu,
            enabled = !syncing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (syncing) CircularProgressIndicator()
            else Text("Перечитати меню з assets")
        }
        Text(
            text = "BuildConfig налаштування читаються з local.properties або gradle.properties.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
