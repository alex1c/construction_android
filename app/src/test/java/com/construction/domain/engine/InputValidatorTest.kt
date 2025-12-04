package com.construction.domain.engine

import com.construction.domain.model.InputFieldType
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for InputValidator.
 * 
 * Tests validation logic for different input field types and edge cases.
 */
class InputValidatorTest {
	
	@Test
	fun testLengthValidation() {
		// Valid length
		assertNull(InputValidator.validateInput(5.0, "length", InputFieldType.LENGTH))
		
		// Too small
		assertNotNull(InputValidator.validateInput(0.001, "length", InputFieldType.LENGTH))
		
		// Too large
		assertNotNull(InputValidator.validateInput(200.0, "length", InputFieldType.LENGTH))
		
		// Negative
		assertNotNull(InputValidator.validateInput(-1.0, "length", InputFieldType.LENGTH))
		
		// Zero (not allowed by default)
		assertNotNull(InputValidator.validateInput(0.0, "length", InputFieldType.LENGTH))
		
		// Zero (allowed)
		assertNull(InputValidator.validateInput(0.0, "openings_area", InputFieldType.AREA, allowZero = true))
	}
	
	@Test
	fun testAreaValidation() {
		// Valid area
		assertNull(InputValidator.validateInput(20.0, "area", InputFieldType.AREA))
		
		// Too small
		assertNotNull(InputValidator.validateInput(0.001, "area", InputFieldType.AREA))
		
		// Too large
		assertNotNull(InputValidator.validateInput(20000.0, "area", InputFieldType.AREA))
		
		// Negative
		assertNotNull(InputValidator.validateInput(-1.0, "area", InputFieldType.AREA))
	}
	
	@Test
	fun testVolumeValidation() {
		// Valid volume
		assertNull(InputValidator.validateInput(10.0, "volume", InputFieldType.VOLUME))
		
		// Too small
		assertNotNull(InputValidator.validateInput(0.0001, "volume", InputFieldType.VOLUME))
		
		// Too large
		assertNotNull(InputValidator.validateInput(20000.0, "volume", InputFieldType.VOLUME))
		
		// Negative
		assertNotNull(InputValidator.validateInput(-1.0, "volume", InputFieldType.VOLUME))
	}
	
	@Test
	fun testPercentValidation() {
		// Valid percent
		assertNull(InputValidator.validateInput(50.0, "percent", InputFieldType.PERCENT))
		assertNull(InputValidator.validateInput(0.0, "percent", InputFieldType.PERCENT))
		assertNull(InputValidator.validateInput(100.0, "percent", InputFieldType.PERCENT))
		
		// Invalid percent
		assertNotNull(InputValidator.validateInput(-1.0, "percent", InputFieldType.PERCENT))
		assertNotNull(InputValidator.validateInput(101.0, "percent", InputFieldType.PERCENT))
	}
	
	@Test
	fun testIntegerValidation() {
		// Valid integer
		assertNull(InputValidator.validateInput(5.0, "count", InputFieldType.INTEGER))
		
		// Not an integer
		assertNotNull(InputValidator.validateInput(5.5, "count", InputFieldType.INTEGER))
		
		// Zero (not allowed)
		assertNotNull(InputValidator.validateInput(0.0, "count", InputFieldType.INTEGER))
		
		// Negative
		assertNotNull(InputValidator.validateInput(-1.0, "count", InputFieldType.INTEGER))
	}
	
	@Test
	fun testNullValidation() {
		// Null value should fail
		assertNotNull(InputValidator.validateInput(null, "field", InputFieldType.NUMBER))
	}
	
	@Test
	fun testNaNValidation() {
		// NaN should fail
		assertNotNull(InputValidator.validateInput(Double.NaN, "field", InputFieldType.NUMBER))
	}
	
	@Test
	fun testInfiniteValidation() {
		// Infinity should fail
		assertNotNull(InputValidator.validateInput(Double.POSITIVE_INFINITY, "field", InputFieldType.NUMBER))
		assertNotNull(InputValidator.validateInput(Double.NEGATIVE_INFINITY, "field", InputFieldType.NUMBER))
	}
	
	@Test
	fun testDropdownValidation() {
		// Valid dropdown value
		assertNull(InputValidator.validateInput(1.0, "type", InputFieldType.DROPDOWN))
		
		// Negative dropdown value should fail
		assertNotNull(InputValidator.validateInput(-1.0, "type", InputFieldType.DROPDOWN))
	}
	
	@Test
	fun testMultipleInputsValidation() {
		val inputs = mapOf(
			"length" to 5.0,
			"width" to 3.0,
			"height" to 2.7
		)
		
		val requiredFields = listOf("length", "width", "height")
		val fieldTypes = mapOf(
			"length" to InputFieldType.LENGTH,
			"width" to InputFieldType.LENGTH,
			"height" to InputFieldType.LENGTH
		)
		
		// Valid inputs
		assertNull(InputValidator.validateInputs(inputs, requiredFields, fieldTypes))
		
		// Missing required field
		val incompleteInputs = mapOf("length" to 5.0, "width" to 3.0)
		val error = InputValidator.validateInputs(incompleteInputs, requiredFields, fieldTypes)
		assertNotNull("Should return error for missing field", error)
		assertTrue("Error should be MISSING_INPUT", error is CalculationResult.Error)
	}
}

