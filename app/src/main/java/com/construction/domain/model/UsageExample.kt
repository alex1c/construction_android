package com.construction.domain.model

/**
 * Usage example for a calculator.
 * Provides a concrete example showing how to use the calculator with typical values.
 *
 * @param id Unique identifier for the example (within a calculator)
 * @param title Short title describing the example scenario
 * @param description Free-form description of the use case in Russian
 * @param inputSummary Compact human-readable description of input values
 * @param resultSummary Compact human-readable description of expected results
 */
data class UsageExample(
	val id: String,
	val title: String,
	val description: String,
	val inputSummary: String,
	val resultSummary: String
)



