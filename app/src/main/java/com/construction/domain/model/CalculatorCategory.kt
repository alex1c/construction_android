package com.construction.domain.model

/**
 * Represents a category that groups related calculators.
 *
 * @param id Unique identifier for the category
 * @param name Display name of the category
 * @param description Detailed description of the category
 */
data class CalculatorCategory(
	val id: String,
	val name: String,
	val description: String
)


