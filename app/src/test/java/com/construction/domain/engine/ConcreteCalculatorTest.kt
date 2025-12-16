package com.construction.domain.engine

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for concrete calculator.
 */
class ConcreteCalculatorTest {
	
	companion object {
		private const val DELTA = 1e-2 // Allow 0.01 kg tolerance
	}
	
	@Test
	fun testNormalCase() {
		val inputs = mapOf(
			"concrete_volume" to 1.0, // 1 m³
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("concrete", inputs)
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		assertTrue("Should have cement_mass", data.containsKey("cement_mass"))
		assertTrue("Should have sand_mass", data.containsKey("sand_mass"))
		assertTrue("Should have gravel_mass", data.containsKey("gravel_mass"))
		assertTrue("Should have water_volume", data.containsKey("water_volume"))
		
		// For M200: density ≈ 2400 kg/m³, ratios 1:2:4:0.5
		// Total mass = 2400 kg
		// Cement = 2400 * 1/7 ≈ 343 kg
		// Sand = 2400 * 2/7 ≈ 686 kg
		// Gravel = 2400 * 4/7 ≈ 1371 kg
		// Water = 2400 * 0.5/7 / 1000 ≈ 0.17 m³ = 171 liters
		
		val cement = data["cement_mass"]!!
		val sand = data["sand_mass"]!!
		val gravel = data["gravel_mass"]!!
		val water = data["water_volume"]!!
		
		assertTrue("Cement should be positive", cement > 0)
		assertTrue("Sand should be positive", sand > 0)
		assertTrue("Gravel should be positive", gravel > 0)
		assertTrue("Water should be positive", water > 0)
		
		// Check ratios are approximately correct
		assertEquals("Sand should be ~2x cement", cement * 2.0, sand, cement * 0.5)
		assertEquals("Gravel should be ~4x cement", cement * 4.0, gravel, cement * 1.0)
	}
	
	@Test
	fun testProportionalScaling() {
		// Test that doubling volume doubles all components
		val inputs1 = mapOf(
			"concrete_volume" to 1.0,
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val inputs2 = mapOf(
			"concrete_volume" to 2.0,
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val result1 = CalculatorEngine.calculateWithValidation("concrete", inputs1)
		val result2 = CalculatorEngine.calculateWithValidation("concrete", inputs2)
		
		assertTrue("Both should succeed", result1.isSuccess && result2.isSuccess)
		
		val data1 = result1.getOrNull()!!
		val data2 = result2.getOrNull()!!
		
		val cement1 = data1["cement_mass"]!!
		val cement2 = data2["cement_mass"]!!
		
		assertEquals("Cement should double", cement1 * 2.0, cement2, cement1 * 0.1)
	}
	
	@Test
	fun testZeroVolume() {
		val inputs = mapOf(
			"concrete_volume" to 0.0,
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("concrete", inputs)
		assertTrue("Should return error", result.isError)
	}
	
	@Test
	fun testNegativeVolume() {
		val inputs = mapOf(
			"concrete_volume" to -1.0,
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("concrete", inputs)
		assertTrue("Should return error", result.isError)
	}
}



