package com.construction.domain.model

/**
 * Definition of a result field for a calculator.
 * Contains metadata needed to display calculation results.
 *
 * @param id Unique identifier for the result field
 * @param label Display label for the result
 * @param unit Optional unit of measurement (e.g., "шт", "кг", "м²")
 */
data class ResultFieldDefinition(
	val id: String,
	val label: String,
	val unit: String? = null
)


