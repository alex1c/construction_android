package com.construction.domain.engine

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for wallpaper calculator.
 * Tests normal cases, boundary cases, invalid inputs, and rounding behavior.
 */
class WallpaperCalculatorTest {
	
	companion object {
		private const val DELTA = 1e-6
	}
	
	@Test
	fun testNormalCase() {
		// Typical room: 4m x 3m x 2.7m, standard roll 0.53m x 10.05m
		// Window: 1.2×1.5 m, Door: 0.9×2.0 m = 3.6 m²
		val inputs = mapOf(
			"room_length" to 4.0,
			"room_width" to 3.0,
			"room_height" to 2.7,
			"openings_area" to 3.6,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		// Expected: wall area = 2 * (4 + 3) * 2.7 = 37.8 m²
		// Useful area = 37.8 - 3.6 = 34.2 m²
		// Roll area = 0.53 * 10.05 = 5.3265 m²
		// Area with waste = 34.2 * 1.1 = 37.62 m²
		// Rolls needed = 37.62 / 5.3265 = 7.06, rounded UP = 8
		assertTrue("Should have rolls_count", data.containsKey("rolls_count"))
		val rollsCount = data["rolls_count"]!!
		assertEquals("Should need 8 rolls", 8.0, rollsCount, DELTA)
	}
	
	@Test
	fun testSmallRoom() {
		// Small room: 2m x 2m x 2.5m, no openings
		val inputs = mapOf(
			"room_length" to 2.0,
			"room_width" to 2.0,
			"room_height" to 2.5,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		// Wall area = 2 * (2 + 2) * 2.5 = 20 m²
		// Useful area = 20 - 0 = 20 m²
		// Roll area = 0.53 * 10.05 = 5.3265 m²
		// Area with waste = 20 * 1.1 = 22 m²
		// Rolls = 22 / 5.3265 = 4.13, rounded UP = 5
		assertTrue("Should need at least 1 roll", data["rolls_count"]!! >= 1.0)
		assertEquals("Should need 5 rolls", 5.0, data["rolls_count"]!!, DELTA)
	}
	
	@Test
	fun testLargeRoom() {
		// Large room: 8m x 6m x 3m
		val inputs = mapOf(
			"room_length" to 8.0,
			"room_width" to 6.0,
			"room_height" to 3.0,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		// Wall area = 2 * (8 + 6) * 3 = 84 m²
		// Useful area = 84 - 0 = 84 m²
		// Roll area = 0.53 * 10.05 = 5.3265 m²
		// Area with waste = 84 * 1.1 = 92.4 m²
		// Rolls = 92.4 / 5.3265 = 17.34, rounded UP = 18
		assertTrue("Should need multiple rolls", data["rolls_count"]!! > 10.0)
	}
	
	@Test
	fun testRoundingUp() {
		// Room that needs exactly 2.1 rolls should round to 3
		val inputs = mapOf(
			"room_length" to 2.0,
			"room_width" to 1.5,
			"room_height" to 2.5,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		// Wall area = 2 * (2 + 1.5) * 2.5 = 17.5 m²
		// Useful area = 17.5 - 0 = 17.5 m²
		// Roll area = 0.53 * 10.05 = 5.3265 m²
		// Area with waste = 17.5 * 1.1 = 19.25 m²
		// Rolls = 19.25 / 5.3265 = 3.61, rounded UP = 4
		val rollsCount = data["rolls_count"]!!
		assertTrue("Should round UP (not down)", rollsCount >= 3.61)
		assertEquals("Should be 4 rolls", 4.0, rollsCount, DELTA)
	}
	
	@Test
	fun testNegativeLength() {
		val inputs = mapOf(
			"room_length" to -4.0,
			"room_width" to 3.0,
			"room_height" to 2.7,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Should return error", result.isError)
		val errorMessage = when (result) {
			is CalculationResult.Error -> result.message
			else -> ""
		}
		assertTrue("Error should mention negative value", 
			errorMessage.contains("положительное") || errorMessage.contains("отрицательное"))
	}
	
	@Test
	fun testZeroLength() {
		val inputs = mapOf(
			"room_length" to 0.0,
			"room_width" to 3.0,
			"room_height" to 2.7,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Should return error", result.isError)
	}
	
	@Test
	fun testMissingInput() {
		val inputs = mapOf(
			"room_length" to 4.0,
			"room_width" to 3.0
			// Missing room_height, roll_width, roll_length
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Should return error", result.isError)
		val errorMessage = when (result) {
			is CalculationResult.Error -> result.message
			else -> ""
		}
		assertTrue("Error should mention missing field", 
			errorMessage.contains("Не заполнено") || errorMessage.contains("missing"))
	}
	
	@Test
	fun testZeroRollArea() {
		val inputs = mapOf(
			"room_length" to 4.0,
			"room_width" to 3.0,
			"room_height" to 2.7,
			"openings_area" to 0.0,
			"roll_width" to 0.0,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
		assertTrue("Should return error for zero roll width", result.isError)
	}
	
	@Test
	fun testProportionalScaling() {
		// Test that doubling all dimensions quadruples the area (2²)
		val inputs1 = mapOf(
			"room_length" to 2.0,
			"room_width" to 2.0,
			"room_height" to 2.5,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val inputs2 = mapOf(
			"room_length" to 4.0,
			"room_width" to 4.0,
			"room_height" to 5.0,
			"openings_area" to 0.0,
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"waste_percent" to 10.0
		)
		
		val result1 = CalculatorEngine.calculateWithValidation("wallpaper", inputs1)
		val result2 = CalculatorEngine.calculateWithValidation("wallpaper", inputs2)
		
		assertTrue("Both should succeed", result1.isSuccess && result2.isSuccess)
		
		val rolls1 = result1.getOrNull()!!["rolls_count"]!!
		val rolls2 = result2.getOrNull()!!["rolls_count"]!!
		
		// When dimensions double, area quadruples, so rolls should roughly quadruple
		// (allowing for rounding differences)
		assertTrue("Rolls should increase significantly", rolls2 > rolls1 * 3.5)
		assertTrue("Rolls should not increase more than 5x", rolls2 < rolls1 * 5.0)
	}
}

