package ru.calc1.construction.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility object for providing calculator icons.
 * 
 * Maps calculator IDs to appropriate Material Design icons.
 * Each calculator has a unique icon that represents its purpose.
 * 
 * Icon Selection:
 * - Icons are chosen to visually represent the calculator's function
 * - Material Design Icons are used for consistency
 * - Icons are grouped by category for easy identification
 */
object CalculatorIcons {
	
	/**
	 * Gets the icon for a calculator by its ID.
	 * 
	 * @param calculatorId ID of the calculator
	 * @return Material Design icon vector, or default icon if not found
	 */
	fun getIcon(calculatorId: String): ImageVector {
		return when (calculatorId) {
			// Finishing & Interior Calculators
			"wallpaper" -> Icons.Default.Image
			"paint" -> Icons.Default.FormatPaint
			"tile_adhesive" -> Icons.Default.Construction
			"putty" -> Icons.Default.Build
			"primer" -> Icons.Default.Layers
			"plaster" -> Icons.Default.Home
			"wall_area" -> Icons.Default.SquareFoot
			"tile" -> Icons.Default.GridOn
			"laminate" -> Icons.Default.ViewQuilt
			
			// Structures & Concrete Calculators
			"foundation" -> Icons.Default.Home
			"concrete" -> Icons.Default.Business
			"roof" -> Icons.Default.Home
			"brick_blocks" -> Icons.Default.Home
			"stairs" -> Icons.Default.TrendingUp
			"gravel" -> Icons.Default.Circle
			
			// Engineering Systems Calculators
			"ventilation" -> Icons.Default.AcUnit
			"heated_floor" -> Icons.Default.WaterDrop
			"water_pipes" -> Icons.Default.Build
			
			// Metal & Electricity Calculators
			"rebar" -> Icons.Default.Build
			"cable_section" -> Icons.Default.Power
			"electrical" -> Icons.Default.Power
			
			// Default icon for unknown calculators
			else -> Icons.Default.Calculate
		}
	}
	
	/**
	 * Gets the icon for a category by its ID.
	 * 
	 * @param categoryId ID of the category
	 * @return Material Design icon vector, or default icon if not found
	 */
	fun getCategoryIcon(categoryId: String): ImageVector {
		return when (categoryId) {
			"finishing_interior" -> Icons.Default.Home
			"structures_concrete" -> Icons.Default.Business
			"engineering_systems" -> Icons.Default.Settings
			"metal_electricity" -> Icons.Default.Power
			else -> Icons.Default.Category
		}
	}
	
	/**
	 * Gets a color tint for the icon based on category.
	 * 
	 * @param categoryId ID of the category
	 * @return Color value as Long (ARGB format)
	 */
	fun getCategoryColor(categoryId: String): Long {
		return when (categoryId) {
			"finishing_interior" -> 0xFF4CAF50      // Green
			"structures_concrete" -> 0xFF2196F3     // Blue
			"engineering_systems" -> 0xFFFF9800     // Orange
			"metal_electricity" -> 0xFF9C27B0       // Purple
			else -> 0xFF757575                     // Gray
		}
	}
}

