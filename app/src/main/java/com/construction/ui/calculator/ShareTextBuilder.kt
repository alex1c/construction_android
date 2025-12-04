package com.construction.ui.calculator

import android.content.Context
import com.construction.R
import com.construction.domain.model.CalculatorDefinition

/**
 * Helper function to build share text for calculation results.
 * Creates a human-readable text containing calculator name, inputs, results, and footer.
 */
fun buildShareText(
	calculator: CalculatorDefinition,
	inputValues: Map<String, String>,
	results: Map<String, Double>,
	context: Context
): String {
	val builder = StringBuilder()
	
	// Calculator name
	builder.append(calculator.name)
	builder.append("\n\n")
	
	// Input values summary
	builder.append("Параметры:\n")
	calculator.inputFields.forEach { field ->
		val value = inputValues[field.id]?.takeIf { it.isNotBlank() }
		if (value != null) {
			builder.append("• ${field.label}: $value")
			if (field.unit != null) {
				builder.append(" ${field.unit}")
			}
			builder.append("\n")
		}
	}
	
	builder.append("\n")
	
	// Results
	builder.append("Результаты:\n")
	calculator.resultFields.forEach { field ->
		val resultValue = results[field.id]
		if (resultValue != null) {
			builder.append("• ${field.label}: ${String.format("%.2f", resultValue)}")
			if (field.unit != null) {
				builder.append(" ${field.unit}")
			}
			builder.append("\n")
		}
	}
	
	builder.append("\n")
	
	// Footer
	builder.append(context.getString(R.string.share_calculation_footer))
	
	return builder.toString()
}

