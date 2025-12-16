package ru.calc1.construction.domain.model

/**
 * Complete definition of a construction calculator.
 * Contains all information needed to render the calculator UI and perform calculations.
 *
 * @param id Unique identifier for the calculator
 * @param categoryId ID of the category this calculator belongs to
 * @param name Display name of the calculator
 * @param shortDescription Brief description of what the calculator does
 * @param inputFields List of input fields required for the calculation
 * @param resultFields List of result fields that will be displayed after calculation
 * @param helpText Optional detailed help text explaining how to use the calculator
 * @param usageExamples List of usage examples showing typical use cases
 */
data class CalculatorDefinition(
	val id: String,
	val categoryId: String,
	val name: String,
	val shortDescription: String,
	val inputFields: List<InputFieldDefinition>,
	val resultFields: List<ResultFieldDefinition>,
	val helpText: String? = null,
	val usageExamples: List<UsageExample> = emptyList()
)


