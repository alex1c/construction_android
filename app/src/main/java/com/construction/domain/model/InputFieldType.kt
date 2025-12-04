package com.construction.domain.model

/**
 * Enum representing different types of input fields for calculators.
 * Used to validate and format user input appropriately.
 */
enum class InputFieldType {
	/** Generic numeric input */
	NUMBER,
	
	/** Integer-only numeric input */
	INTEGER,
	
	/** Length measurement (meters, centimeters, etc.) */
	LENGTH,
	
	/** Area measurement (square meters, etc.) */
	AREA,
	
	/** Volume measurement (cubic meters, liters, etc.) */
	VOLUME,
	
	/** Mass measurement (kilograms, tons, etc.) */
	MASS,
	
	/** Power measurement (watts, kilowatts, etc.) */
	POWER,
	
	/** Flow rate measurement (cubic meters per hour, etc.) */
	FLOW,
	
	/** Percentage value (0-100) */
	PERCENT,
	
	/** Dropdown selection field */
	DROPDOWN
}


