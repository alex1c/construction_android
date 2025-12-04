package com.construction.ui.calculator

import android.content.Context
import com.construction.R
import com.construction.domain.model.CalculatorDefinition

/**
 * Helper function to build share text for calculation results.
 * 
 * Creates a human-readable, formatted text string containing:
 * - Calculator name
 * - Input parameters with labels and units
 * - Calculation results with labels and units
 * - Footer with app attribution
 * 
 * The generated text is suitable for sharing via:
 * - Messengers (Telegram, WhatsApp, etc.)
 * - Email
 * - Social networks
 * - Clipboard
 * - Any app that supports text sharing
 * 
 * Format Example:
 * ```
 * Калькулятор обоев
 * 
 * Параметры:
 * • Длина комнаты: 4.0 м
 * • Ширина комнаты: 3.0 м
 * • Высота стен: 2.7 м
 * ...
 * 
 * Результаты:
 * • Количество рулонов: 8.00 шт
 * 
 * Расчёт выполнен в приложении "Строительные калькуляторы Calc1".
 * ```
 * 
 * @param calculator Calculator definition (for field labels and structure)
 * @param inputValues Map of input field IDs to their string values
 * @param results Map of result field IDs to their calculated numeric values
 * @param context Android context for accessing string resources
 * @return Formatted text string ready for sharing
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

