package com.construction.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.construction.domain.model.CalculatorCategory
import com.construction.ui.util.CalculatorIcons

/**
 * Section displaying all calculator categories.
 *
 * @param categories List of categories to display
 * @param onCategoryClick Callback when a category is tapped
 */
@Composable
fun CategorySection(
	categories: List<CalculatorCategory>,
	onCategoryClick: (String) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = "Категории",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 12.dp)
		)
		
		Column(
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			categories.forEach { category ->
				CategoryItem(
					category = category,
					onClick = { onCategoryClick(category.id) }
				)
			}
		}
	}
}

/**
 * Item displaying a single category with icon.
 *
 * @param category Category to display
 * @param onClick Callback when the category is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryItem(
	category: CalculatorCategory,
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
			// Category icon with colored background
			Box(
				modifier = Modifier
					.size(56.dp),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = CalculatorIcons.getCategoryIcon(category.id),
					contentDescription = category.name,
					modifier = Modifier.size(32.dp),
					tint = Color(CalculatorIcons.getCategoryColor(category.id))
				)
			}
			
			Spacer(modifier = Modifier.width(16.dp))
			
			// Category info
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = category.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = category.description,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

