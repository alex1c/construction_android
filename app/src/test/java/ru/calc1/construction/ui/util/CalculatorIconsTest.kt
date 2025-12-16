package ru.calc1.construction.ui.util

import ru.calc1.construction.domain.CalculatorDocumentation
import ru.calc1.construction.domain.repository.CalculatorRepository
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for CalculatorIcons utility.
 * 
 * Verifies that all calculators and categories have icons assigned.
 */
class CalculatorIconsTest {
	
	@Test
	fun testAllCalculatorsHaveIcons() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		
		for (calculatorId in allIds) {
			val icon = CalculatorIcons.getIcon(calculatorId)
			assertNotNull("Calculator $calculatorId should have an icon", icon)
		}
	}
	
	@Test
	fun testAllCategoriesHaveIcons() {
		val categories = CalculatorRepository.getCategories()
		
		for (category in categories) {
			val icon = CalculatorIcons.getCategoryIcon(category.id)
			assertNotNull("Category ${category.name} should have an icon", icon)
		}
	}
	
	@Test
	fun testAllCategoriesHaveColors() {
		val categories = CalculatorRepository.getCategories()
		
		for (category in categories) {
			val color = CalculatorIcons.getCategoryColor(category.id)
			// ARGB colors are 32-bit unsigned values (0x00000000 to 0xFFFFFFFF)
			// When stored as Long, we need to check the lower 32 bits
			val colorAsUnsigned = color and 0xFFFFFFFFL
			// Format as 8-digit hex (ARGB format) for display
			val colorHex = colorAsUnsigned.toString(16).uppercase().padStart(8, '0')
			assertTrue(
				"Category ${category.name} should have a valid color (got: 0x$colorHex)",
				colorAsUnsigned > 0L
			)
			// Check that color is in valid ARGB range (0x00000000 to 0xFFFFFFFF)
			assertTrue(
				"Category ${category.name} color should be valid ARGB (got: 0x$colorHex)",
				colorAsUnsigned <= 0xFFFFFFFFL
			)
			// Verify alpha channel is set (most significant byte should be 0xFF for opaque colors)
			val alpha = (colorAsUnsigned shr 24) and 0xFF
			val alphaHex = alpha.toString(16).uppercase().padStart(2, '0')
			assertTrue(
				"Category ${category.name} color should have alpha channel set (got: 0x$alphaHex)",
				alpha > 0
			)
		}
	}
	
	@Test
	fun testDefaultIconForUnknownCalculator() {
		val icon = CalculatorIcons.getIcon("unknown_calculator_xyz")
		assertNotNull("Should return default icon for unknown calculator", icon)
	}
	
	@Test
	fun testDefaultIconForUnknownCategory() {
		val icon = CalculatorIcons.getCategoryIcon("unknown_category_xyz")
		assertNotNull("Should return default icon for unknown category", icon)
	}
	
	@Test
	fun testCategoryColorsAreDistinct() {
		val categories = CalculatorRepository.getCategories()
		val colors = categories.map { CalculatorIcons.getCategoryColor(it.id) }.toSet()
		
		// All categories should have distinct colors
		assertEquals("All categories should have distinct colors", categories.size, colors.size)
	}
}

