package com.construction.domain.model

/**
 * Represents a category that groups related calculators.
 *
 * @param id Unique identifier for the category
 * @param name Display name of the category
 * @param description Detailed description of the category
 * @param accessLevel Access level required to use this category (FREE or PREMIUM)
 */
data class CalculatorCategory(
	val id: String,
	val name: String,
	val description: String,
	val accessLevel: AccessLevel = AccessLevel.PREMIUM
)




