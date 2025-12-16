package ru.calc1.construction.ui.category

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
import ru.calc1.construction.domain.premium.AccessControl
import ru.calc1.construction.domain.repository.CalculatorRepository
import ru.calc1.construction.ui.home.CalculatorList
import ru.calc1.construction.ui.premium.PremiumStub

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
	
	// Check access to category
	// TODO: Enable Premium gating after RuStore billing launch
	val hasAccess = category?.let { AccessControl.isCategoryAccessible(it) } ?: false
	
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
		// Show PremiumStub if access is denied
		// Currently, hasAccess is always true since isPremiumUser = true
		if (!hasAccess && category != null) {
			PremiumStub(
				contentName = category.name,
				onNavigateUp = onNavigateUp
			)
		} else if (calculators.isEmpty()) {
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

