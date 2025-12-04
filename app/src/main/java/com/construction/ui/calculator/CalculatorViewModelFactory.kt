package com.construction.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating CalculatorViewModel instances.
 * Allows passing calculatorId to the ViewModel constructor.
 */
class CalculatorViewModelFactory(
	private val calculatorId: String
) : ViewModelProvider.Factory {
	
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
			return CalculatorViewModel(calculatorId) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}


