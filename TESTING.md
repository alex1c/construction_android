# Testing Guide for Construction Calculators

## Overview

This document describes the testing strategy for the construction calculators Android app. The focus is on validating calculation logic, input validation, and ensuring consistency across related calculators.

## Test Strategy

The testing approach covers:

1. **Unit Tests**: Individual calculator logic validation
2. **Boundary Tests**: Edge cases and limits
3. **Invalid Input Tests**: Error handling and validation
4. **Consistency Tests**: Cross-calculator compatibility
5. **Smoke Tests**: Regression prevention for all calculators

## Test Structure

Tests are located in `app/src/test/java/com/construction/domain/engine/`:

### Calculator-Specific Tests

- `WallpaperCalculatorTest.kt` - Tests for wallpaper calculator
- `ConcreteCalculatorTest.kt` - Tests for concrete mix calculator
- `FoundationCalculatorTest.kt` - Tests for foundation calculator (includes consistency with concrete)

### Cross-Calculator Consistency Tests

- `ElectricalConsistencyTest.kt` - Ensures `cable_section` and `electrical` calculators produce compatible results

### Smoke Tests

- `CalculatorEngineSmokeTest.kt` - Tests all 21 calculators with reasonable inputs to prevent regressions

## Running Tests

### Using Gradle

Run all tests:
```bash
./gradlew test
```

Run specific test class:
```bash
./gradlew test --tests "com.construction.domain.engine.WallpaperCalculatorTest"
```

Run tests with coverage:
```bash
./gradlew test jacocoTestReport
```

### Using Android Studio

1. Right-click on `app/src/test` folder
2. Select "Run 'Tests in 'test''"
3. Or right-click on a specific test class/method and select "Run"

## Test Coverage

### Current Coverage

The following calculators have dedicated test classes:

1. ✅ **wallpaper** - `WallpaperCalculatorTest`
2. ✅ **concrete** - `ConcreteCalculatorTest`
3. ✅ **foundation** - `FoundationCalculatorTest`
4. ✅ **cable_section** - `ElectricalConsistencyTest`
5. ✅ **electrical** - `ElectricalConsistencyTest`

### All Calculators Covered by Smoke Test

All 21 calculators are tested via `CalculatorEngineSmokeTest`:

1. wallpaper
2. paint
3. tile_adhesive
4. putty
5. primer
6. plaster
7. wall_area
8. tile
9. laminate
10. foundation
11. concrete
12. roof
13. brick_blocks
14. stairs
15. gravel
16. ventilation
17. heated_floor
18. water_pipes
19. rebar
20. cable_section
21. electrical

## Test Categories

### 1. Normal Cases

Tests with realistic, typical input values:

```kotlin
@Test
fun testNormalCase() {
    val inputs = mapOf(
        "room_length" to 4.0,
        "room_width" to 3.0,
        "room_height" to 2.7,
        // ...
    )
    val result = CalculatorEngine.calculateWithValidation("wallpaper", inputs)
    assertTrue(result.isSuccess)
    // Verify results are reasonable
}
```

### 2. Boundary Cases

Tests with minimal and maximum valid values:

- Very small rooms (0.1m dimensions)
- Very large rooms (approaching max limits)
- Zero values where allowed (e.g., openings_area)
- Maximum percentages (100%)

### 3. Invalid Input Tests

Tests that validation correctly rejects:

- Negative values
- Zero where not allowed
- Extremely large values (overflow protection)
- Missing required fields
- Non-numeric values (handled by ViewModel)

### 4. Rounding Behavior

Tests that material quantities are rounded UP:

```kotlin
@Test
fun testRoundingUp() {
    // Input that needs 2.1 rolls should round to 3
    // Verify rolls_count >= 2.1 and is an integer >= 3
}
```

### 5. Proportional Scaling

Tests that relationships scale correctly:

```kotlin
@Test
fun testProportionalScaling() {
    // Double all dimensions -> area should quadruple (2²)
    // Volume should scale by 2³
}
```

### 6. Cross-Calculator Consistency

Tests that related calculators produce compatible results:

- **Wall Area Consistency**: `wall_area` calculator matches internal calculations in `wallpaper`, `paint`, `plaster`, `putty`
- **Concrete Consistency**: `foundation.concrete_volume` can be used directly in `concrete` calculator
- **Electrical Consistency**: `cable_section` and `electrical` produce compatible current and cable section values

## Validation Rules

### Common Validation

All calculators enforce:

- **Length/Area/Volume**: Must be > 0
  - Min: 0.01 m (length), 0.01 m² (area), 0.001 m³ (volume)
  - Max: 100 m (length), 10000 m² (area), 10000 m³ (volume)
  
- **Percentages**: 0% to 100%

- **Integers/Counts**: Must be > 0, max 1,000,000

- **Material consumption**: Must be > 0

### Calculator-Specific Rules

See `CalculatorDocumentation.kt` for detailed validation rules per calculator.

## Error Handling

All calculations use `CalculationResult<Map<String, Double>>`:

```kotlin
sealed class CalculationResult<out T> {
    data class Success<T>(val data: T) : CalculationResult<T>()
    data class Error(
        val message: String,
        val fieldId: String? = null,
        val errorType: ErrorType = ErrorType.VALIDATION
    ) : CalculationResult<Nothing>()
}
```

Error types:
- `VALIDATION` - Input validation error
- `INTERNAL` - Calculation error (division by zero, etc.)
- `MISSING_INPUT` - Required field not provided

## Adding Tests for New Calculators

### Step 1: Create Test Class

Create `{CalculatorName}CalculatorTest.kt` in `app/src/test/java/com/construction/domain/engine/`:

```kotlin
package com.construction.domain.engine

import org.junit.Assert.*
import org.junit.Test

class PaintCalculatorTest {
    companion object {
        private const val DELTA = 1e-6
    }
    
    @Test
    fun testNormalCase() {
        // Test with realistic values
    }
    
    @Test
    fun testBoundaryCases() {
        // Test min/max values
    }
    
    @Test
    fun testInvalidInputs() {
        // Test negative, zero, missing fields
    }
    
    @Test
    fun testRounding() {
        // Test rounding behavior if applicable
    }
}
```

### Step 2: Add to Smoke Test

The smoke test automatically includes all calculators from `CalculatorRepository`, but you may need to add default values in `generateReasonableInputs()` if your calculator has unique fields.

### Step 3: Add Consistency Tests (if applicable)

If your calculator relates to others (e.g., uses wall area, concrete volume), add consistency tests:

```kotlin
@Test
fun testConsistencyWithRelatedCalculator() {
    // Test that results are compatible
}
```

## Test Best Practices

1. **Use Descriptive Names**: Test names should clearly describe what they test
2. **Test One Thing**: Each test should verify one specific behavior
3. **Use Constants**: Define `DELTA` for floating-point comparisons
4. **Test Both Success and Error Cases**: Don't just test happy path
5. **Verify All Result Fields**: Ensure all expected outputs are present
6. **Check for NaN/Infinite**: Verify results are valid numbers
7. **Test Edge Cases**: Boundary values, zero, negative, very large
8. **Document Formulas**: If testing specific formulas, include comments with expected calculations

## Continuous Integration

Tests should run automatically on:
- Pre-commit hooks (if configured)
- Pull request validation
- Nightly builds

## Troubleshooting

### Tests Fail with "Calculator not found"

- Ensure calculator ID matches exactly (case-sensitive)
- Check that calculator is registered in `CalculatorRepository`

### Floating-Point Precision Issues

- Use appropriate `DELTA` values (e.g., `1e-6` for precise, `1e-2` for material quantities)
- Consider rounding in calculations vs. in tests

### Validation Errors in Smoke Test

- Some calculators may fail validation with default values
- Adjust `generateReasonableInputs()` to provide valid defaults
- Or mark test as expected to fail for specific calculators

## Future Improvements

- [ ] Add parameterized tests for similar input-output pairs
- [ ] Add performance tests for large calculations
- [ ] Add property-based tests (using KotlinTest or similar)
- [ ] Increase coverage to 90%+ for all calculators
- [ ] Add integration tests with ViewModel
- [ ] Add UI tests for calculator screens

## References

- `CalculatorDocumentation.kt` - Complete calculator specifications
- `CalculationResult.kt` - Error handling types
- `InputValidator.kt` - Validation logic
- `CalculatorEngine.kt` - Calculation implementations

