package com.construction.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.construction.R
import com.construction.util.ShareHelper

/**
 * About screen displaying app information and share app functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
	onNavigateUp: () -> Unit
) {
	val context = LocalContext.current
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.about)) },
				navigationIcon = {
					IconButton(onClick = onNavigateUp) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Назад"
						)
					}
				}
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// App name
			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold
			)
			
			// Description
			Text(
				text = stringResource(R.string.about_description),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			Divider()
			
			// Share app button
			Button(
				onClick = {
					val rustoreUrl = context.getString(R.string.rustore_url)
					val shareText = context.getString(R.string.share_app_message, rustoreUrl)
					ShareHelper.shareText(context, shareText)
				},
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(
					containerColor = Color(0xFF39FF14), // Ярко кислотно-зеленый
					contentColor = Color.Black
				)
			) {
				Icon(
					imageVector = Icons.Default.Share,
					contentDescription = null,
					modifier = Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(stringResource(R.string.share_app))
			}
			
			// Version info (placeholder)
			Text(
				text = "Версия 1.0.0",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

