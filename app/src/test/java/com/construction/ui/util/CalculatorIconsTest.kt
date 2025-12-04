package com.construction.ui.util

import com.construction.domain.CalculatorDocumentation
import com.construction.domain.repository.CalculatorRepository
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
			assertTrue("Category ${category.name} should have a valid color", color > 0)
			// Check that color is in valid ARGB range
			assertTrue("Category ${category.name} color should be valid ARGB", color <= 0xFFFFFFFF)
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

