package ru.calc1.construction.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.calc1.construction.domain.model.CalculatorDefinition
import ru.calc1.construction.ui.util.CalculatorIcons

/**
 * Section displaying popular calculators in a horizontal scrollable list.
 *
 * @param calculators List of popular calculators to display
 * @param onCalculatorClick Callback when a calculator is tapped
 */
@Composable
fun PopularSection(
	calculators: List<CalculatorDefinition>,
	onCalculatorClick: (String) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = "Популярные",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 12.dp)
		)
		
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			contentPadding = PaddingValues(horizontal = 4.dp)
		) {
			items(calculators) { calculator ->
				PopularCalculatorCard(
					calculator = calculator,
					onClick = { onCalculatorClick(calculator.id) }
				)
			}
		}
	}
}

/**
 * Card displaying a popular calculator with icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PopularCalculatorCard(
	calculator: CalculatorDefinition,
	onClick: () -> Unit
) {
	Card(
		onClick = onClick,
		modifier = Modifier
			.width(160.dp)
			.height(140.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp),
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Icon at the top
			Icon(
				imageVector = CalculatorIcons.getIcon(calculator.id),
				contentDescription = calculator.name,
				modifier = Modifier.size(48.dp),
				tint = Color(CalculatorIcons.getCategoryColor(calculator.categoryId))
			)
			
			// Calculator name and description
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = calculator.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold,
					maxLines = 2
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = calculator.shortDescription,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 2
				)
			}
		}
	}
}

