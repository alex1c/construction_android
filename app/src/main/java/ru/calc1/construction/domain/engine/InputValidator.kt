package ru.calc1.construction.domain.engine

import ru.calc1.construction.domain.model.InputFieldType

/**
 * Validates input values for calculators.
 * 
 * Provides consistent validation rules across all calculators to ensure
 * data integrity and prevent invalid calculations. Uses type-specific
 * validation based on InputFieldType.
 * 
 * Validation Rules:
 * - Null values: Rejected (required fields must have values)
 * - NaN/Infinite: Rejected (invalid numeric values)
 * - Negative values: Rejected (except for fields that explicitly allow them)
 * - Zero values: Rejected by default (unless allowZero = true)
 * - Type-specific ranges: Enforced based on field type
 * 
 * Type-Specific Validation:
 * - LENGTH: 0.01 m to 100 m (reasonable building dimensions)
 * - AREA: 0.01 m² to 10,000 m² (reasonable room/area sizes)
 * - VOLUME: 0.001 m³ to 10,000 m³ (reasonable material volumes)
 * - INTEGER: Must be whole number, 1 to 1,000,000
 * - PERCENT: 0 to 100
 * - POWER: 0 to 10,000 W (10 MW max)
 * - DROPDOWN: Pre-validated by selection, just check non-negative
 * - NUMBER/MASS/FLOW: Generic numeric validation with reasonable max
 * 
 * Architecture:
 * - Uses object singleton pattern (no instance needed)
 * - Stateless validation (pure functions)
 * - Returns error messages for user-friendly feedback
 */
object InputValidator {
	
	/**
	 * Reasonable maximum and minimum limits for different measurement types.
	 * These limits prevent unrealistic input values that could cause calculation errors.
	 */
	// Reasonable maximum limits
	private const val MAX_LENGTH = 100.0 // meters (reasonable building dimension)
	private const val MAX_AREA = 10000.0 // m² (reasonable room/area size)
	private const val MAX_VOLUME = 10000.0 // m³ (reasonable material volume)
	private const val MAX_COUNT = 1000000.0 // Maximum count for integer fields
	
	// Reasonable minimum limits (prevent division by zero and unrealistic values)
	private const val MIN_LENGTH = 0.01 // meters (1 cm minimum)
	private const val MIN_AREA = 0.01 // m² (100 cm² minimum)
	private const val MIN_VOLUME = 0.001 // m³ (1 liter minimum)
	
	/**
	 * Validates a single input value based on its type.
	 * 
	 * Performs comprehensive validation including:
	 * 1. Null check (required fields)
	 * 2. NaN/Infinite check (invalid numeric values)
	 * 3. Negative value check
	 * 4. Zero value check (if not allowed)
	 * 5. Type-specific range validation
	 * 
	 * @param value Value to validate (can be null for optional fields)
	 * @param fieldId ID of the input field (used in error messages)
	 * @param type Type of the input field (determines validation rules)
	 * @param allowZero Whether zero is allowed (default false)
	 * @param minValue Optional minimum value override (overrides type default)
	 * @param maxValue Optional maximum value override (overrides type default)
	 * @return Error message if validation fails, null if valid
	 */
	fun validateInput(
		value: Double?,
		fieldId: String,
		type: InputFieldType,
		allowZero: Boolean = false,
		minValue: Double? = null,
		maxValue: Double? = null
	): String? {
		if (value == null) {
			return "$ErrorMessages.MISSING_REQUIRED: $fieldId"
		}
		
		if (value.isNaN() || value.isInfinite()) {
			return ErrorMessages.INVALID_RANGE
		}
		
		// Check for negative values
		if (value < 0) {
			return ErrorMessages.NEGATIVE_VALUE
		}
		
		// Check for zero
		if (!allowZero && value == 0.0) {
			return ErrorMessages.ZERO_NOT_ALLOWED
		}
		
		// Type-specific validation
		when (type) {
			InputFieldType.LENGTH -> {
				val min = minValue ?: MIN_LENGTH
				val max = maxValue ?: MAX_LENGTH
				if (value < min) return ErrorMessages.TOO_SMALL
				if (value > max) return ErrorMessages.TOO_LARGE
			}
			InputFieldType.AREA -> {
				val min = minValue ?: MIN_AREA
				val max = maxValue ?: MAX_AREA
				if (value < min) return ErrorMessages.TOO_SMALL
				if (value > max) return ErrorMessages.TOO_LARGE
			}
			InputFieldType.VOLUME -> {
				val min = minValue ?: MIN_VOLUME
				val max = maxValue ?: MAX_VOLUME
				if (value < min) return ErrorMessages.TOO_SMALL
				if (value > max) return ErrorMessages.TOO_LARGE
			}
			InputFieldType.INTEGER -> {
				if (value != value.toInt().toDouble()) {
					return "Значение должно быть целым числом"
				}
				if (value < 1) return ErrorMessages.ZERO_NOT_ALLOWED
				if (value > MAX_COUNT) return ErrorMessages.TOO_LARGE
			}
			InputFieldType.PERCENT -> {
				if (value < 0 || value > 100) {
					return ErrorMessages.INVALID_PERCENT
				}
			}
			InputFieldType.POWER -> {
				if (value < 0) return ErrorMessages.NEGATIVE_VALUE
				if (value > 10000.0) return ErrorMessages.TOO_LARGE // 10 MW max
			}
			InputFieldType.DROPDOWN -> {
				// Dropdown values are pre-validated by selection, just check it's not negative
				if (value < 0) return ErrorMessages.NEGATIVE_VALUE
			}
			InputFieldType.NUMBER, InputFieldType.MASS, InputFieldType.FLOW -> {
				// Generic numeric validation
				if (value < 0) return ErrorMessages.NEGATIVE_VALUE
				val max = maxValue ?: 1000000.0
				if (value > max) return ErrorMessages.TOO_LARGE
			}
		}
		
		return null // Valid
	}
	
	/**
	 * Validates multiple input values at once.
	 * 
	 * This method is used by CalculatorEngine to validate all inputs before
	 * performing calculations. It returns the first error found, allowing
	 * the UI to display a specific error message to the user.
	 * 
	 * Validation Process:
	 * 1. Check all required fields are present
	 * 2. Validate each input value individually
	 * 3. Return first error found (if any)
	 * 4. Return null if all validations pass
	 * 
	 * Error Types:
	 * - MISSING_INPUT: Required field is missing
	 * - VALIDATION: Field value fails validation rules
	 * 
	 * @param inputs Map of field IDs to their numeric values
	 * @param requiredFields List of field IDs that must be present and valid
	 * @param fieldTypes Map of field IDs to their types (for type-specific validation)
	 * @param allowZero Map of field IDs to whether zero is allowed (for fields like "openings_area", "waste_percent")
	 * @return First error found as CalculationResult.Error, or null if all valid
	 */
	fun validateInputs(
		inputs: Map<String, Double>,
		requiredFields: List<String>,
		fieldTypes: Map<String, InputFieldType>,
		allowZero: Map<String, Boolean> = emptyMap()
	): CalculationResult.Error? {
		// Step 1: Check all required fields are present
		// Missing required fields cause immediate validation failure
		for (fieldId in requiredFields) {
			if (!inputs.containsKey(fieldId)) {
				return CalculationResult.Error(
					message = "$ErrorMessages.MISSING_REQUIRED: $fieldId",
					fieldId = fieldId,
					errorType = CalculationResult.ErrorType.MISSING_INPUT
				)
			}
		}
		
		// Step 2: Validate each input value
		// Uses type-specific validation rules and field-specific constraints
		for ((fieldId, value) in inputs) {
			// Get field type (default to NUMBER if not specified)
			val type = fieldTypes[fieldId] ?: InputFieldType.NUMBER
			// Check if zero is allowed for this field
			val allowZeroForField = allowZero[fieldId] ?: false
			
			// Perform validation
			val error = validateInput(value, fieldId, type, allowZeroForField)
			if (error != null) {
				// Return first error found
				return CalculationResult.Error(
					message = error,
					fieldId = fieldId,
					errorType = CalculationResult.ErrorType.VALIDATION
				)
			}
		}
		
		// All validations passed
		return null
	}
}

