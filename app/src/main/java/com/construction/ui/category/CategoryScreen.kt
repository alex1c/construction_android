package com.construction.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.construction.domain.repository.CalculatorRepository
import com.construction.ui.home.CalculatorList

/**
 * Screen displaying calculators for a specific category.
 *
 * @param categoryId ID of the category to display
 * @param onCalculatorClick Callback when a calculator is tapped
 * @param onNavigateUp Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
	categoryId: String,
	onCalculatorClick: (String) -> Unit,
	onNavigateUp: () -> Unit
) {
	// Get category and calculators
	val category = remember(categoryId) {
		CalculatorRepository.getCategories().firstOrNull { it.id == categoryId }
	}
	val calculators = remember(categoryId) {
		CalculatorRepository.getCalculatorsByCategory(categoryId)
	}
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(category?.name ?: "Категория") },
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
		if (calculators.isEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.padding(16.dp),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = "В этой категории пока нет калькуляторов",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		} else {
			CalculatorList(
				calculators = calculators,
				onCalculatorClick = onCalculatorClick,
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.padding(horizontal = 16.dp)
			)
		}
	}
}

