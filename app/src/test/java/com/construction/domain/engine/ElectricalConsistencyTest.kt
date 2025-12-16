package com.construction.domain.engine

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests consistency between cable_section and electrical calculators.
 * Both should produce compatible results for the same inputs.
 */
class ElectricalConsistencyTest {
	
	companion object {
		private const val DELTA = 1e-1 // Allow 0.1 A tolerance
	}
	
	@Test
	fun testConsistencyForSinglePhase() {
		// Same inputs for both calculators
		val power = 5.0 // 5 kW
		val voltage = 220.0 // 220 V
		val cableLength = 10.0 // 10 m
		
		// Cable section calculator
		val cableInputs = mapOf(
			"power" to power,
			"voltage" to voltage,
			"cable_length" to cableLength,
			"power_factor" to 0.9,
			"voltage_drop_percent" to 5.0
		)
		
		// Electrical calculator (1-phase)
		val electricalInputs = mapOf(
			"total_power" to power,
			"voltage" to voltage,
			"phase_count" to 1.0,
			"cable_length" to cableLength,
			"safety_factor" to 1.25
		)
		
		val cableResult = CalculatorEngine.calculateWithValidation("cable_section", cableInputs)
		val electricalResult = CalculatorEngine.calculateWithValidation("electrical", electricalInputs)
		
		assertTrue("Both should succeed", cableResult.isSuccess && electricalResult.isSuccess)
		
		val cableData = cableResult.getOrNull()!!
		val electricalData = electricalResult.getOrNull()!!
		
		val cableCurrent = cableData["current"]!!
		val electricalCurrent = electricalData["current"]!!
		
		// For single phase: I = P / (U * cos φ)
		// Cable: I = 5000 / (220 * 0.9) ≈ 25.25 A
		// Electrical: I = 5000 / 220 ≈ 22.73 A (no power factor)
		// They should be close but not identical (different formulas)
		assertTrue("Currents should be in similar range (20-30A)", 
			cableCurrent in 20.0..30.0 && electricalCurrent in 20.0..30.0)
		
		// Cable sections should be similar
		val cableSection = cableData["cable_section"]!!
		val electricalSection = electricalData["cable_section"]!!
		
		assertTrue("Cable sections should be similar", 
			kotlin.math.abs(cableSection - electricalSection) < 2.0) // Within 2 mm²
	}
	
	@Test
	fun testBasicPhysicsRelations() {
		// Test P = U * I (for single phase, ignoring power factor)
		val power = 2.2 // 2.2 kW = 2200 W
		val voltage = 220.0 // 220 V
		
		val inputs = mapOf(
			"total_power" to power,
			"voltage" to voltage,
			"phase_count" to 1.0,
			"cable_length" to 5.0,
			"safety_factor" to 1.25
		)
		
		val result = CalculatorEngine.calculateWithValidation("electrical", inputs)
		assertTrue("Should succeed", result.isSuccess)
		
		val data = result.getOrNull()!!
		val current = data["current"]!!
		
		// P = U * I, so I = P / U = 2200 / 220 = 10 A
		assertEquals("Current should be 10A", 10.0, current, DELTA)
	}
	
	@Test
	fun testThreePhaseCurrent() {
		// For 3-phase: I = P / (U * √3)
		val power = 6.6 // 6.6 kW
		val voltage = 380.0 // 380 V (line voltage)
		
		val inputs = mapOf(
			"total_power" to power,
			"voltage" to voltage,
			"phase_count" to 3.0,
			"cable_length" to 10.0,
			"safety_factor" to 1.25
		)
		
		val result = CalculatorEngine.calculateWithValidation("electrical", inputs)
		assertTrue("Should succeed", result.isSuccess)
		
		val data = result.getOrNull()!!
		val current = data["current"]!!
		
		// I = 6600 / (380 * √3) ≈ 10.03 A
		val expectedCurrent = 6600.0 / (380.0 * kotlin.math.sqrt(3.0))
		assertEquals("3-phase current should match formula", expectedCurrent, current, DELTA)
	}
}



