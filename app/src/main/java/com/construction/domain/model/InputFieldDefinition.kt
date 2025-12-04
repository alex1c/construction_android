package com.construction.domain.model

/**
 * Definition of an input field for a calculator.
 * Contains all metadata needed to render and validate user input.
 *
 * @param id Unique identifier for the input field
 * @param label Display label for the field
 * @param unit Optional unit of measurement (e.g., "м", "м²", "кг")
 * @param type Type of input field (determines validation and formatting)
 * @param hint Optional hint text to guide user input
 * @param defaultValue Optional default value for the field
 * @param minValue Optional minimum allowed value
 * @param maxValue Optional maximum allowed value
 * @param step Optional step value for increment/decrement controls
 * @param options Optional list of options for dropdown (value to display text mapping)
 */
data class InputFieldDefinition(
	val id: String,
	val label: String,
	val unit: String? = null,
	val type: InputFieldType,
	val hint: String? = null,
	val defaultValue: Double? = null,
	val minValue: Double? = null,
	val maxValue: Double? = null,
	val step: Double? = null,
	val options: List<Pair<Double, String>>? = null
)


