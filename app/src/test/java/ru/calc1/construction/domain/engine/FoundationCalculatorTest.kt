package ru.calc1.construction.domain.engine

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for foundation calculator.
 * Also tests consistency with concrete calculator.
 */
class FoundationCalculatorTest {
	
	companion object {
		private const val DELTA = 1e-2
	}
	
	@Test
	fun testNormalCase() {
		val inputs = mapOf(
			"foundation_length" to 10.0,
			"foundation_width" to 0.5,
			"foundation_height" to 0.5,
			"rebar_diameter" to 12.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("foundation", inputs)
		assertTrue("Calculation should succeed", result.isSuccess)
		val data = result.getOrNull()!!
		
		assertTrue("Should have concrete_volume", data.containsKey("concrete_volume"))
		assertTrue("Should have rebar_mass", data.containsKey("rebar_mass"))
		assertTrue("Should have formwork_area", data.containsKey("formwork_area"))
		
		// Expected: volume = 10 * 0.5 * 0.5 = 2.5 m³
		val volume = data["concrete_volume"]!!
		assertEquals("Volume should be 2.5 m³", 2.5, volume, DELTA)
		
		// Rebar ≈ 100 kg/m³
		val rebar = data["rebar_mass"]!!
		assertEquals("Rebar should be ~250 kg", 250.0, rebar, 50.0) // Allow ±50 kg tolerance
		
		// Formwork = 2 * (10 + 0.5) * 0.5 = 10.5 m²
		val formwork = data["formwork_area"]!!
		assertEquals("Formwork should be 10.5 m²", 10.5, formwork, DELTA)
	}
	
	@Test
	fun testConsistencyWithConcreteCalculator() {
		// Foundation calculation
		val foundationInputs = mapOf(
			"foundation_length" to 5.0,
			"foundation_width" to 0.4,
			"foundation_height" to 0.4,
			"rebar_diameter" to 12.0
		)
		
		val foundationResult = CalculatorEngine.calculateWithValidation("foundation", foundationInputs)
		assertTrue("Foundation calculation should succeed", foundationResult.isSuccess)
		val foundationData = foundationResult.getOrNull()!!
		val concreteVolume = foundationData["concrete_volume"]!!
		
		// Use this volume in concrete calculator
		val concreteInputs = mapOf(
			"concrete_volume" to concreteVolume,
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		val concreteResult = CalculatorEngine.calculateWithValidation("concrete", concreteInputs)
		assertTrue("Concrete calculation should succeed", concreteResult.isSuccess)
		val concreteData = concreteResult.getOrNull()!!
		
		// Verify that concrete calculator accepts the volume from foundation calculator
		assertTrue("Concrete calculator should produce results", concreteData.isNotEmpty())
		
		// The volume should match
		// Foundation: 5 * 0.4 * 0.4 = 0.8 m³
		assertEquals("Volumes should match", 0.8, concreteVolume, DELTA)
	}
	
	@Test
	fun testZeroDimensions() {
		val inputs = mapOf(
			"foundation_length" to 0.0,
			"foundation_width" to 0.5,
			"foundation_height" to 0.5,
			"rebar_diameter" to 12.0
		)
		
		val result = CalculatorEngine.calculateWithValidation("foundation", inputs)
		assertTrue("Should return error", result.isError)
	}
}



