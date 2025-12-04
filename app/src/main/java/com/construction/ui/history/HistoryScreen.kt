package com.construction.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.construction.R
import com.construction.util.ShareHelper

/**
 * Simple data model for history entry.
 * Stored in SharedPreferences (no database needed).
 */
data class HistoryEntry(
	val id: String,
	val calculatorId: String,
	val calculatorName: String,
	val inputValues: Map<String, String>,
	val results: Map<String, Double>,
	val timestamp: Long = System.currentTimeMillis()
)

/**
 * History screen displaying saved calculations.
 * Uses SharedPreferences for storage (no database needed).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
	onNavigateUp: () -> Unit
) {
	val context = LocalContext.current
	val viewModel: HistoryViewModel = viewModel { HistoryViewModel(context) }
	val historyEntries by viewModel.historyEntries.collectAsState()
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.history)) },
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
		if (historyEntries.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = stringResource(R.string.history_empty),
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		} else {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues),
				contentPadding = PaddingValues(16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(historyEntries) { entry ->
					HistoryEntryCard(
						entry = entry,
						onShare = {
							val shareText = buildHistoryShareText(entry, context)
							ShareHelper.shareText(context, shareText)
						}
					)
				}
			}
		}
	}
}

/**
 * Card displaying a single history entry.
 */
@Composable
private fun HistoryEntryCard(
	entry: HistoryEntry,
	onShare: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				Text(
					text = entry.calculatorName,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				
				// Show first result as summary
				entry.results.entries.firstOrNull()?.let { (_, value) ->
					Text(
						text = "${String.format("%.2f", value)}",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.primary
					)
				}
			}
			
			IconButton(
				onClick = onShare,
				colors = IconButtonDefaults.iconButtonColors(
					contentColor = Color(0xFF39FF14) // Ярко кислотно-зеленый
				)
			) {
				Icon(
					imageVector = Icons.Default.Share,
					contentDescription = stringResource(R.string.share_calculation)
				)
			}
		}
	}
}

/**
 * Builds share text from a history entry.
 */
private fun buildHistoryShareText(entry: HistoryEntry, context: android.content.Context): String {
	val builder = StringBuilder()
	
	builder.append(entry.calculatorName)
	builder.append("\n\n")
	
	builder.append("Параметры:\n")
	entry.inputValues.forEach { (key, value) ->
		if (value.isNotBlank()) {
			builder.append("• $key: $value\n")
		}
	}
	
	builder.append("\n")
	
	builder.append("Результаты:\n")
	entry.results.forEach { (key, value) ->
		builder.append("• $key: ${String.format("%.2f", value)}\n")
	}
	
	builder.append("\n")
	builder.append(context.getString(R.string.share_calculation_footer))
	
	return builder.toString()
}
