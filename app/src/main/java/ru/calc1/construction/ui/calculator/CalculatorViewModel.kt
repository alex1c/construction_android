package ru.calc1.construction.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.calc1.construction.domain.engine.CalculatorEngine
import ru.calc1.construction.domain.model.CalculatorDefinition
import ru.calc1.construction.domain.repository.CalculatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the calculator screen.
 * 
 * Manages calculator state, input values, and calculation results using StateFlow
 * for reactive UI updates. Follows MVVM architecture pattern.
 * 
 * Responsibilities:
 * - Loading calculator definition from repository
 * - Managing input field values (as strings for text fields)
 * - Validating user inputs
 * - Performing calculations via CalculatorEngine
 * - Storing calculation results and detailed descriptions
 * - Managing error states
 * 
 * State Management:
 * - Uses StateFlow for reactive state updates
 * - All state changes trigger UI recomposition automatically
 * - State survives configuration changes (screen rotation)
 * 
 * @param calculatorId ID of the calculator to display (e.g., "wallpaper", "concrete")
 */
class CalculatorViewModel(calculatorId: String) : ViewModel() {
	
	/**
	 * Calculator definition loaded from repository.
	 * Contains all metadata: input fields, result fields, examples, etc.
	 * Null until calculator is loaded from repository.
	 */
	private val _calculator = MutableStateFlow<CalculatorDefinition?>(null)
	val calculator: StateFlow<CalculatorDefinition?> = _calculator.asStateFlow()
	
	/**
	 * Input field values stored as strings (for text field compatibility).
	 * Map structure: fieldId -> value as String
	 * Example: "room_length" -> "4.5"
	 */
	private val _inputValues = MutableStateFlow<Map<String, String>>(emptyMap())
	val inputValues: StateFlow<Map<String, String>> = _inputValues.asStateFlow()
	
	/**
	 * Calculation results as numeric values.
	 * Map structure: resultFieldId -> calculated value as Double
	 * Example: "rolls_count" -> 8.0
	 */
	private val _results = MutableStateFlow<Map<String, Double>>(emptyMap())
	val results: StateFlow<Map<String, Double>> = _results.asStateFlow()
	
	/**
	 * Detailed calculation description (premium feature).
	 * Contains step-by-step explanation of calculations with formulas.
	 * Always calculated and stored, but only displayed when premium is enabled (currently always true for testing).
	 * 
	 * TODO: Enable after Premium feature launch
	 */
	private val _calculationDetails = MutableStateFlow<String?>(null)
	val calculationDetails: StateFlow<String?> = _calculationDetails.asStateFlow()
	
	/**
	 * Error message if calculation fails or validation errors occur.
	 * Null when no errors.
	 */
	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error.asStateFlow()
	
	/**
	 * Initializes ViewModel by loading calculator definition.
	 * Called automatically when ViewModel is created.
	 */
	init {
		loadCalculator(calculatorId)
	}
	
	/**
	 * Loads calculator definition from repository and initializes input values.
	 * 
	 * Process:
	 * 1. Fetch calculator definition by ID from CalculatorRepository
	 * 2. Store definition in _calculator StateFlow
	 * 3. Initialize input values with default values from field definitions
	 * 
	 * @param calculatorId ID of the calculator to load
	 */
	private fun loadCalculator(calculatorId: String) {
		viewModelScope.launch {
			// Load calculator definition from repository
			val calculator = CalculatorRepository.getCalculatorById(calculatorId)
			_calculator.value = calculator
			
			// Initialize input values with defaults or empty strings
			// This ensures all fields have initial values (either default or empty)
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
	 * 
	 * Called when user types in a text field or selects a dropdown option.
	 * Automatically clears any previous error messages.
	 * 
	 * @param fieldId ID of the field to update
	 * @param value New value as string (will be converted to Double during calculation)
	 */
	fun updateInput(fieldId: String, value: String) {
		val currentValues = _inputValues.value.toMutableMap()
		currentValues[fieldId] = value
		_inputValues.value = currentValues
		_error.value = null // Clear error when user types
	}
	
	/**
	 * Validates all input fields and performs calculation.
	 * 
	 * Process:
	 * 1. Convert string inputs to numeric values
	 * 2. Validate all required fields are filled
	 * 3. Validate min/max constraints
	 * 4. Perform calculation via CalculatorEngine
	 * 5. Get detailed calculation description (if available)
	 * 6. Store results and clear errors on success
	 * 7. Store error message on failure
	 * 
	 * Validation:
	 * - All fields must be filled (unless optional)
	 * - Values must be valid numbers
	 * - Values must be within min/max constraints
	 * 
	 * Calculation:
	 * - Uses CalculatorEngine.calculate() for numeric results
	 * - Uses CalculatorEngine.getCalculationDetails() for detailed descriptions
	 * - Both are always calculated, but details only shown when premium enabled
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
				// Get calculation details if available
				// TODO: Enable after Premium feature launch
				// Note: Details are always calculated and stored, but only displayed when premium is enabled (currently always true for testing)
				// This ensures data is ready when premium feature is enabled
				_calculationDetails.value = CalculatorEngine.getCalculationDetails(calculator.id, numericInputs)
				_error.value = null
			} catch (e: Exception) {
				_error.value = "Ошибка при расчёте: ${e.message}"
				_calculationDetails.value = null
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
		_calculationDetails.value = null
		_error.value = null
	}
}


