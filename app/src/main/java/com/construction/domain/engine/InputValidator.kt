package com.construction.domain.engine

import com.construction.domain.model.InputFieldType

/**
 * Validates input values for calculators.
 * Provides consistent validation rules across all calculators.
 */
object InputValidator {
	
	// Reasonable maximum limits
	private const val MAX_LENGTH = 100.0 // meters
	private const val MAX_AREA = 10000.0 // m²
	private const val MAX_VOLUME = 10000.0 // m³
	private const val MAX_COUNT = 1000000.0
	private const val MIN_LENGTH = 0.01 // meters
	private const val MIN_AREA = 0.01 // m²
	private const val MIN_VOLUME = 0.001 // m³
	
	/**
	 * Validates a single input value based on its type.
	 * @param value Value to validate
	 * @param fieldId ID of the input field (for error messages)
	 * @param type Type of the input field
	 * @param allowZero Whether zero is allowed (default false)
	 * @param minValue Optional minimum value override
	 * @param maxValue Optional maximum value override
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
	 * @param inputs Map of field IDs to values
	 * @param requiredFields List of field IDs that must be present and valid
	 * @param fieldTypes Map of field IDs to their types
	 * @param allowZero Map of field IDs to whether zero is allowed
	 * @return First error found, or null if all valid
	 */
	fun validateInputs(
		inputs: Map<String, Double>,
		requiredFields: List<String>,
		fieldTypes: Map<String, InputFieldType>,
		allowZero: Map<String, Boolean> = emptyMap()
	): CalculationResult.Error? {
		// Check required fields are present
		for (fieldId in requiredFields) {
			if (!inputs.containsKey(fieldId)) {
				return CalculationResult.Error(
					message = "$ErrorMessages.MISSING_REQUIRED: $fieldId",
					fieldId = fieldId,
					errorType = CalculationResult.ErrorType.MISSING_INPUT
				)
			}
		}
		
		// Validate each input
		for ((fieldId, value) in inputs) {
			val type = fieldTypes[fieldId] ?: InputFieldType.NUMBER
			val allowZeroForField = allowZero[fieldId] ?: false
			
			val error = validateInput(value, fieldId, type, allowZeroForField)
			if (error != null) {
				return CalculationResult.Error(
					message = error,
					fieldId = fieldId,
					errorType = CalculationResult.ErrorType.VALIDATION
				)
			}
		}
		
		return null
	}
}

