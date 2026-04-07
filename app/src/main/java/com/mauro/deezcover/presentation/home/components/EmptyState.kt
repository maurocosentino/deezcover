package com.mauro.deezcover.presentation.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mauro.deezcover.R

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Sync, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(40.dp).padding(bottom = 8.dp))
            Text(stringResource(R.string.no_songs_available), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.6f))
            Text(stringResource(R.string.connect_to_sync), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.35f))
        }
    }
}
