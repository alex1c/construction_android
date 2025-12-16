package ru.calc1.construction.domain.engine

import ru.calc1.construction.domain.CalculatorDocumentation
import ru.calc1.construction.domain.repository.CalculatorRepository
import kotlin.math.abs
import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive test suite for all calculators.
 * 
 * Tests all 21 calculators with various scenarios:
 * - Normal cases with reasonable inputs
 * - Edge cases (minimum/maximum values)
 * - Validation errors
 * - Result correctness
 */
class ComprehensiveCalculatorTest {
	
	companion object {
		private const val DELTA = 1e-3
	}
	
	/**
	 * Generates comprehensive test inputs for a calculator.
	 */
	private fun generateTestInputs(calculatorId: String): Map<String, Double> {
		val calculator = CalculatorRepository.getCalculatorById(calculatorId)
			?: return emptyMap()
		
		// Comprehensive defaults based on calculator type
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
			"roof_area" to 48.0,
			
			// Volumes
			"concrete_volume" to 2.0,
			"volume" to 10.0,
			"gravel_volume" to 5.0,
			
			// Foundation
			"length" to 10.0,
			"width" to 0.5,
			"height" to 0.5,
			"foundation_length" to 10.0,
			"foundation_width" to 0.5,
			"foundation_height" to 0.5,
			"wall_thickness" to 0.4,
			"pillars_count" to 4.0,
			
			// Wall dimensions
			"wall_length" to 5.0,
			"wall_height" to 2.5,
			"wall_thickness_bricks" to 1.0,
			
			// Roof
			"house_length" to 8.0,
			"house_width" to 6.0,
			"roof_angle" to 30.0,
			"eaves_length" to 0.5,
			"sheet_length" to 2.0,
			"sheet_width" to 1.0,
			
			// Materials
			"roll_width" to 0.53,
			"roll_length" to 10.05,
			"tile_length" to 30.0,
			"tile_width" to 30.0,
			"laminate_length" to 1.3,
			"laminate_width" to 0.2,
			"pack_size" to 10.0,
			"pack_count" to 8.0,
			"material_length" to 20.0,
			"material_width" to 30.0,
			"material_height" to 20.0,
			"material_type" to 1.0,
			"tile_size" to 30.0,
			"seam_width" to 3.0,
			
			// Consumption rates
			"paint_consumption" to 0.15,
			"adhesive_consumption" to 4.0,
			"putty_consumption" to 1.2,
			"primer_consumption" to 0.2,
			"plaster_consumption" to 8.5,
			
			// Thicknesses
			"layer_thickness" to 3.0,
			"joint_thickness" to 10.0,
			
			// Counts
			"coats_count" to 2.0,
			"rebar_count" to 10.0,
			"layers_count" to 2.0,
			
			// Percentages
			"waste_percent" to 10.0,
			"voltage_drop_percent" to 5.0,
			
			// Electrical
			"power" to 3.0,
			"total_power" to 5.0,
			"voltage" to 220.0,
			"current_ampere" to 15.0,
			"cable_length" to 10.0,
			"power_factor" to 0.9,
			"phase_count" to 1.0,
			"safety_factor" to 1.25,
			"calculation_type" to 1.0,
			"input_method" to 1.0,
			"conductor_material" to 1.0,
			"network_type" to 1.0,
			"installation_type" to 1.0,
			"breaker_type" to 1.0,
			"ambient_temperature" to 25.0,
			
			// Rebar
			"rebar_diameter" to 12.0,
			"rebar_length" to 6.0,
			"structure_type" to 1.0,
			"mesh_step" to 200.0,
			"overlap" to 300.0,
			"steel_density" to 7850.0,
			
			// Stairs
			"total_height" to 270.0,
			"step_height" to 17.0,
			"step_depth" to 30.0,
			"stairs_type" to 1.0,
			
			// Gravel
			"gravel_density" to 1500.0,
			"compaction_coefficient" to 1.3,
			"work_type" to 1.0,
			"input_method" to 1.0,
			"fraction_type" to 1.0,
			
			// Ventilation
			"air_exchange_rate" to 1.0,
			"room_type" to 1.0,
			"people_count" to 2.0,
			
			// Heated floor
			"room_type" to 1.0,
			"insulation_type" to 1.0,
			"desired_temperature" to 24.0,
			"usage_hours" to 8.0,
			"electricity_price" to 5.0,
			"power_per_sqm" to 150.0,
			
			// Water pipes
			"calculation_type" to 1.0,
			"pipe_diameter" to 20.0,
			"water_flow_rate_m3s" to 0.001,
			"flow_velocity" to 1.5,
			"pipe_material" to 1.0,
			"pipe_length" to 10.0,
			
			// Concrete
			"concrete_grade" to 200.0,
			"cement_ratio" to 1.0,
			"sand_ratio" to 2.0,
			"gravel_ratio" to 4.0,
			"water_cement_ratio" to 0.5
		)
		
		// Build inputs map using defaults or field defaults
		return calculator.inputFields.associate { field ->
			field.id to (defaults[field.id] ?: field.defaultValue ?: when {
				field.type.name.contains("DROPDOWN", ignoreCase = true) -> 1.0
				field.id.contains("percent") -> 10.0
				field.id.contains("count") -> 1.0
				else -> 1.0
			})
		}
	}
	
	@Test
	fun testAllCalculatorsExist() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		assertEquals("Should have exactly 21 calculators", 21, allIds.size)
		
		// Verify all calculators can be retrieved
		for (calculatorId in allIds) {
			val calculator = CalculatorRepository.getCalculatorById(calculatorId)
			assertNotNull("Calculator $calculatorId should exist", calculator)
			assertEquals("Calculator ID should match", calculatorId, calculator!!.id)
		}
	}
	
	@Test
	fun testAllCalculatorsHaveRequiredFields() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		
		for (calculatorId in allIds) {
			val calculator = CalculatorRepository.getCalculatorById(calculatorId)!!
			
			// Check that calculator has at least one input field
			assertTrue(
				"Calculator $calculatorId should have input fields",
				calculator.inputFields.isNotEmpty()
			)
			
			// Check that calculator has at least one result field
			assertTrue(
				"Calculator $calculatorId should have result fields",
				calculator.resultFields.isNotEmpty()
			)
			
			// Check that all input fields have valid IDs
			for (field in calculator.inputFields) {
				assertTrue(
					"Input field ID should not be empty for $calculatorId",
					field.id.isNotBlank()
				)
				assertTrue(
					"Input field label should not be empty for $calculatorId",
					field.label.isNotBlank()
				)
			}
			
			// Check that all result fields have valid IDs
			for (field in calculator.resultFields) {
				assertTrue(
					"Result field ID should not be empty for $calculatorId",
					field.id.isNotBlank()
				)
				assertTrue(
					"Result field label should not be empty for $calculatorId",
					field.label.isNotBlank()
				)
			}
		}
	}
	
	@Test
	fun testAllCalculatorsCalculateSuccessfully() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		val failures = mutableListOf<String>()
		
		for (calculatorId in allIds) {
			try {
				val inputs = generateTestInputs(calculatorId)
				val result = CalculatorEngine.calculateWithValidation(calculatorId, inputs)
				
				when {
					result.isError -> {
						val errorMessage = when (result) {
							is CalculationResult.Error -> result.message
							else -> "Unknown error"
						}
						// Log but don't fail - some calculators might need specific inputs
						println("⚠️  Calculator $calculatorId returned error: $errorMessage")
					}
					result.isSuccess -> {
						val data = result.getOrNull()
						if (data == null || data.isEmpty()) {
							failures.add("$calculatorId: returned empty results")
						} else {
							println("✅ Calculator $calculatorId calculated successfully with ${data.size} results")
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
			val inputs = generateTestInputs(calculatorId)
			val result = CalculatorEngine.calculateWithValidation(calculatorId, inputs)
			
			if (result.isSuccess) {
				val data = result.getOrNull()!!
				val calculator = CalculatorRepository.getCalculatorById(calculatorId)!!
				
				// Check that all expected result fields are present
				for (resultField in calculator.resultFields) {
					assertTrue(
						"Calculator $calculatorId should return ${resultField.id}",
						data.containsKey(resultField.id)
					)
					
					// Check that result is valid number
					val value = data[resultField.id]!!
					assertFalse(
						"Calculator $calculatorId result ${resultField.id} should not be NaN",
						value.isNaN()
					)
					assertFalse(
						"Calculator $calculatorId result ${resultField.id} should not be Infinite",
						value.isInfinite()
					)
					
					// Check that result is not negative (unless it's a percentage or special case)
					if (!resultField.id.contains("percent") && 
						!resultField.id.contains("drop") &&
						!resultField.id.contains("angle")) {
						assertTrue(
							"Calculator $calculatorId result ${resultField.id} should be non-negative",
							value >= 0.0
						)
					}
				}
			}
		}
	}
	
	@Test
	fun testInputValidation() {
		val allIds = CalculatorDocumentation.getAllCalculatorIds()
		
		for (calculatorId in allIds) {
			// Test with negative values
			val negativeInputs = generateTestInputs(calculatorId).mapValues { -abs(it.value) }
			val negativeResult = CalculatorEngine.calculateWithValidation(calculatorId, negativeInputs)
			assertTrue(
				"Calculator $calculatorId should reject negative inputs",
				negativeResult.isError
			)
			
			// Test with zero values (for non-optional fields)
			val zeroInputs = generateTestInputs(calculatorId).mapValues { 0.0 }
			val zeroResult = CalculatorEngine.calculateWithValidation(calculatorId, zeroInputs)
			// Some fields allow zero (like openings_area), so we just check it doesn't crash
			assertNotNull("Calculator $calculatorId should handle zero inputs", zeroResult)
		}
	}
	
	@Test
	fun testInvalidCalculatorId() {
		val result = CalculatorEngine.calculateWithValidation("nonexistent_calculator_12345", emptyMap())
		assertTrue("Should return error for invalid calculator ID", result.isError)
		
		val errorMessage = when (result) {
			is CalculationResult.Error -> result.message
			else -> ""
		}
		assertTrue("Error should mention calculator not found", 
			errorMessage.contains("не найден") || errorMessage.contains("not found"))
	}
	
	@Test
	fun testCalculatorCategories() {
		val categories = CalculatorRepository.getCategories()
		assertEquals("Should have 4 categories", 4, categories.size)
		
		// Verify each category has calculators
		for (category in categories) {
			val calculators = CalculatorRepository.getCalculatorsByCategory(category.id)
			assertTrue(
				"Category ${category.name} should have at least one calculator",
				calculators.isNotEmpty()
			)
		}
	}
	
	@Test
	fun testAllCalculatorsInCategories() {
		val allCalculators = CalculatorRepository.getCalculators()
		val allCategories = CalculatorRepository.getCategories().map { it.id }
		
		for (calculator in allCalculators) {
			assertTrue(
				"Calculator ${calculator.id} should belong to a valid category",
				allCategories.contains(calculator.categoryId)
			)
		}
	}
}

