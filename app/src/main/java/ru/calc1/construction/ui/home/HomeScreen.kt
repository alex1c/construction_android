package ru.calc1.construction.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.calc1.construction.domain.model.CalculatorCategory
import ru.calc1.construction.domain.model.CalculatorDefinition
import ru.calc1.construction.domain.repository.CalculatorRepository

/**
 * Main home screen displaying popular calculators, categories, and search functionality.
 *
 * @param onCategoryClick Callback when a category is tapped
 * @param onCalculatorClick Callback when a calculator is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	onCategoryClick: (String) -> Unit,
	onCalculatorClick: (String) -> Unit
) {
	var searchText by remember { mutableStateOf("") }
	
	// Get all calculators and categories
	val allCalculators = remember { CalculatorRepository.getCalculators() }
	val categories = remember { CalculatorRepository.getCategories() }
	
	// Filter calculators based on search text
	// Recalculate whenever searchText changes
	val filteredCalculators = remember(searchText) {
		val query = searchText.trim()
		if (query.isBlank()) {
			emptyList()
		} else {
			val queryLower = query.lowercase()
			allCalculators.filter { calculator ->
				// Search in name (case-insensitive)
				calculator.name.lowercase().contains(queryLower) ||
				// Search in description (case-insensitive)
				calculator.shortDescription.lowercase().contains(queryLower) ||
				// Search in ID (for English names, case-insensitive)
				calculator.id.lowercase().contains(queryLower)
			}
		}
	}
	
	// Popular calculator IDs
	val popularCalculatorIds = remember {
		listOf("concrete", "plaster", "wallpaper", "paint", "foundation", "electrical")
	}
	
	val popularCalculators = remember {
		allCalculators.filter { it.id in popularCalculatorIds }
	}
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Строительные калькуляторы") }
			)
		}
	) { paddingValues ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
			contentPadding = PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Search field
			item {
				SearchField(
					value = searchText,
					onValueChange = { searchText = it },
					modifier = Modifier.fillMaxWidth()
				)
			}
			
			// Search results section
			if (searchText.isNotBlank()) {
				if (filteredCalculators.isEmpty()) {
					item {
						Text(
							text = "Ничего не найдено",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.padding(vertical = 16.dp)
						)
					}
				} else {
					item {
						Text(
							text = "Результаты поиска",
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(bottom = 8.dp)
						)
					}
					items(filteredCalculators) { calculator ->
						CalculatorItem(
							calculator = calculator,
							onClick = { onCalculatorClick(calculator.id) }
						)
					}
				}
			} else {
				// Popular section
				item {
					PopularSection(
						calculators = popularCalculators,
						onCalculatorClick = onCalculatorClick
					)
				}
				
				// Categories section
				item {
					CategorySection(
						categories = categories,
						onCategoryClick = onCategoryClick
					)
				}
			}
		}
	}
}

/**
 * Search text field component.
 */
@Composable
private fun SearchField(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	OutlinedTextField(
		value = value,
		onValueChange = onValueChange,
		modifier = modifier,
		label = { Text("Поиск калькуляторов") },
		placeholder = { Text("Введите название калькулятора...") },
		singleLine = true,
		leadingIcon = {
			Icon(
				imageVector = Icons.Default.Search,
				contentDescription = "Поиск"
			)
		},
		trailingIcon = {
			if (value.isNotBlank()) {
				IconButton(onClick = { onValueChange("") }) {
					Icon(
						imageVector = Icons.Default.Close,
						contentDescription = "Очистить"
					)
				}
			}
		},
		colors = OutlinedTextFieldDefaults.colors(
			focusedBorderColor = MaterialTheme.colorScheme.primary,
			unfocusedBorderColor = MaterialTheme.colorScheme.outline
		)
	)
}

