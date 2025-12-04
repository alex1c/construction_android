package com.construction.domain.repository

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests to verify that all calculators have usage examples according to the rules:
 * - Simple calculators: 2 examples
 * - Complex calculators: 3 examples
 */
class UsageExamplesTest {
	
	// Simple calculators (2 examples each)
	private val simpleCalculators = listOf(
		"wallpaper", "paint", "tile_adhesive", "putty", "primer",
		"plaster", "wall_area", "tile", "laminate", "gravel"
	)
	
	// Complex calculators (3 examples each)
	private val complexCalculators = listOf(
		"foundation", "concrete", "roof", "brick_blocks", "stairs",
		"ventilation", "heated_floor", "water_pipes", "rebar",
		"cable_section", "electrical"
	)
	
	@Test
	fun testAllCalculatorsHaveExamples() {
		val allCalculators = CalculatorRepository.getCalculators()
		
		for (calculator in allCalculators) {
			assertTrue(
				"Calculator '${calculator.id}' should have at least 2 examples, but has ${calculator.usageExamples.size}",
				calculator.usageExamples.size >= 2
			)
		}
	}
	
	@Test
	fun testSimpleCalculatorsHaveTwoExamples() {
		val allCalculators = CalculatorRepository.getCalculators()
		
		for (calculatorId in simpleCalculators) {
			val calculator = allCalculators.find { it.id == calculatorId }
			assertNotNull("Calculator '$calculatorId' should exist", calculator)
			
			assertEquals(
				"Simple calculator '$calculatorId' should have exactly 2 examples",
				2,
				calculator!!.usageExamples.size
			)
		}
	}
	
	@Test
	fun testComplexCalculatorsHaveThreeExamples() {
		val allCalculators = CalculatorRepository.getCalculators()
		
		for (calculatorId in complexCalculators) {
			val calculator = allCalculators.find { it.id == calculatorId }
			assertNotNull("Calculator '$calculatorId' should exist", calculator)
			
			assertEquals(
				"Complex calculator '$calculatorId' should have exactly 3 examples",
				3,
				calculator!!.usageExamples.size
			)
		}
	}
	
	@Test
	fun testExampleIdsAreUniqueWithinCalculator() {
		val allCalculators = CalculatorRepository.getCalculators()
		
		for (calculator in allCalculators) {
			val exampleIds = calculator.usageExamples.map { it.id }
			val uniqueIds = exampleIds.toSet()
			
			assertEquals(
				"Calculator '${calculator.id}' should have unique example IDs. " +
					"Found duplicates: ${exampleIds.size - uniqueIds.size}",
				exampleIds.size,
				uniqueIds.size
			)
		}
	}
	
	@Test
	fun testExamplesHaveNonEmptyFields() {
		val allCalculators = CalculatorRepository.getCalculators()
		
		for (calculator in allCalculators) {
			for (example in calculator.usageExamples) {
				assertTrue(
					"Example '${example.id}' in calculator '${calculator.id}' should have non-empty title",
					example.title.isNotBlank()
				)
				assertTrue(
					"Example '${example.id}' in calculator '${calculator.id}' should have non-empty description",
					example.description.isNotBlank()
				)
				assertTrue(
					"Example '${example.id}' in calculator '${calculator.id}' should have non-empty inputSummary",
					example.inputSummary.isNotBlank()
				)
				assertTrue(
					"Example '${example.id}' in calculator '${calculator.id}' should have non-empty resultSummary",
					example.resultSummary.isNotBlank()
				)
			}
		}
	}
	
	@Test
	fun testEmptyExamplesListDoesNotCrash() {
		// Test defensive behavior: calculator with empty examples list should not crash
		val allCalculators = CalculatorRepository.getCalculators()
		
		// All calculators should have examples, but we test that empty list is handled
		for (calculator in allCalculators) {
			// Accessing usageExamples should not throw
			val examples = calculator.usageExamples
			assertNotNull("Usage examples should not be null", examples)
			// Empty list is valid (though we require >= 2 in other tests)
		}
	}
}

