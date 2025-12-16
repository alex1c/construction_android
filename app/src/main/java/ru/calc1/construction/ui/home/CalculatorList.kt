package ru.calc1.construction.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.calc1.construction.domain.model.CalculatorDefinition
import ru.calc1.construction.ui.util.CalculatorIcons

/**
 * Reusable component for displaying a list of calculators.
 *
 * @param calculators List of calculators to display
 * @param onCalculatorClick Callback when a calculator is tapped
 */
@Composable
fun CalculatorList(
	calculators: List<CalculatorDefinition>,
	onCalculatorClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(vertical = 8.dp)
	) {
		items(calculators) { calculator ->
			CalculatorItem(
				calculator = calculator,
				onClick = { onCalculatorClick(calculator.id) }
			)
		}
	}
}

/**
 * Item displaying a single calculator with icon.
 *
 * @param calculator Calculator to display
 * @param onClick Callback when the calculator is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorItem(
	calculator: CalculatorDefinition,
	onClick: () -> Unit
) {
	Card(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Calculator icon
			Icon(
				imageVector = CalculatorIcons.getIcon(calculator.id),
				contentDescription = calculator.name,
				modifier = Modifier.size(48.dp),
				tint = Color(CalculatorIcons.getCategoryColor(calculator.categoryId))
			)
			
			Spacer(modifier = Modifier.width(16.dp))
			
			// Calculator info
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = calculator.name,
					style = MaterialTheme.typography.titleMedium
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = calculator.shortDescription,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

