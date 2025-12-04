package com.construction.domain.engine

import com.construction.domain.CalculatorDocumentation
import org.junit.Assert.*
import org.junit.Test

/**
 * Smoke test for CalculatorEngine.
 * Tests that all calculators can handle reasonable input values without crashing.
 * This acts as a regression test to ensure future changes don't break existing calculators.
 */
class CalculatorEngineSmokeTest {
	
	/**
	 * Generates reasonable input values for a calculator based on its field types.
	 */
	private fun generateReasonableInputs(calculatorId: String): Map<String, Double> {
		// Default reasonable values based on field IDs
		val defaults = mapOf(
			// Room dimensions
			"room_length" to 4.0,
			"room_width" to 3.0,
			"room_height" to 2.7,
			"floor_height" to 3.0,
			
			// Areas
			"wall_area" to 30.0,
			"surface_area" to 20.0,
			"tile_area" to 15.0,
			"floor_area" to 12.0,
			"area" to 50.0,
			"openings_area" to 5.0,
			
			// Volumes
			"concrete_volume" to 2.0,
			
			// Foundation
			"foundation_length" to 10.0,
			"foundation_width" to 0.5,
			"foundation_height" to 0.5,
			
			// Wall dimensions
			"wall_length" to 5.0,
			"wall_height" to 2.5,
			"wall_thickness" to 0.4,
			
			// Roof
			"roof_length" to 8.0,
			"roof_width" to 6.0,
			"roof_angle" to 30.0,
			"material_weight" to 5.0,
			
			// Materials
			"roll_width" to 0.53,
			"roll_length" to 10.0,
			"tile_length" to 30.0,
			"tile_width" to 30.0,
			"laminate_length" to 1.3,
			"laminate_width" to 0.2,
			"pack_count" to 8.0,
			"block_length" to 20.0,
			"block_width" to 30.0,
			"block_height" to 20.0,
			"material_type" to 1.0,
			
			// Consumption rates
			"paint_consumption" to 0.15,
			"adhesive_consumption" to 4.0,
			"putty_consumption" to 1.2,
			"primer_consumption" to 0.2,
			"plaster_consumption" to 8.5,
			
			// Thicknesses
			"layer_thickness" to 3.0,
			
			// Counts
			"coats_count" to 2.0,
			"rebar_count" to 10.0,
			
			// Percentages
			"waste_percent" to 10.0,
			"voltage_drop_percent" to 5.0,
			
			// Electrical
			"power" to 3.0,
			"total_power" to 5.0,
			"voltage" to 220.0,
			"cable_length" to 10.0,
			"power_factor" to 0.9,
			"phase_count" to 1.0,
			"safety_factor" to 1.25,
			
			// Rebar
			"rebar_diameter" to 12.0,
			"rebar_length" to 6.0,
			"steel_density" to 7850.0,
			
			// Stairs
			"step_height" to 17.0,
			"step_width" to 30.0,
			
			// Gravel
			"gravel_density" to 1500.0,
			"compaction_coefficient" to 1.3,
			
			// Ventilation
			"air_exchange_rate" to 1.0,
			
			// Heated floor
			"power_per_sqm" to 150.0,
			"usage_hours" to 8.0,
			"electricity_price" to 5.0,
			
			// Water pipes
			"pipe_diameter" to 20.0,
			"pipe_length" to 10.0,
			"flow_velocity" to 1.5,
			"roughness" to 0.1,
			
			// Concrete grades
			"cement_grade" to 400.0,
			"concrete_grade" to 200.0
		)
		
		// Get calculator definition to know which fields it needs
		val calculator = com.construction.domain.repository.CalculatorRepository.getCalculatorById(calculatorId)
			?: return mapOf()
		
		// Build inputs map with defaults or field defaults
		return calculator.inputFields.associate { field ->
			field.id to (defaults[field.id] ?: field.defaultValue ?: 1.0)
		}
	}
	
	@Test
	fun testAllCalculatorsExist() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		assertEquals("Should have 21 calculators", 21, allIds.size)
	}
	
	@Test
	fun testAllCalculatorsHandleReasonableInputs() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		val failures = mutableListOf<String>()
		
		for (calculatorId in allIds) {
			try {
				val inputs = generateReasonableInputs(calculatorId)
				val result = CalculatorEngine.calculateWithValidation(calculatorId, inputs)
				
				when {
					result.isError -> {
						// Some calculators might fail validation with defaults, that's OK
						// But we should log it
						val errorMessage = when (result) {
							is CalculationResult.Error -> result.message
							else -> "Unknown error"
						}
						println("Calculator $calculatorId returned error: $errorMessage")
					}
					result.isSuccess -> {
						val data = result.getOrNull()
						if (data == null || data.isEmpty()) {
							failures.add("$calculatorId: returned empty results")
						}
					}
				}
			} catch (e: Exception) {
				failures.add("$calculatorId: threw exception ${e.javaClass.simpleName}: ${e.message}")
			}
		}
		
		if (failures.isNotEmpty()) {
			fail("Some calculators failed:\n${failures.joinToString("\n")}")
		}
	}
	
	@Test
	fun testAllCalculatorsReturnValidResults() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		
		for (calculatorId in allIds) {
			val inputs = generateReasonableInputs(calculatorId)
			val result = CalculatorEngine.calculateWithValidation(calculatorId, inputs)
			
			if (result.isSuccess) {
				val data = result.getOrNull()!!
				val calculator = com.construction.domain.repository.CalculatorRepository.getCalculatorById(calculatorId)!!
				
				// Check that all expected result fields are present
				for (resultField in calculator.resultFields) {
					assertTrue(
						"Calculator $calculatorId should return ${resultField.id}",
						data.containsKey(resultField.id)
					)
					
					// Check that result is not NaN or Infinite
					val value = data[resultField.id]!!
					assertFalse(
						"Calculator $calculatorId result ${resultField.id} should not be NaN",
						value.isNaN()
					)
					assertFalse(
						"Calculator $calculatorId result ${resultField.id} should not be Infinite",
						value.isInfinite()
					)
				}
			}
		}
	}
	
	@Test
	fun testInvalidCalculatorId() {
		val result = CalculatorEngine.calculateWithValidation("nonexistent_calculator", emptyMap())
		assertTrue("Should return error for invalid calculator ID", result.isError)
	}
}

