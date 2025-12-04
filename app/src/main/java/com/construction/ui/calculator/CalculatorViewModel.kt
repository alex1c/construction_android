package com.construction.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.construction.domain.engine.CalculatorEngine
import com.construction.domain.model.CalculatorDefinition
import com.construction.domain.repository.CalculatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the calculator screen.
 * Manages calculator state, input values, and calculation results.
 *
 * @param calculatorId ID of the calculator to display
 */
class CalculatorViewModel(calculatorId: String) : ViewModel() {
	
	// Calculator definition loaded from repository
	private val _calculator = MutableStateFlow<CalculatorDefinition?>(null)
	val calculator: StateFlow<CalculatorDefinition?> = _calculator.asStateFlow()
	
	// Input field values (stored as strings for text fields)
	private val _inputValues = MutableStateFlow<Map<String, String>>(emptyMap())
	val inputValues: StateFlow<Map<String, String>> = _inputValues.asStateFlow()
	
	// Calculation results
	private val _results = MutableStateFlow<Map<String, Double>>(emptyMap())
	val results: StateFlow<Map<String, Double>> = _results.asStateFlow()
	
	// Error state
	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error.asStateFlow()
	
	init {
		loadCalculator(calculatorId)
	}
	
	/**
	 * Loads calculator definition from repository and initializes input values.
	 */
	private fun loadCalculator(calculatorId: String) {
		viewModelScope.launch {
			val calculator = CalculatorRepository.getCalculatorById(calculatorId)
			_calculator.value = calculator
			
			// Initialize input values with defaults or empty strings
			if (calculator != null) {
				val initialValues = calculator.inputFields.associate { field ->
					field.id to (field.defaultValue?.toString() ?: "")
				}
				_inputValues.value = initialValues
			}
		}
	}
	
	/**
	 * Updates the value of an input field.
	 */
	fun updateInput(fieldId: String, value: String) {
		val currentValues = _inputValues.value.toMutableMap()
		currentValues[fieldId] = value
		_inputValues.value = currentValues
		_error.value = null // Clear error when user types
	}
	
	/**
	 * Validates all input fields and performs calculation.
	 */
	fun calculate() {
		val calculator = _calculator.value ?: return
		
		// Validate all inputs
		val numericInputs = mutableMapOf<String, Double>()
		val errors = mutableListOf<String>()
		
		calculator.inputFields.forEach { field ->
			val valueStr = _inputValues.value[field.id] ?: ""
			
			if (valueStr.isBlank()) {
				errors.add("Поле '${field.label}' не заполнено")
				return@forEach
			}
			
			try {
				val value = valueStr.toDouble()
				
				// Check min/max constraints
				field.minValue?.let { min ->
					if (value < min) {
						errors.add("Значение '${field.label}' меньше минимального ($min)")
						return@forEach
					}
				}
				
				field.maxValue?.let { max ->
					if (value > max) {
						errors.add("Значение '${field.label}' больше максимального ($max)")
						return@forEach
					}
				}
				
				numericInputs[field.id] = value
			} catch (e: NumberFormatException) {
				errors.add("Неверное значение в поле '${field.label}'")
			}
		}
		
		if (errors.isNotEmpty()) {
			_error.value = errors.joinToString("\n")
			return
		}
		
		// Perform calculation
		viewModelScope.launch {
			try {
				val calculatedResults = CalculatorEngine.calculate(calculator.id, numericInputs)
				_results.value = calculatedResults
				_error.value = null
			} catch (e: Exception) {
				_error.value = "Ошибка при расчёте: ${e.message}"
			}
		}
	}
	
	/**
	 * Clears all input values and results.
	 */
	fun clear() {
		val calculator = _calculator.value ?: return
		val clearedValues = calculator.inputFields.associate { field ->
			field.id to (field.defaultValue?.toString() ?: "")
		}
		_inputValues.value = clearedValues
		_results.value = emptyMap()
		_error.value = null
	}
}


