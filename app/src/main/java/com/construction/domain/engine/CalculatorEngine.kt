package com.construction.domain.engine

import com.construction.domain.model.InputFieldType
import com.construction.domain.repository.CalculatorRepository
import kotlin.math.*

/**
 * Calculation engine for construction calculators.
 * Contains placeholder formulas that can be refined later with real calculations.
 *
 * @param calculatorId ID of the calculator to use
 * @param inputs Map of input field IDs to their numeric values
 * @return Map of result field IDs to calculated values
 */
object CalculatorEngine {
	
	/**
	 * Calculates with validation and returns a Result type.
	 * This is the recommended method for new code.
	 */
	fun calculateWithValidation(
		calculatorId: String,
		inputs: Map<String, Double>
	): CalculationResult<Map<String, Double>> {
		val calculator = CalculatorRepository.getCalculatorById(calculatorId)
			?: return CalculationResult.Error(
				message = "Калькулятор с ID '$calculatorId' не найден",
				errorType = CalculationResult.ErrorType.INTERNAL
			)
		
		// Build validation maps
		val requiredFields = calculator.inputFields.map { it.id }
		val fieldTypes = calculator.inputFields.associate { it.id to it.type }
		val allowZero = calculator.inputFields.associate { 
			it.id to (it.defaultValue == 0.0 || it.id.contains("openings") || it.id.contains("waste"))
		}
		
		// Validate inputs
		val validationError = InputValidator.validateInputs(inputs, requiredFields, fieldTypes, allowZero)
		if (validationError != null) {
			return validationError
		}
		
		// Perform calculation with error handling
		return try {
			val results = calculate(calculatorId, inputs)
			if (results.isEmpty()) {
				CalculationResult.Error(
					message = ErrorMessages.INTERNAL_ERROR,
					errorType = CalculationResult.ErrorType.INTERNAL
				)
			} else {
				CalculationResult.Success(results)
			}
		} catch (e: ArithmeticException) {
			CalculationResult.Error(
				message = ErrorMessages.DIVISION_BY_ZERO,
				errorType = CalculationResult.ErrorType.INTERNAL
			)
		} catch (e: Exception) {
			CalculationResult.Error(
				message = "${ErrorMessages.INTERNAL_ERROR}: ${e.message}",
				errorType = CalculationResult.ErrorType.INTERNAL
			)
		}
	}
	
	/**
	 * Gets detailed calculation description for a calculator.
	 * Returns null if no detailed description is available.
	 */
	fun getCalculationDetails(calculatorId: String, inputs: Map<String, Double>): String? {
		return when (calculatorId) {
			"foundation" -> getFoundationCalculationDetails(inputs)
			"brick_blocks" -> getBrickBlocksCalculationDetails(inputs)
			"stairs" -> getStairsCalculationDetails(inputs)
			"gravel" -> getGravelCalculationDetails(inputs)
			"ventilation" -> getVentilationCalculationDetails(inputs)
			"heated_floor" -> getHeatedFloorCalculationDetails(inputs)
			"water_pipes" -> getWaterPipesCalculationDetails(inputs)
			"rebar" -> getRebarCalculationDetails(inputs)
			"cable_section" -> getCableSectionCalculationDetails(inputs)
			"electrical" -> getElectricalCalculationDetails(inputs)
			else -> null
		}
	}
	
	/**
	 * Legacy calculation method without validation.
	 * @deprecated Use calculateWithValidation instead for proper error handling
	 */
	@Deprecated("Use calculateWithValidation for proper validation and error handling")
	fun calculate(calculatorId: String, inputs: Map<String, Double>): Map<String, Double> {
		return when (calculatorId) {
			// Finishing & Interior Calculators
			"wallpaper" -> calculateWallpaper(inputs)
			"paint" -> calculatePaint(inputs)
			"tile_adhesive" -> calculateTileAdhesive(inputs)
			"putty" -> calculatePutty(inputs)
			"primer" -> calculatePrimer(inputs)
			"plaster" -> calculatePlaster(inputs)
			"wall_area" -> calculateWallArea(inputs)
			"tile" -> calculateTile(inputs)
			"laminate" -> calculateLaminate(inputs)
			
			// Structures & Concrete Calculators
			"foundation" -> calculateFoundation(inputs)
			"concrete" -> calculateConcrete(inputs)
			"roof" -> calculateRoof(inputs)
			"brick_blocks" -> calculateBrickBlocks(inputs)
			"stairs" -> calculateStairs(inputs)
			"gravel" -> calculateGravel(inputs)
			
			// Engineering Systems Calculators
			"ventilation" -> calculateVentilation(inputs)
			"heated_floor" -> calculateHeatedFloor(inputs)
			"water_pipes" -> calculateWaterPipes(inputs)
			
			// Metal & Electricity Calculators
			"rebar" -> calculateRebar(inputs)
			"cable_section" -> calculateCableSection(inputs)
			"electrical" -> calculateElectrical(inputs)
			
			else -> emptyMap()
		}
	}
	
	// Finishing & Interior Calculations
	
	/**
	 * Calculates number of wallpaper rolls needed.
	 * Formula: (Wall area - Openings area) × (1 + Waste / 100) / Roll area
	 * Based on calc1.ru calculation method
	 */
	private fun calculateWallpaper(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 0.0
		val openingsArea = inputs["openings_area"] ?: 0.0
		val rollWidth = inputs["roll_width"] ?: 0.53
		val rollLength = inputs["roll_length"] ?: 10.05
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate total wall area: perimeter × height
		val wallArea = 2 * (roomLength + roomWidth) * roomHeight
		
		// Subtract openings (windows and doors)
		val usefulArea = wallArea - openingsArea
		
		// Calculate roll area
		val rollArea = rollWidth * rollLength
		if (rollArea <= 0) throw ArithmeticException("Roll area must be positive")
		
		// Calculate rolls needed with waste: (useful area × (1 + waste%)) / roll area, rounded UP
		val areaWithWaste = usefulArea * (1 + wastePercent / 100.0)
		val rollsNeeded = kotlin.math.ceil(areaWithWaste / rollArea)
		
		return mapOf("rolls_count" to rollsNeeded)
	}
	
	/**
	 * Calculates paint quantity needed.
	 * Formula: (Wall area - Openings area) × Consumption (l/m²) × Coats × (1 + Waste / 100)
	 * Based on calc1.ru calculation method
	 */
	private fun calculatePaint(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 0.0
		val openingsArea = inputs["openings_area"] ?: 0.0
		val coatsCount = inputs["coats_count"] ?: 2.0
		val paintConsumption = inputs["paint_consumption"] ?: 0.12
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate total wall area: perimeter × height
		val wallArea = 2 * (roomLength + roomWidth) * roomHeight
		
		// Subtract openings (windows and doors)
		val usefulArea = wallArea - openingsArea
		
		// Calculate paint volume: useful area × consumption × coats × (1 + waste%)
		val paintVolume = usefulArea * paintConsumption * coatsCount * (1 + wastePercent / 100.0)
		
		return mapOf("paint_volume" to paintVolume)
	}
	
	/**
	 * Calculates tile adhesive quantity needed.
	 * Formula: (Floor area - Openings area) × Consumption (kg/m²) × (1 + Waste / 100)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateTileAdhesive(inputs: Map<String, Double>): Map<String, Double> {
		val floorArea = inputs["floor_area"] ?: 0.0
		val openingsArea = inputs["openings_area"] ?: 0.0
		val adhesiveConsumption = inputs["adhesive_consumption"] ?: 4.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Subtract openings from floor area
		val usefulArea = floorArea - openingsArea
		
		// Calculate adhesive mass: useful area × consumption × (1 + waste%)
		val adhesiveMass = usefulArea * adhesiveConsumption * (1 + wastePercent / 100.0)
		
		return mapOf("adhesive_mass" to adhesiveMass)
	}
	
	/**
	 * Calculates putty quantity needed.
	 * Formula: Wall area × Consumption (kg/m²) × (Layer thickness / Base thickness) × (1 + Waste / 100)
	 * Based on calc1.ru calculation method
	 */
	private fun calculatePutty(inputs: Map<String, Double>): Map<String, Double> {
		val wallArea = inputs["wall_area"] ?: 0.0
		val puttyConsumption = inputs["putty_consumption"] ?: 1.2
		val layerThickness = inputs["layer_thickness"] ?: 2.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Adjust consumption based on thickness (2mm = base)
		val adjustedConsumption = puttyConsumption * (layerThickness / 2.0)
		val puttyMass = wallArea * adjustedConsumption * (1 + wastePercent / 100.0)
		
		return mapOf("putty_mass" to puttyMass)
	}
	
	/**
	 * Calculates primer quantity needed.
	 * Formula: Surface area × Consumption (l/m²) × Coats × (1 + Waste / 100)
	 * Based on calc1.ru calculation method
	 */
	private fun calculatePrimer(inputs: Map<String, Double>): Map<String, Double> {
		val surfaceArea = inputs["surface_area"] ?: 0.0
		val primerConsumption = inputs["primer_consumption"] ?: 0.2
		val coatsCount = inputs["coats_count"] ?: 1.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		val primerVolume = surfaceArea * primerConsumption * coatsCount * (1 + wastePercent / 100.0)
		
		return mapOf("primer_volume" to primerVolume)
	}
	
	/**
	 * Calculates plaster quantity needed.
	 * Formula: Wall area × Consumption (kg/m²) × (Layer thickness / Base thickness) × (1 + Waste / 100)
	 * Based on calc1.ru calculation method
	 */
	private fun calculatePlaster(inputs: Map<String, Double>): Map<String, Double> {
		val wallArea = inputs["wall_area"] ?: 0.0
		val plasterConsumption = inputs["plaster_consumption"] ?: 8.5
		val layerThickness = inputs["layer_thickness"] ?: 10.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Adjust consumption based on thickness (10mm = base)
		val adjustedConsumption = plasterConsumption * (layerThickness / 10.0)
		val plasterMass = wallArea * adjustedConsumption * (1 + wastePercent / 100.0)
		
		return mapOf("plaster_mass" to plasterMass)
	}
	
	/**
	 * Calculates wall area with openings and waste.
	 * Placeholder: perimeter * height - openings, then add waste
	 */
	private fun calculateWallArea(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 0.0
		val openingsArea = inputs["openings_area"] ?: 0.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		val perimeter = 2 * (roomLength + roomWidth)
		val totalArea = perimeter * roomHeight - openingsArea
		val areaWithWaste = totalArea * (1 + wastePercent / 100.0)
		
		return mapOf(
			"total_area" to totalArea,
			"area_with_waste" to areaWithWaste
		)
	}
	
	/**
	 * Calculates number of tiles needed.
	 * Formula: (Floor area - Openings area) × (1 + Waste / 100) / Tile area
	 * Based on calc1.ru calculation method
	 */
	private fun calculateTile(inputs: Map<String, Double>): Map<String, Double> {
		val floorArea = inputs["floor_area"] ?: 0.0
		val openingsArea = inputs["openings_area"] ?: 0.0
		val tileLength = inputs["tile_length"] ?: 0.0
		val tileWidth = inputs["tile_width"] ?: 0.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Subtract openings from floor area
		val usefulArea = floorArea - openingsArea
		
		// Calculate tile area (convert cm to m)
		val tileArea = (tileLength / 100.0) * (tileWidth / 100.0)
		if (tileArea <= 0) throw ArithmeticException("Tile area must be positive")
		
		// Calculate tiles needed with waste, rounded UP
		val areaWithWaste = usefulArea * (1 + wastePercent / 100.0)
		val tilesNeeded = kotlin.math.ceil(areaWithWaste / tileArea)
		
		return mapOf("tile_count" to tilesNeeded)
	}
	
	/**
	 * Calculates laminate quantity needed.
	 * Placeholder: room_area / board_area with waste, then packs
	 */
	private fun calculateLaminate(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val laminateLength = inputs["laminate_length"] ?: 1.3
		val laminateWidth = inputs["laminate_width"] ?: 0.2
		val packCount = inputs["pack_count"] ?: 8.0
		val wastePercent = inputs["waste_percent"] ?: 5.0
		
		val roomArea = roomLength * roomWidth
		val boardArea = laminateLength * laminateWidth
		if (boardArea <= 0) throw ArithmeticException("Board area must be positive")
		if (packCount <= 0) throw ArithmeticException("Pack count must be positive")
		val boardsNeeded = (roomArea / boardArea) * (1 + wastePercent / 100.0)
		val packsNeeded = kotlin.math.ceil(boardsNeeded / packCount).coerceAtLeast(1.0) // Rounded UP
		
		return mapOf(
			"pack_count" to packsNeeded,
			"total_area" to roomArea
		)
	}
	
	// Structures & Concrete Calculations
	
	/**
	 * Calculates foundation materials: concrete, rebar, formwork.
	 * Supports strip, slab, and column foundation types.
	 * Based on calc1.ru calculation method
	 */
	private fun calculateFoundation(inputs: Map<String, Double>): Map<String, Double> {
		val foundationType = inputs["foundation_type"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val height = inputs["height"] ?: 0.0
		val wallThickness = inputs["wall_thickness"] ?: 0.3
		val pillarsCount = inputs["pillars_count"] ?: 9.0
		val concreteGrade = inputs["concrete_grade"] ?: 200.0
		val rebarDiameter = inputs["rebar_diameter"] ?: 12.0
		val meshStep = inputs["mesh_step"] ?: 20.0 // in cm
		val layersCount = inputs["layers_count"] ?: 2.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate concrete volume based on foundation type
		val concreteVolume = when (foundationType.toInt()) {
			1 -> {
				// Strip foundation: V = (L×W×H) - ((L-2t)×(W-2t)×H)
				val outerVolume = length * width * height
				val innerLength = length - 2 * wallThickness
				val innerWidth = width - 2 * wallThickness
				val innerVolume = innerLength * innerWidth * height
				outerVolume - innerVolume
			}
			2 -> {
				// Slab foundation: V = L×W×H (where H is thickness)
				length * width * height
			}
			3 -> {
				// Column foundation: V = N×L×W×H
				pillarsCount * length * width * height
			}
			else -> {
				// Default: strip foundation
				val outerVolume = length * width * height
				val innerLength = length - 2 * wallThickness
				val innerWidth = width - 2 * wallThickness
				val innerVolume = innerLength * innerWidth * height
				outerVolume - innerVolume
			}
		}
		
		if (concreteVolume <= 0) throw ArithmeticException("Concrete volume must be positive")
		
		// Apply waste percentage
		val concreteVolumeWithWaste = concreteVolume * (1 + wastePercent / 100.0)
		
		// Calculate cement, sand, and gravel based on concrete grade
		val (cementPerM3, sandPerM3, gravelPerM3) = when (concreteGrade.toInt()) {
			200 -> Triple(280.0, 730.0, 1250.0)
			250 -> Triple(330.0, 720.0, 1250.0)
			300 -> Triple(380.0, 700.0, 1250.0)
			else -> Triple(280.0, 730.0, 1250.0) // Default to M200
		}
		
		val cementMass = concreteVolumeWithWaste * cementPerM3
		val cementBags = kotlin.math.ceil(cementMass / 50.0) // 50 kg per bag
		val sandMass = concreteVolumeWithWaste * sandPerM3
		val gravelMass = concreteVolumeWithWaste * gravelPerM3
		
		// Calculate rebar mass based on foundation type, dimensions, mesh step, diameter, and layers
		// Formula similar to rebar calculator: calculate bars count and length
		val meshStepMeters = meshStep / 100.0
		if (meshStepMeters <= 0) throw ArithmeticException("Mesh step must be positive")
		
		// Get mass per meter according to GOST
		val massPerMeter = getRebarMassPerMeter(rebarDiameter)
		
		// Calculate rebar length based on foundation type
		val totalRebarLength = when (foundationType.toInt()) {
			1 -> {
				// Strip foundation: calculate bars along length and width
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				longLength + transLength
			}
			2 -> {
				// Slab foundation: calculate bars along length and width
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				longLength + transLength
			}
			3 -> {
				// Column foundation: 4-6 bars per column (standard)
				val barsPerColumn = 4.0 * layersCount
				val barLength = height
				pillarsCount * barsPerColumn * barLength
			}
			else -> {
				// Default: strip foundation
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				longLength + transLength
			}
		}
		
		val rebarMass = totalRebarLength * massPerMeter
		
		// Calculate formwork area
		val formworkArea = when (foundationType.toInt()) {
			1 -> {
				// Strip foundation: perimeter × height × 2 (inner and outer)
				val perimeter = 2.0 * (length + width)
				perimeter * height * 2.0
			}
			2 -> {
				// Slab foundation: perimeter × thickness
				val perimeter = 2.0 * (length + width)
				perimeter * height
			}
			3 -> {
				// Column foundation: perimeter of one column × height × count
				val columnPerimeter = 2.0 * (length + width)
				columnPerimeter * height * pillarsCount
			}
			else -> {
				// Default: strip foundation
				val perimeter = 2.0 * (length + width)
				perimeter * height * 2.0
			}
		}
		
		return mapOf(
			"concrete_volume" to concreteVolumeWithWaste,
			"cement_mass" to cementMass,
			"cement_bags" to cementBags,
			"sand_mass" to sandMass,
			"gravel_mass" to gravelMass,
			"rebar_mass" to rebarMass,
			"formwork_area" to formworkArea
		)
	}
	
	/**
	 * Gets detailed calculation description for foundation calculator.
	 */
	private fun getFoundationCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val foundationType = inputs["foundation_type"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val height = inputs["height"] ?: 0.0
		val wallThickness = inputs["wall_thickness"] ?: 0.3
		val pillarsCount = inputs["pillars_count"] ?: 9.0
		val concreteGrade = inputs["concrete_grade"] ?: 200.0
		val rebarDiameter = inputs["rebar_diameter"] ?: 12.0
		val meshStep = inputs["mesh_step"] ?: 20.0
		val layersCount = inputs["layers_count"] ?: 2.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate concrete volume
		val concreteVolume = when (foundationType.toInt()) {
			1 -> {
				val outerVolume = length * width * height
				val innerLength = length - 2 * wallThickness
				val innerWidth = width - 2 * wallThickness
				val innerVolume = innerLength * innerWidth * height
				outerVolume - innerVolume
			}
			2 -> length * width * height
			3 -> pillarsCount * length * width * height
			else -> {
				val outerVolume = length * width * height
				val innerLength = length - 2 * wallThickness
				val innerWidth = width - 2 * wallThickness
				val innerVolume = innerLength * innerWidth * height
				outerVolume - innerVolume
			}
		}
		val concreteVolumeWithWaste = concreteVolume * (1 + wastePercent / 100.0)
		
		// Get component ratios
		val (cementPerM3, sandPerM3, gravelPerM3) = when (concreteGrade.toInt()) {
			200 -> Triple(280.0, 730.0, 1250.0)
			250 -> Triple(330.0, 720.0, 1250.0)
			300 -> Triple(380.0, 700.0, 1250.0)
			else -> Triple(280.0, 730.0, 1250.0)
		}
		
		val cementMass = concreteVolumeWithWaste * cementPerM3
		val cementBags = kotlin.math.ceil(cementMass / 50.0)
		val sandMass = concreteVolumeWithWaste * sandPerM3
		val gravelMass = concreteVolumeWithWaste * gravelPerM3
		
		// Calculate rebar
		val meshStepMeters = meshStep / 100.0
		val massPerMeter = getRebarMassPerMeter(rebarDiameter)
		
		val totalRebarLength = when (foundationType.toInt()) {
			1, 2 -> {
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				longBars * width + transBars * length
			}
			3 -> {
				val barsPerColumn = 4.0 * layersCount
				pillarsCount * barsPerColumn * height
			}
			else -> {
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				longBars * width + transBars * length
			}
		}
		val rebarMass = totalRebarLength * massPerMeter
		
		// Calculate formwork
		val formworkArea = when (foundationType.toInt()) {
			1 -> {
				val perimeter = 2.0 * (length + width)
				perimeter * height * 2.0
			}
			2 -> {
				val perimeter = 2.0 * (length + width)
				perimeter * height
			}
			3 -> {
				val columnPerimeter = 2.0 * (length + width)
				columnPerimeter * height * pillarsCount
			}
			else -> {
				val perimeter = 2.0 * (length + width)
				perimeter * height * 2.0
			}
		}
		
		// Build description
		val foundationTypeName = when (foundationType.toInt()) {
			1 -> "Ленточный"
			2 -> "Плитный"
			3 -> "Столбчатый"
			else -> "Ленточный"
		}
		val concreteGradeName = "М${concreteGrade.toInt()}"
		
		val longBars: Double
		val transBars: Double
		val longLength: Double
		val transLength: Double
		
		when (foundationType.toInt()) {
			1, 2 -> {
				longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				longLength = longBars * width
				transLength = transBars * length
			}
			3 -> {
				val barsPerColumn = 4.0 * layersCount
				longBars = pillarsCount * barsPerColumn
				transBars = 0.0
				longLength = totalRebarLength
				transLength = 0.0
			}
			else -> {
				longBars = 0.0
				transBars = 0.0
				longLength = 0.0
				transLength = 0.0
			}
		}
		
		return buildString {
			appendLine("Тип фундамента: $foundationTypeName")
			appendLine()
			appendLine("1. РАСЧЁТ ОБЪЁМА БЕТОНА:")
			when (foundationType.toInt()) {
				1 -> {
					val outerVolume = length * width * height
					val innerLength = length - 2 * wallThickness
					val innerWidth = width - 2 * wallThickness
					val innerVolume = innerLength * innerWidth * height
					appendLine("   Внешний объём: ${String.format("%.2f", length)} × ${String.format("%.2f", width)} × ${String.format("%.2f", height)} = ${String.format("%.2f", outerVolume)} м³")
					appendLine("   Внутренний объём: ${String.format("%.2f", innerLength)} × ${String.format("%.2f", innerWidth)} × ${String.format("%.2f", height)} = ${String.format("%.2f", innerVolume)} м³")
					appendLine("   Объём бетона: ${String.format("%.2f", outerVolume)} - ${String.format("%.2f", innerVolume)} = ${String.format("%.2f", concreteVolume)} м³")
				}
				2 -> {
					appendLine("   Объём бетона: ${String.format("%.2f", length)} × ${String.format("%.2f", width)} × ${String.format("%.2f", height)} = ${String.format("%.2f", concreteVolume)} м³")
				}
				3 -> {
					val singleColumnVolume = length * width * height
					appendLine("   Объём одного столба: ${String.format("%.2f", length)} × ${String.format("%.2f", width)} × ${String.format("%.2f", height)} = ${String.format("%.2f", singleColumnVolume)} м³")
					appendLine("   Объём бетона: ${String.format("%.2f", singleColumnVolume)} × ${pillarsCount.toInt()} = ${String.format("%.2f", concreteVolume)} м³")
				}
			}
			appendLine("   С учётом запаса ${String.format("%.0f", wastePercent)}%: ${String.format("%.2f", concreteVolume)} × 1.${String.format("%.0f", wastePercent)} = ${String.format("%.2f", concreteVolumeWithWaste)} м³")
			appendLine()
			appendLine("2. РАСЧЁТ КОМПОНЕНТОВ БЕТОНА (марка $concreteGradeName):")
			appendLine("   Цемент: ${String.format("%.2f", concreteVolumeWithWaste)} × ${String.format("%.0f", cementPerM3)} = ${String.format("%.2f", cementMass)} кг")
			appendLine("   Мешков цемента (50 кг): ${String.format("%.2f", cementMass)} / 50 = ${cementBags.toInt()} мешков")
			appendLine("   Песок: ${String.format("%.2f", concreteVolumeWithWaste)} × ${String.format("%.0f", sandPerM3)} = ${String.format("%.2f", sandMass)} кг")
			appendLine("   Щебень: ${String.format("%.2f", concreteVolumeWithWaste)} × ${String.format("%.0f", gravelPerM3)} = ${String.format("%.2f", gravelMass)} кг")
			appendLine()
			appendLine("3. РАСЧЁТ АРМАТУРЫ (диаметр ${rebarDiameter.toInt()} мм, шаг ${meshStep.toInt()} см, ${layersCount.toInt()} слоя):")
			when (foundationType.toInt()) {
				1, 2 -> {
					appendLine("   Продольных стержней: ⌈${String.format("%.2f", length)} / ${String.format("%.2f", meshStepMeters)} + 1⌉ × ${layersCount.toInt()} = ${longBars.toInt()} шт")
					appendLine("   Длина продольных: ${longBars.toInt()} × ${String.format("%.2f", width)} = ${String.format("%.2f", longLength)} м")
					appendLine("   Поперечных стержней: ⌈${String.format("%.2f", width)} / ${String.format("%.2f", meshStepMeters)} + 1⌉ × ${layersCount.toInt()} = ${transBars.toInt()} шт")
					appendLine("   Длина поперечных: ${transBars.toInt()} × ${String.format("%.2f", length)} = ${String.format("%.2f", transLength)} м")
					appendLine("   Общая длина: ${String.format("%.2f", longLength)} + ${String.format("%.2f", transLength)} = ${String.format("%.2f", totalRebarLength)} м")
				}
				3 -> {
					appendLine("   Стержней на столб: 4 × ${layersCount.toInt()} = ${longBars.toInt()} шт")
					appendLine("   Длина стержня: ${String.format("%.2f", height)} м")
					appendLine("   Общая длина: ${longBars.toInt()} × ${String.format("%.2f", height)} × ${pillarsCount.toInt()} = ${String.format("%.2f", totalRebarLength)} м")
				}
			}
			appendLine("   Масса на метр (ГОСТ): ${String.format("%.3f", massPerMeter)} кг/м")
			appendLine("   Общая масса: ${String.format("%.2f", totalRebarLength)} × ${String.format("%.3f", massPerMeter)} = ${String.format("%.2f", rebarMass)} кг")
			appendLine()
			appendLine("4. РАСЧЁТ ПЛОЩАДИ ОПАЛУБКИ:")
			when (foundationType.toInt()) {
				1 -> {
					val perimeter = 2.0 * (length + width)
					appendLine("   Периметр: 2 × (${String.format("%.2f", length)} + ${String.format("%.2f", width)}) = ${String.format("%.2f", perimeter)} м")
					appendLine("   Площадь: ${String.format("%.2f", perimeter)} × ${String.format("%.2f", height)} × 2 = ${String.format("%.2f", formworkArea)} м²")
				}
				2 -> {
					val perimeter = 2.0 * (length + width)
					appendLine("   Периметр: 2 × (${String.format("%.2f", length)} + ${String.format("%.2f", width)}) = ${String.format("%.2f", perimeter)} м")
					appendLine("   Площадь: ${String.format("%.2f", perimeter)} × ${String.format("%.2f", height)} = ${String.format("%.2f", formworkArea)} м²")
				}
				3 -> {
					val columnPerimeter = 2.0 * (length + width)
					appendLine("   Периметр столба: 2 × (${String.format("%.2f", length)} + ${String.format("%.2f", width)}) = ${String.format("%.2f", columnPerimeter)} м")
					appendLine("   Площадь: ${String.format("%.2f", columnPerimeter)} × ${String.format("%.2f", height)} × ${pillarsCount.toInt()} = ${String.format("%.2f", formworkArea)} м²")
				}
			}
		}
	}
	
	/**
	 * Calculates concrete mix components.
	 * Formula: Component = Volume × Density × Ratio / Sum of ratios
	 * Water = Cement × Water-cement ratio (in liters, 1 kg water = 1 liter)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateConcrete(inputs: Map<String, Double>): Map<String, Double> {
		val concreteVolume = inputs["concrete_volume"] ?: 0.0
		val cementRatio = inputs["cement_ratio"] ?: 1.0
		val sandRatio = inputs["sand_ratio"] ?: 2.5
		val gravelRatio = inputs["gravel_ratio"] ?: 4.5
		val waterCementRatio = inputs["water_cement_ratio"] ?: 0.5
		
		if (concreteVolume <= 0) throw ArithmeticException("Concrete volume must be positive")
		
		// Densities (kg/m³) according to calc1.ru
		val cementDensity = 1400.0
		val sandDensity = 1600.0
		val gravelDensity = 1400.0
		
		// Calculate sum of ratios
		val totalRatio = cementRatio + sandRatio + gravelRatio
		if (totalRatio <= 0) throw ArithmeticException("Sum of ratios must be positive")
		
		// Calculate component masses using volume and density
		// Formula: Component = Volume × Density × Ratio / Sum of ratios
		val cementMass = concreteVolume * cementDensity * cementRatio / totalRatio
		val sandMass = concreteVolume * sandDensity * sandRatio / totalRatio
		val gravelMass = concreteVolume * gravelDensity * gravelRatio / totalRatio
		
		// Calculate water volume: Water = Cement × Water-cement ratio
		// Water-cement ratio is mass ratio, so water mass = cement mass × W/C ratio
		// 1 kg water = 1 liter, so water volume in liters = water mass in kg
		val waterVolume = cementMass * waterCementRatio
		
		// Calculate cement bags (50 kg per bag)
		val cementBags = kotlin.math.ceil(cementMass / 50.0)
		
		return mapOf(
			"cement_mass" to cementMass,
			"cement_bags" to cementBags,
			"sand_mass" to sandMass,
			"gravel_mass" to gravelMass,
			"water_volume" to waterVolume
		)
	}
	
	/**
	 * Calculates roof area, material area, and sheets count.
	 * Formula: Projection area × Slope coefficient × Complexity coefficient + Overhang area
	 * Based on calc1.ru calculation method
	 */
	private fun calculateRoof(inputs: Map<String, Double>): Map<String, Double> {
		val houseLength = inputs["house_length"] ?: 0.0
		val houseWidth = inputs["house_width"] ?: 0.0
		val roofType = inputs["roof_type"] ?: 2.0
		val roofAngle = inputs["roof_angle"] ?: 30.0
		val overhang = inputs["overhang"] ?: 0.5
		val sheetLength = inputs["sheet_length"] ?: 1.18
		val sheetWidth = inputs["sheet_width"] ?: 0.35
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		if (houseLength <= 0 || houseWidth <= 0) throw ArithmeticException("House dimensions must be positive")
		if (roofAngle < 0 || roofAngle >= 90) throw ArithmeticException("Roof angle must be between 0 and 90 degrees")
		if (sheetLength <= 0 || sheetWidth <= 0) throw ArithmeticException("Sheet dimensions must be positive")
		
		// Calculate projection area
		val projectionArea = houseLength * houseWidth
		
		// Calculate slope coefficient: 1 / cos(angle)
		val angleRad = roofAngle * PI / 180.0
		val slopeCoefficient = 1.0 / cos(angleRad)
		
		// Calculate complexity coefficient based on roof type
		val complexityCoefficient = when (roofType.toInt()) {
			1 -> {
				// Single-pitch (Односкатная): simple form
				1.0
			}
			2 -> {
				// Gable (Двускатная): two slopes, standard form
				1.0 // Will multiply by 2 for two slopes
			}
			3 -> {
				// Hip (Вальмовая): four slopes, complex form
				1.4
			}
			4 -> {
				// Mansard (Мансардная): broken slopes, very complex form
				1.5
			}
			else -> 1.0 // Default to gable
		}
		
		// Calculate slopes area
		val slopesArea = when (roofType.toInt()) {
			1 -> {
				// Single-pitch: one slope
				projectionArea * slopeCoefficient * complexityCoefficient
			}
			2 -> {
				// Gable: two slopes
				projectionArea * slopeCoefficient * complexityCoefficient * 2.0
			}
			3 -> {
				// Hip: four slopes with complexity coefficient
				projectionArea * slopeCoefficient * complexityCoefficient
			}
			4 -> {
				// Mansard: broken slopes with complexity coefficient
				projectionArea * slopeCoefficient * complexityCoefficient
			}
			else -> {
				// Default to gable
				projectionArea * slopeCoefficient * complexityCoefficient * 2.0
			}
		}
		
		// Calculate overhang area
		// For single-pitch and gable: 2 sides (length + width)
		// For hip: 4 sides (perimeter)
		// For mansard: 2 sides (length + width)
		val overhangArea = when (roofType.toInt()) {
			1, 2, 4 -> {
				// Single-pitch, gable, mansard: 2 × (length + width) × overhang
				2.0 * (houseLength + houseWidth) * overhang
			}
			3 -> {
				// Hip: 4 sides, but simplified as perimeter × overhang
				2.0 * (houseLength + houseWidth) * overhang
			}
			else -> {
				2.0 * (houseLength + houseWidth) * overhang
			}
		}
		
		// Calculate total roof area
		val totalRoofArea = slopesArea + overhangArea
		
		// Apply waste percentage
		val materialArea = totalRoofArea * (1 + wastePercent / 100.0)
		
		// Calculate sheet area
		val sheetArea = sheetLength * sheetWidth
		if (sheetArea <= 0) throw ArithmeticException("Sheet area must be positive")
		
		// Calculate number of sheets (rounded up)
		val sheetsCount = kotlin.math.ceil(materialArea / sheetArea)
		
		return mapOf(
			"roof_area" to totalRoofArea,
			"material_area" to materialArea,
			"sheets_count" to sheetsCount
		)
	}
	
	/**
	 * Calculates number of bricks/blocks needed for wall construction.
	 * Formula: Count = (Wall area / Material area) × Thickness in material units
	 * Based on calc1.ru calculation method
	 */
	private fun calculateBrickBlocks(inputs: Map<String, Double>): Map<String, Double> {
		val wallLength = inputs["wall_length"] ?: 0.0
		val wallHeight = inputs["wall_height"] ?: 0.0
		val wallThicknessBricks = inputs["wall_thickness_bricks"] ?: 1.0
		val materialType = inputs["material_type"] ?: 1.0
		val materialLength = inputs["material_length"] ?: 250.0 // in mm
		val materialWidth = inputs["material_width"] ?: 120.0 // in mm
		val materialHeight = inputs["material_height"] ?: 65.0 // in mm
		val jointThickness = inputs["joint_thickness"] ?: 10.0 // in mm
		val wastePercent = inputs["waste_percent"] ?: 5.0
		
		if (wallLength <= 0 || wallHeight <= 0) throw ArithmeticException("Wall dimensions must be positive")
		if (materialLength <= 0 || materialWidth <= 0 || materialHeight <= 0) throw ArithmeticException("Material dimensions must be positive")
		
		// Calculate wall area
		val wallArea = wallLength * wallHeight
		
		// Convert material dimensions from mm to m
		val materialLengthM = materialLength / 1000.0
		val materialWidthM = materialWidth / 1000.0
		val materialHeightM = materialHeight / 1000.0
		val jointThicknessM = jointThickness / 1000.0
		
		// Calculate effective material dimensions (with joint)
		// Effective length = material length + joint thickness
		// Effective height = material height + joint thickness
		val effectiveLength = materialLengthM + jointThicknessM
		val effectiveHeight = materialHeightM + jointThicknessM
		
		// Calculate number of materials in one row (length direction)
		// Formula: materialsInRow = wallLength / (materialLength + joint)
		val materialsInRow = kotlin.math.ceil(wallLength / effectiveLength)
		
		// Calculate number of rows (height direction)
		// Formula: rowsCount = wallHeight / (materialHeight + joint)
		val rowsCount = kotlin.math.ceil(wallHeight / effectiveHeight)
		
		// Calculate total count without waste
		// Formula: baseCount = materialsInRow × rowsCount × thicknessInBricks
		// For both bricks and blocks, thickness is specified in units (bricks or blocks)
		val baseCount = materialsInRow * rowsCount * wallThicknessBricks
		
		// Apply waste percentage
		val materialCount = kotlin.math.ceil(baseCount * (1 + wastePercent / 100.0))
		
		// Calculate wall thickness in meters
		val actualWallThickness = if (materialType.toInt() >= 4) {
			// Blocks: thickness = block width × number of blocks
			// Standard block orientation: width determines thickness
			// For 600×300×200 mm block: width 300 mm = 0.3 m (if laid sideways) or 200 mm = 0.2 m (standard)
			// Use material width as base thickness
			materialWidthM * wallThicknessBricks
		} else {
			// Bricks: standard thicknesses based on brick width (120 mm)
			// 0.5 brick = 120 mm = 0.12 m
			// 1 brick = 250 mm = 0.25 m (120 + 10 joint + 120)
			// 1.5 brick = 380 mm = 0.38 m
			// 2 brick = 510 mm = 0.51 m
			when {
				wallThicknessBricks <= 0.75 -> 0.12 // 0.5 brick
				wallThicknessBricks <= 1.25 -> 0.25 // 1 brick
				wallThicknessBricks <= 1.75 -> 0.38 // 1.5 brick
				else -> 0.51 // 2 brick
			}
		}
		
		// Calculate wall volume
		val wallVolume = wallArea * actualWallThickness
		
		// Calculate mortar/glue volume
		// For blocks: Glue volume = Wall area × Joint thickness (thin joint 3-5 mm)
		// For bricks: Mortar volume = Wall area × Joint thickness × Coefficient (thicker joints)
		// Typical mortar consumption: 0.03-0.05 m³ per 1 m² of wall for bricks
		// Typical glue consumption: 0.003-0.005 m³ per 1 m² of wall for blocks
		val mortarVolume = if (materialType.toInt() >= 4) {
			// Blocks: thin joint glue
			wallArea * jointThicknessM
		} else {
			// Bricks: mortar with coefficient
			// Approximate: 0.03-0.05 m³ per m², or use joint thickness × coefficient
			// From examples: 30 m² wall, 1.14 m³ mortar = 0.038 m³ per m²
			// This is approximately: wall area × (joint thickness × 3.8) for 10 mm joint
			wallArea * jointThicknessM * 3.8
		}
		
		return mapOf(
			"material_count" to materialCount,
			"wall_volume" to wallVolume,
			"mortar_volume" to mortarVolume
		)
	}
	
	/**
	 * Gets detailed calculation description for brick/blocks calculator.
	 */
	private fun getBrickBlocksCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val wallLength = inputs["wall_length"] ?: 0.0
		val wallHeight = inputs["wall_height"] ?: 0.0
		val wallThicknessBricks = inputs["wall_thickness_bricks"] ?: 1.0
		val materialType = inputs["material_type"] ?: 1.0
		val materialLength = inputs["material_length"] ?: 250.0
		val materialWidth = inputs["material_width"] ?: 120.0
		val materialHeight = inputs["material_height"] ?: 65.0
		val jointThickness = inputs["joint_thickness"] ?: 10.0
		val wastePercent = inputs["waste_percent"] ?: 5.0
		
		// Calculate wall area
		val wallArea = wallLength * wallHeight
		
		// Convert material dimensions from mm to m
		val materialLengthM = materialLength / 1000.0
		val materialWidthM = materialWidth / 1000.0
		val materialHeightM = materialHeight / 1000.0
		val jointThicknessM = jointThickness / 1000.0
		
		// Calculate effective dimensions
		val effectiveLength = materialLengthM + jointThicknessM
		val effectiveHeight = materialHeightM + jointThicknessM
		
		// Calculate counts
		val materialsInRow = kotlin.math.ceil(wallLength / effectiveLength)
		val rowsCount = kotlin.math.ceil(wallHeight / effectiveHeight)
		val baseCount = materialsInRow * rowsCount * wallThicknessBricks
		val materialCount = kotlin.math.ceil(baseCount * (1 + wastePercent / 100.0))
		
		// Calculate wall thickness
		val actualWallThickness = if (materialType.toInt() >= 4) {
			materialWidthM * wallThicknessBricks
		} else {
			when {
				wallThicknessBricks <= 0.75 -> 0.12
				wallThicknessBricks <= 1.25 -> 0.25
				wallThicknessBricks <= 1.75 -> 0.38
				else -> 0.51
			}
		}
		
		// Calculate volumes
		val wallVolume = wallArea * actualWallThickness
		val mortarVolume = if (materialType.toInt() >= 4) {
			wallArea * jointThicknessM
		} else {
			wallArea * jointThicknessM * 3.8
		}
		
		// Get material type name
		val materialTypeName = when (materialType.toInt()) {
			1 -> "Кирпич одинарный"
			2 -> "Кирпич полуторный"
			3 -> "Кирпич двойной"
			4 -> "Газоблок"
			5 -> "Пеноблок"
			else -> "Кирпич одинарный"
		}
		
		// Get wall thickness description
		val wallThicknessDesc = when {
			wallThicknessBricks <= 0.75 -> "В полкирпича (120 мм)"
			wallThicknessBricks <= 1.25 -> "В кирпич (250 мм)"
			wallThicknessBricks <= 1.75 -> "В полтора кирпича (380 мм)"
			else -> "В два кирпича (510 мм)"
		}
		
		// Build description
		return buildString {
			appendLine("Тип материала: $materialTypeName")
			appendLine("Толщина стены: $wallThicknessDesc")
			appendLine()
			appendLine("1. РАСЧЁТ ПЛОЩАДИ СТЕНЫ:")
			appendLine("   Площадь стены: ${String.format("%.2f", wallLength)} × ${String.format("%.2f", wallHeight)} = ${String.format("%.2f", wallArea)} м²")
			appendLine()
			appendLine("2. РАСЧЁТ КОЛИЧЕСТВА МАТЕРИАЛА:")
			appendLine("   Размеры материала: ${materialLength.toInt()} × ${materialWidth.toInt()} × ${materialHeight.toInt()} мм")
			appendLine("   Размеры материала: ${String.format("%.3f", materialLengthM)} × ${String.format("%.3f", materialWidthM)} × ${String.format("%.3f", materialHeightM)} м")
			appendLine("   Толщина шва: ${jointThickness.toInt()} мм (${String.format("%.3f", jointThicknessM)} м)")
			appendLine()
			appendLine("   Эффективная длина (с учётом шва):")
			appendLine("   ${String.format("%.3f", materialLengthM)} + ${String.format("%.3f", jointThicknessM)} = ${String.format("%.3f", effectiveLength)} м")
			appendLine()
			appendLine("   Эффективная высота (с учётом шва):")
			appendLine("   ${String.format("%.3f", materialHeightM)} + ${String.format("%.3f", jointThicknessM)} = ${String.format("%.3f", effectiveHeight)} м")
			appendLine()
			appendLine("   Материалов в ряду (по длине):")
			appendLine("   ⌈${String.format("%.2f", wallLength)} / ${String.format("%.3f", effectiveLength)}⌉ = ${materialsInRow.toInt()} шт")
			appendLine()
			appendLine("   Количество рядов (по высоте):")
			appendLine("   ⌈${String.format("%.2f", wallHeight)} / ${String.format("%.3f", effectiveHeight)}⌉ = ${rowsCount.toInt()} шт")
			appendLine()
			appendLine("   Базовое количество (без запаса):")
			appendLine("   ${materialsInRow.toInt()} × ${rowsCount.toInt()} × ${String.format("%.1f", wallThicknessBricks)} = ${baseCount.toInt()} шт")
			appendLine()
			appendLine("   С учётом запаса ${String.format("%.0f", wastePercent)}%:")
			appendLine("   ${baseCount.toInt()} × 1.${String.format("%.0f", wastePercent)} = ${materialCount.toInt()} шт")
			appendLine()
			appendLine("3. РАСЧЁТ ОБЪЁМА СТЕНЫ:")
			if (materialType.toInt() >= 4) {
				appendLine("   Толщина стены (блоки): ${String.format("%.3f", materialWidthM)} × ${String.format("%.1f", wallThicknessBricks)} = ${String.format("%.3f", actualWallThickness)} м")
			} else {
				appendLine("   Толщина стены (кирпич): ${String.format("%.3f", actualWallThickness)} м")
			}
			appendLine("   Объём стены: ${String.format("%.2f", wallArea)} × ${String.format("%.3f", actualWallThickness)} = ${String.format("%.3f", wallVolume)} м³")
			appendLine()
			appendLine("4. РАСЧЁТ ОБЪЁМА РАСТВОРА/КЛЕЯ:")
			if (materialType.toInt() >= 4) {
				appendLine("   Для блоков используется тонкий шов (клей):")
				appendLine("   Объём клея = Площадь стены × Толщина шва")
				appendLine("   ${String.format("%.2f", wallArea)} × ${String.format("%.3f", jointThicknessM)} = ${String.format("%.3f", mortarVolume)} м³")
			} else {
				appendLine("   Для кирпича используется раствор с коэффициентом:")
				appendLine("   Объём раствора = Площадь стены × Толщина шва × 3.8")
				appendLine("   ${String.format("%.2f", wallArea)} × ${String.format("%.3f", jointThicknessM)} × 3.8 = ${String.format("%.3f", mortarVolume)} м³")
			}
		}
	}
	
	/**
	 * Calculates stairs parameters: steps count, flight length, angle, comfort formula.
	 * Formula: n = ⌈H / h⌉, L = n × b, α = arctan(h / b), 2h + b
	 * Based on calc1.ru calculation method
	 */
	private fun calculateStairs(inputs: Map<String, Double>): Map<String, Double> {
		val totalHeight = inputs["total_height"] ?: 0.0 // in mm
		val stepDepth = inputs["step_depth"] ?: 300.0 // in mm (проступь)
		val stepHeight = inputs["step_height"] ?: 180.0 // in mm (подступенок)
		val stairsType = inputs["stairs_type"] ?: 1.0
		
		if (totalHeight <= 0) throw ArithmeticException("Total height must be positive")
		if (stepHeight <= 0) throw ArithmeticException("Step height must be positive")
		if (stepDepth <= 0) throw ArithmeticException("Step depth must be positive")
		
		// Calculate number of steps: n = ⌈H / h⌉ (rounded up)
		val stepsCount = kotlin.math.ceil(totalHeight / stepHeight).coerceAtLeast(1.0)
		
		// Calculate flight length: L = n × b
		val flightLength = stepsCount * stepDepth
		
		// Calculate angle: α = arctan(h / b)
		val angle = atan(stepHeight / stepDepth) * 180.0 / PI
		
		// Calculate comfort formula: 2h + b (should be 600-640 mm for comfortable stairs)
		val comfortFormula = 2.0 * stepHeight + stepDepth
		
		return mapOf(
			"steps_count" to stepsCount,
			"flight_length" to flightLength,
			"angle" to angle,
			"comfort_formula" to comfortFormula
		)
	}
	
	/**
	 * Gets detailed calculation description for stairs calculator.
	 */
	private fun getStairsCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val totalHeight = inputs["total_height"] ?: 0.0
		val stepDepth = inputs["step_depth"] ?: 300.0
		val stepHeight = inputs["step_height"] ?: 180.0
		val stairsType = inputs["stairs_type"] ?: 1.0
		
		// Calculate number of steps
		val stepsCount = kotlin.math.ceil(totalHeight / stepHeight).coerceAtLeast(1.0)
		
		// Calculate flight length
		val flightLength = stepsCount * stepDepth
		
		// Calculate angle
		val angleRad = atan(stepHeight / stepDepth)
		val angle = angleRad * 180.0 / PI
		
		// Calculate comfort formula
		val comfortFormula = 2.0 * stepHeight + stepDepth
		
		// Get stairs type name
		val stairsTypeName = when (stairsType.toInt()) {
			1 -> "Прямая"
			2 -> "Поворотная 90°"
			3 -> "Поворотная 180°"
			else -> "Прямая"
		}
		
		// Calculate actual height covered by steps
		val actualHeight = stepsCount * stepHeight
		
		// Build description
		return buildString {
			appendLine("Тип лестницы: $stairsTypeName")
			appendLine()
			appendLine("1. РАСЧЁТ КОЛИЧЕСТВА СТУПЕНЕЙ:")
			appendLine("   Общая высота подъёма: ${String.format("%.0f", totalHeight)} мм")
			appendLine("   Высота подступенка: ${String.format("%.0f", stepHeight)} мм")
			appendLine()
			appendLine("   Формула: n = ⌈H / h⌉")
			appendLine("   Количество ступеней: ⌈${String.format("%.0f", totalHeight)} / ${String.format("%.0f", stepHeight)}⌉ = ${stepsCount.toInt()} шт")
			appendLine()
			appendLine("   Фактическая высота подъёма:")
			appendLine("   ${stepsCount.toInt()} × ${String.format("%.0f", stepHeight)} = ${String.format("%.0f", actualHeight)} мм")
			if (actualHeight != totalHeight) {
				appendLine("   (Отличие от заданной высоты: ${String.format("%.0f", totalHeight - actualHeight)} мм)")
			}
			appendLine()
			appendLine("2. РАСЧЁТ ДЛИНЫ ПРОЛЁТА:")
			appendLine("   Глубина проступи: ${String.format("%.0f", stepDepth)} мм")
			appendLine()
			appendLine("   Формула: L = n × b")
			appendLine("   Длина пролёта: ${stepsCount.toInt()} × ${String.format("%.0f", stepDepth)} = ${String.format("%.0f", flightLength)} мм")
			appendLine("   Длина пролёта: ${String.format("%.2f", flightLength / 1000.0)} м")
			appendLine()
			appendLine("3. РАСЧЁТ УГЛА НАКЛОНА:")
			appendLine("   Высота подступенка: ${String.format("%.0f", stepHeight)} мм")
			appendLine("   Глубина проступи: ${String.format("%.0f", stepDepth)} мм")
			appendLine()
			appendLine("   Формула: α = arctan(h / b) × 180° / π")
			appendLine("   Угол: arctan(${String.format("%.0f", stepHeight)} / ${String.format("%.0f", stepDepth)}) = arctan(${String.format("%.3f", stepHeight / stepDepth)})")
			appendLine("   Угол: ${String.format("%.3f", angleRad)} рад × 180° / π = ${String.format("%.2f", angle)}°")
			appendLine()
			when {
				angle < 20 -> appendLine("   Оценка: Очень пологий подъём (подходит для пожилых людей)")
				angle < 30 -> appendLine("   Оценка: Пологий подъём (комфортный для всех)")
				angle < 35 -> appendLine("   Оценка: Нормальный подъём (стандартный для жилых домов)")
				angle < 45 -> appendLine("   Оценка: Крутой подъём (экономит пространство)")
				else -> appendLine("   Оценка: Очень крутой подъём (только для подвалов и чердаков)")
			}
			appendLine()
			appendLine("4. ФОРМУЛА УДОБНОЙ ЛЕСТНИЦЫ:")
			appendLine("   Формула: 2h + b (должна быть 600-640 мм для комфортной лестницы)")
			appendLine("   Расчёт: 2 × ${String.format("%.0f", stepHeight)} + ${String.format("%.0f", stepDepth)} = ${String.format("%.0f", comfortFormula)} мм")
			appendLine()
			when {
				comfortFormula < 600 -> appendLine("   Оценка: Менее комфортно (формула < 600 мм)")
				comfortFormula <= 640 -> appendLine("   Оценка: Оптимально (формула 600-640 мм)")
				else -> appendLine("   Оценка: Более комфортно, но занимает больше места (формула > 640 мм)")
			}
			appendLine()
			appendLine("5. РЕКОМЕНДАЦИИ:")
			appendLine("   • Высота подступенка: оптимально 150-200 мм")
			appendLine("   • Глубина проступи: оптимально 280-320 мм")
			appendLine("   • Угол наклона: оптимально 30-35° для жилых помещений")
			appendLine("   • Формула удобства: 600-640 мм обеспечивает комфортный подъём")
		}
	}
	
	/**
	 * Calculates gravel volume and mass needed.
	 * Formula: Area = Length × Width (if input_method = 1)
	 *          Volume = Area × Thickness or Volume (if input_method = 3)
	 *          Mass = Volume × Density
	 *          Volume with waste = Volume × (1 + Waste / 100)
	 *          Mass with waste = Volume with waste × Density
	 * Based on calc1.ru calculation method
	 */
	private fun calculateGravel(inputs: Map<String, Double>): Map<String, Double> {
		val inputMethod = inputs["input_method"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val area = inputs["area"] ?: 0.0
		val volume = inputs["volume"] ?: 0.0
		val layerThickness = inputs["layer_thickness"] ?: 0.2
		val fractionType = inputs["fraction_type"] ?: 3.0
		val gravelDensity = inputs["gravel_density"] ?: 1400.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate volume based on input method (consolidated logic)
		val gravelVolume = when (inputMethod.toInt()) {
			1 -> { // Length × Width
				if (length <= 0 || width <= 0) throw ArithmeticException("Length and width must be positive")
				if (layerThickness <= 0) throw ArithmeticException("Layer thickness must be positive")
				length * width * layerThickness
			}
			2 -> { // Area
				if (area <= 0) throw ArithmeticException("Area must be positive")
				if (layerThickness <= 0) throw ArithmeticException("Layer thickness must be positive")
				area * layerThickness
			}
			3 -> { // Volume directly
				if (volume <= 0) throw ArithmeticException("Volume must be positive")
				volume
			}
			else -> throw ArithmeticException("Invalid input method. Must be 1 (Length × Width), 2 (Area), or 3 (Volume)")
		}
		
		// Calculate mass
		if (gravelDensity <= 0) throw ArithmeticException("Gravel density must be positive")
		val gravelMass = gravelVolume * gravelDensity
		
		// Calculate with waste
		val gravelVolumeWithWaste = gravelVolume * (1 + wastePercent / 100.0)
		val gravelMassWithWaste = gravelVolumeWithWaste * gravelDensity
		
		return mapOf(
			"gravel_volume" to gravelVolume,
			"gravel_mass" to gravelMass,
			"gravel_volume_with_waste" to gravelVolumeWithWaste,
			"gravel_mass_with_waste" to gravelMassWithWaste
		)
	}
	
	/**
	 * Gets detailed calculation description for gravel calculator.
	 */
	private fun getGravelCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val workType = inputs["work_type"] ?: 1.0
		val inputMethod = inputs["input_method"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val area = inputs["area"] ?: 0.0
		val volume = inputs["volume"] ?: 0.0
		val layerThickness = inputs["layer_thickness"] ?: 0.2
		val fractionType = inputs["fraction_type"] ?: 3.0
		val customGravelDensity = inputs["gravel_density"] ?: 1400.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Get effective density based on fraction type
		val effectiveGravelDensity = when (fractionType.toInt()) {
			1 -> 1500.0 // 5-10 мм
			2 -> 1450.0 // 10-20 мм
			3 -> 1400.0 // 20-40 мм
			4 -> 1350.0 // 40-70 мм
			5 -> customGravelDensity // Своя фракция
			else -> 1400.0
		}
		
		// Calculate volume based on input method
		val calculatedArea: Double
		val gravelVolume = when (inputMethod.toInt()) {
			1 -> { // Length × Width
				calculatedArea = length * width
				calculatedArea * layerThickness
			}
			2 -> { // Area
				calculatedArea = area
				area * layerThickness
			}
			3 -> { // Volume directly
				calculatedArea = volume / layerThickness
				volume
			}
			else -> throw ArithmeticException("Invalid input method")
		}
		
		// Calculate mass
		val gravelMass = gravelVolume * effectiveGravelDensity
		
		// Calculate with waste
		val gravelVolumeWithWaste = gravelVolume * (1 + wastePercent / 100.0)
		val gravelMassWithWaste = gravelVolumeWithWaste * effectiveGravelDensity
		
		// Get work type name
		val workTypeName = when (workType.toInt()) {
			1 -> "Фундамент"
			2 -> "Дорожка"
			3 -> "Отмостка"
			4 -> "Подсыпка"
			5 -> "Свой вариант"
			else -> "Фундамент"
		}
		
		// Get input method name
		val inputMethodName = when (inputMethod.toInt()) {
			1 -> "Длина × Ширина"
			2 -> "Площадь"
			3 -> "Объём"
			else -> "Длина × Ширина"
		}
		
		// Get fraction name
		val fractionName = when (fractionType.toInt()) {
			1 -> "5-10 мм"
			2 -> "10-20 мм"
			3 -> "20-40 мм"
			4 -> "40-70 мм"
			5 -> "Своя фракция"
			else -> "20-40 мм"
		}
		
		// Build description
		return buildString {
			appendLine("Тип работ: $workTypeName")
			appendLine("Способ ввода: $inputMethodName")
			appendLine("Фракция щебня: $fractionName")
			appendLine()
			appendLine("1. РАСЧЁТ ОБЪЁМА ЩЕБНЯ:")
			when (inputMethod.toInt()) {
				1 -> {
					appendLine("   Длина: ${String.format("%.2f", length)} м")
					appendLine("   Ширина: ${String.format("%.2f", width)} м")
					appendLine("   Площадь: ${String.format("%.2f", length)} × ${String.format("%.2f", width)} = ${String.format("%.2f", calculatedArea)} м²")
					appendLine("   Толщина слоя: ${String.format("%.2f", layerThickness)} м")
					appendLine("   Объём: ${String.format("%.2f", calculatedArea)} × ${String.format("%.2f", layerThickness)} = ${String.format("%.2f", gravelVolume)} м³")
				}
				2 -> {
					appendLine("   Площадь: ${String.format("%.2f", area)} м²")
					appendLine("   Толщина слоя: ${String.format("%.2f", layerThickness)} м")
					appendLine("   Объём: ${String.format("%.2f", area)} × ${String.format("%.2f", layerThickness)} = ${String.format("%.2f", gravelVolume)} м³")
				}
				3 -> {
					appendLine("   Объём: ${String.format("%.2f", volume)} м³")
				}
			}
			appendLine()
			appendLine("2. РАСЧЁТ ПЛОТНОСТИ ЩЕБНЯ:")
			when (fractionType.toInt()) {
				1 -> appendLine("   Фракция 5-10 мм: плотность 1500 кг/м³")
				2 -> appendLine("   Фракция 10-20 мм: плотность 1450 кг/м³")
				3 -> appendLine("   Фракция 20-40 мм: плотность 1400 кг/м³")
				4 -> appendLine("   Фракция 40-70 мм: плотность 1350 кг/м³")
				5 -> appendLine("   Своя фракция: плотность ${String.format("%.0f", customGravelDensity)} кг/м³")
			}
			appendLine("   Используемая плотность: ${String.format("%.0f", effectiveGravelDensity)} кг/м³")
			appendLine()
			appendLine("3. РАСЧЁТ ВЕСА ЩЕБНЯ:")
			appendLine("   Формула: Масса = Объём × Плотность")
			appendLine("   Вес: ${String.format("%.2f", gravelVolume)} × ${String.format("%.0f", effectiveGravelDensity)} = ${String.format("%.2f", gravelMass)} кг")
			appendLine("   Вес: ${String.format("%.2f", gravelMass / 1000.0)} т")
			appendLine()
			appendLine("4. РАСЧЁТ С УЧЁТОМ ЗАПАСА:")
			appendLine("   Запас материала: ${String.format("%.0f", wastePercent)}%")
			appendLine("   Объём с запасом: ${String.format("%.2f", gravelVolume)} × 1.${String.format("%.0f", wastePercent)} = ${String.format("%.2f", gravelVolumeWithWaste)} м³")
			appendLine("   Вес с запасом: ${String.format("%.2f", gravelVolumeWithWaste)} × ${String.format("%.0f", effectiveGravelDensity)} = ${String.format("%.2f", gravelMassWithWaste)} кг")
			appendLine("   Вес с запасом: ${String.format("%.2f", gravelMassWithWaste / 1000.0)} т")
			appendLine()
			appendLine("5. РЕКОМЕНДАЦИИ:")
			when (workType.toInt()) {
				1 -> appendLine("   Для фундамента рекомендуется фракция 20-40 мм, толщина слоя 20-30 см")
				2 -> appendLine("   Для дорожек рекомендуется фракция 5-20 мм, толщина слоя 10-15 см")
				3 -> appendLine("   Для отмостки рекомендуется фракция 5-20 мм, толщина слоя 10-15 см")
				4 -> appendLine("   Для подсыпки рекомендуется фракция 20-40 мм, толщина слоя 15-25 см")
				else -> appendLine("   Выберите фракцию и толщину слоя в зависимости от типа работ")
			}
			appendLine("   Запас 10-15% необходим для компенсации усадки при трамбовке")
		}
	}
	
	// Engineering Systems Calculations
	
	/**
	 * Calculates ventilation airflow rate.
	 * Formula: Volume = Length × Width × Height
	 *          Airflow by volume = Volume × Air exchange rate
	 *          Airflow by people = People count × Air norm per person
	 *          Required airflow = max(Airflow by volume, Airflow by people)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateVentilation(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 2.7
		val roomType = inputs["room_type"] ?: 1.0
		val peopleCount = inputs["people_count"] ?: 1.0
		val optionalAirExchangeRate = inputs["air_exchange_rate"] // Optional input
		
		if (roomLength <= 0 || roomWidth <= 0 || roomHeight <= 0) {
			throw ArithmeticException("Room dimensions must be positive")
		}
		if (peopleCount < 0) {
			throw ArithmeticException("People count must be non-negative")
		}
		
		// Get air exchange rate and air norm per person based on room type
		val (defaultAirExchangeRate, airNormPerPerson) = when (roomType.toInt()) {
			1 -> Pair(1.0, 30.0) // Жилая комната
			2 -> Pair(3.0, 60.0) // Кухня
			3 -> Pair(3.0, 25.0) // Ванная/туалет
			4 -> Pair(2.0, 40.0) // Офис
			5 -> Pair(5.0, 60.0) // Ресторан/кафе
			6 -> Pair(4.0, 80.0) // Спортзал
			7 -> Pair(2.0, 40.0) // Учебный класс
			8 -> Pair(1.0, 30.0) // Склад
			9 -> Pair(3.0, 40.0) // Производство
			else -> Pair(1.0, 30.0) // Default: жилая комната
		}
		
		// Use provided air exchange rate or default based on room type
		// Validate that if provided, it must be positive
		val effectiveAirExchangeRate = if (optionalAirExchangeRate != null) {
			if (optionalAirExchangeRate <= 0) {
				throw ArithmeticException("Air exchange rate must be positive if provided")
			}
			optionalAirExchangeRate
		} else {
			defaultAirExchangeRate
		}
		
		// Calculate room volume
		val roomVolume = roomLength * roomWidth * roomHeight
		
		// Calculate airflow by volume
		val airflowByVolume = roomVolume * effectiveAirExchangeRate
		
		// Calculate airflow by people
		val airflowByPeople = peopleCount * airNormPerPerson
		
		// Required airflow is maximum of both
		val requiredAirflow = maxOf(airflowByVolume, airflowByPeople)
		
		return mapOf(
			"room_volume" to roomVolume,
			"airflow_by_volume" to airflowByVolume,
			"airflow_by_people" to airflowByPeople,
			"required_airflow" to requiredAirflow
		)
	}
	
	/**
	 * Gets detailed calculation description for ventilation calculator.
	 */
	private fun getVentilationCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 2.7
		val roomType = inputs["room_type"] ?: 1.0
		val peopleCount = inputs["people_count"] ?: 1.0
		val optionalAirExchangeRate = inputs["air_exchange_rate"]
		
		// Get air exchange rate and air norm per person based on room type
		val (defaultAirExchangeRate, airNormPerPerson) = when (roomType.toInt()) {
			1 -> Pair(1.0, 30.0) // Жилая комната
			2 -> Pair(3.0, 60.0) // Кухня
			3 -> Pair(3.0, 25.0) // Ванная/туалет
			4 -> Pair(2.0, 40.0) // Офис
			5 -> Pair(5.0, 60.0) // Ресторан/кафе
			6 -> Pair(4.0, 80.0) // Спортзал
			7 -> Pair(2.0, 40.0) // Учебный класс
			8 -> Pair(1.0, 30.0) // Склад
			9 -> Pair(3.0, 40.0) // Производство
			else -> Pair(1.0, 30.0)
		}
		
		// Use provided air exchange rate or default based on room type
		val effectiveAirExchangeRate = if (optionalAirExchangeRate != null) {
			if (optionalAirExchangeRate <= 0) {
				throw ArithmeticException("Air exchange rate must be positive if provided")
			}
			optionalAirExchangeRate
		} else {
			defaultAirExchangeRate
		}
		
		// Calculate room volume
		val roomVolume = roomLength * roomWidth * roomHeight
		
		// Calculate airflow by volume
		val airflowByVolume = roomVolume * effectiveAirExchangeRate
		
		// Calculate airflow by people
		val airflowByPeople = peopleCount * airNormPerPerson
		
		// Required airflow is maximum of both
		val requiredAirflow = maxOf(airflowByVolume, airflowByPeople)
		
		// Get room type name
		val roomTypeName = when (roomType.toInt()) {
			1 -> "Жилая комната"
			2 -> "Кухня"
			3 -> "Ванная/туалет"
			4 -> "Офис"
			5 -> "Ресторан/кафе"
			6 -> "Спортзал"
			7 -> "Учебный класс"
			8 -> "Склад"
			9 -> "Производство"
			else -> "Жилая комната"
		}
		
		// Build description
		return buildString {
			appendLine("Тип помещения: $roomTypeName")
			appendLine()
			appendLine("1. РАСЧЁТ ОБЪЁМА ПОМЕЩЕНИЯ:")
			appendLine("   Длина: ${String.format("%.2f", roomLength)} м")
			appendLine("   Ширина: ${String.format("%.2f", roomWidth)} м")
			appendLine("   Высота: ${String.format("%.2f", roomHeight)} м")
			appendLine()
			appendLine("   Формула: Объём = Длина × Ширина × Высота")
			appendLine("   Объём: ${String.format("%.2f", roomLength)} × ${String.format("%.2f", roomWidth)} × ${String.format("%.2f", roomHeight)} = ${String.format("%.2f", roomVolume)} м³")
			appendLine()
			appendLine("2. ОПРЕДЕЛЕНИЕ ПАРАМЕТРОВ ВОЗДУХООБМЕНА:")
			appendLine("   Тип помещения: $roomTypeName")
			if (optionalAirExchangeRate != null) {
				appendLine("   Кратность воздухообмена (задана вручную): ${String.format("%.1f", optionalAirExchangeRate)} раз/ч")
			} else {
				appendLine("   Кратность воздухообмена (по типу помещения): ${String.format("%.1f", defaultAirExchangeRate)} раз/ч")
			}
			appendLine("   Используемая кратность: ${String.format("%.1f", effectiveAirExchangeRate)} раз/ч")
			appendLine()
			appendLine("   Норма воздуха на человека (по типу помещения): ${String.format("%.0f", airNormPerPerson)} м³/ч")
			appendLine("   Количество людей: ${peopleCount.toInt()} чел")
			appendLine()
			appendLine("3. РАСЧЁТ ПРОИЗВОДИТЕЛЬНОСТИ ПО ОБЪЁМУ:")
			appendLine("   Формула: Производительность = Объём × Кратность")
			appendLine("   Производительность: ${String.format("%.2f", roomVolume)} × ${String.format("%.1f", effectiveAirExchangeRate)} = ${String.format("%.2f", airflowByVolume)} м³/ч")
			appendLine()
			appendLine("4. РАСЧЁТ ПРОИЗВОДИТЕЛЬНОСТИ ПО ЛЮДЯМ:")
			appendLine("   Формула: Производительность = Количество людей × Норма на человека")
			appendLine("   Производительность: ${peopleCount.toInt()} × ${String.format("%.0f", airNormPerPerson)} = ${String.format("%.2f", airflowByPeople)} м³/ч")
			appendLine()
			appendLine("5. ОПРЕДЕЛЕНИЕ ТРЕБУЕМОЙ ПРОИЗВОДИТЕЛЬНОСТИ:")
			appendLine("   Формула: Требуемая = max(По объёму, По людям)")
			appendLine("   По объёму: ${String.format("%.2f", airflowByVolume)} м³/ч")
			appendLine("   По людям: ${String.format("%.2f", airflowByPeople)} м³/ч")
			appendLine("   Требуемая производительность: max(${String.format("%.2f", airflowByVolume)}, ${String.format("%.2f", airflowByPeople)}) = ${String.format("%.2f", requiredAirflow)} м³/ч")
			if (airflowByVolume > airflowByPeople) {
				appendLine("   (Определяющий фактор: объём помещения)")
			} else if (airflowByPeople > airflowByVolume) {
				appendLine("   (Определяющий фактор: количество людей)")
			} else {
				appendLine("   (Оба фактора дают одинаковый результат)")
			}
			appendLine()
			appendLine("6. РЕКОМЕНДАЦИИ:")
			appendLine("   • Выберите вентилятор с производительностью не менее ${String.format("%.0f", requiredAirflow)} м³/ч")
			appendLine("   • Для кухни и ванной рекомендуется вытяжная вентиляция")
			appendLine("   • Для жилых комнат достаточно приточной вентиляции")
			appendLine("   • Учитывайте потери в воздуховодах (добавьте 10-20% к расчётной производительности)")
		}
	}
	
	/**
	 * Calculates heated floor power and consumption.
	 * Formula: Base power depends on room type
	 *          Power = Base power × Insulation coefficient × Temperature coefficient
	 *          Consumption = (Power × Area × Hours) / 1000
	 *          Cost = Consumption × Electricity price
	 * Based on calc1.ru calculation method
	 */
	private fun calculateHeatedFloor(inputs: Map<String, Double>): Map<String, Double> {
		val floorArea = inputs["floor_area"] ?: 10.0
		val roomType = inputs["room_type"] ?: 1.0
		val insulationType = inputs["insulation_type"] ?: 2.0
		val desiredTemperature = inputs["desired_temperature"] ?: 25.0
		val usageHours = inputs["usage_hours"] ?: 8.0
		val electricityPrice = inputs["electricity_price"] ?: 5.5
		
		if (floorArea <= 0) throw ArithmeticException("Floor area must be positive")
		if (usageHours < 0) throw ArithmeticException("Usage hours must be non-negative")
		if (electricityPrice < 0) throw ArithmeticException("Electricity price must be non-negative")
		
		// Get base power based on room type (average of recommended range)
		val basePower = when (roomType.toInt()) {
			1 -> 165.0 // Ванная комната: 150-180 Вт/м²
			2 -> 135.0 // Кухня: 120-150 Вт/м²
			3 -> 115.0 // Гостиная: 100-130 Вт/м²
			4 -> 100.0 // Спальня: 80-100 Вт/м² (из таблицы)
			5 -> 180.0 // Балкон/лоджия: 160-200 Вт/м²
			else -> 150.0 // Default
		}
		
		// Get insulation coefficient
		val insulationCoefficient = when (insulationType.toInt()) {
			1 -> 0.85 // Хорошее утепление: 0.8-0.9
			2 -> 1.0  // Среднее утепление: 1.0
			3 -> 1.35 // Слабое утепление: 1.2-1.5
			else -> 1.0
		}
		
		// Temperature coefficient (slight adjustment based on desired temperature)
		// Normal range: 20-30°C, optimal: 25°C
		val temperatureCoefficient = when {
			desiredTemperature <= 20.0 -> 0.9
			desiredTemperature <= 25.0 -> 1.0
			desiredTemperature <= 30.0 -> 1.1
			else -> 1.15
		}
		
		// Calculate recommended power per m²
		val recommendedPower = basePower * insulationCoefficient * temperatureCoefficient
		
		// Calculate total power
		val totalPowerWatts = floorArea * recommendedPower
		val totalPowerKw = totalPowerWatts / 1000.0
		
		// Calculate consumption
		val dailyConsumption = (totalPowerWatts * usageHours) / 1000.0 // кВт⋅ч
		val monthlyConsumption = dailyConsumption * 30.0
		
		// Calculate costs
		val dailyCost = dailyConsumption * electricityPrice
		val monthlyCost = monthlyConsumption * electricityPrice
		
		return mapOf(
			"recommended_power" to recommendedPower,
			"total_power_watts" to totalPowerWatts,
			"total_power_kw" to totalPowerKw,
			"daily_consumption" to dailyConsumption,
			"monthly_consumption" to monthlyConsumption,
			"daily_cost" to dailyCost,
			"monthly_cost" to monthlyCost
		)
	}
	
	/**
	 * Gets detailed calculation description for heated floor calculator.
	 */
	private fun getHeatedFloorCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val floorArea = inputs["floor_area"] ?: 10.0
		val roomType = inputs["room_type"] ?: 1.0
		val insulationType = inputs["insulation_type"] ?: 2.0
		val desiredTemperature = inputs["desired_temperature"] ?: 25.0
		val usageHours = inputs["usage_hours"] ?: 8.0
		val electricityPrice = inputs["electricity_price"] ?: 5.5
		
		// Get base power based on room type
		val basePower = when (roomType.toInt()) {
			1 -> 165.0 // Ванная комната: 150-180 Вт/м²
			2 -> 135.0 // Кухня: 120-150 Вт/м²
			3 -> 115.0 // Гостиная: 100-130 Вт/м²
			4 -> 100.0 // Спальня: 80-100 Вт/м²
			5 -> 180.0 // Балкон/лоджия: 160-200 Вт/м²
			else -> 150.0
		}
		
		// Get insulation coefficient
		val insulationCoefficient = when (insulationType.toInt()) {
			1 -> 0.85 // Хорошее утепление
			2 -> 1.0  // Среднее утепление
			3 -> 1.35 // Слабое утепление
			else -> 1.0
		}
		
		// Temperature coefficient
		val temperatureCoefficient = when {
			desiredTemperature <= 20.0 -> 0.9
			desiredTemperature <= 25.0 -> 1.0
			desiredTemperature <= 30.0 -> 1.1
			else -> 1.15
		}
		
		// Calculate recommended power per m²
		val recommendedPower = basePower * insulationCoefficient * temperatureCoefficient
		
		// Calculate total power
		val totalPowerWatts = floorArea * recommendedPower
		val totalPowerKw = totalPowerWatts / 1000.0
		
		// Calculate consumption
		val dailyConsumption = (totalPowerWatts * usageHours) / 1000.0
		val monthlyConsumption = dailyConsumption * 30.0
		
		// Calculate costs
		val dailyCost = dailyConsumption * electricityPrice
		val monthlyCost = monthlyConsumption * electricityPrice
		
		// Get room type name
		val roomTypeName = when (roomType.toInt()) {
			1 -> "Ванная комната"
			2 -> "Кухня"
			3 -> "Гостиная"
			4 -> "Спальня"
			5 -> "Балкон/лоджия"
			else -> "Ванная комната"
		}
		
		// Get insulation type name
		val insulationTypeName = when (insulationType.toInt()) {
			1 -> "Хорошее утепление"
			2 -> "Среднее утепление"
			3 -> "Слабое утепление"
			else -> "Среднее утепление"
		}
		
		// Build description
		return buildString {
			appendLine("Тип помещения: $roomTypeName")
			appendLine("Тип утепления: $insulationTypeName")
			appendLine()
			appendLine("1. ОПРЕДЕЛЕНИЕ БАЗОВОЙ МОЩНОСТИ:")
			appendLine("   Тип помещения: $roomTypeName")
			when (roomType.toInt()) {
				1 -> appendLine("   Базовая мощность: 165 Вт/м² (диапазон 150-180 Вт/м² для ванной)")
				2 -> appendLine("   Базовая мощность: 135 Вт/м² (диапазон 120-150 Вт/м² для кухни)")
				3 -> appendLine("   Базовая мощность: 115 Вт/м² (диапазон 100-130 Вт/м² для гостиной)")
				4 -> appendLine("   Базовая мощность: 100 Вт/м² (диапазон 80-100 Вт/м² для спальни)")
				5 -> appendLine("   Базовая мощность: 180 Вт/м² (диапазон 160-200 Вт/м² для балкона)")
				else -> appendLine("   Базовая мощность: 150 Вт/м²")
			}
			appendLine()
			appendLine("2. ОПРЕДЕЛЕНИЕ КОЭФФИЦИЕНТА УТЕПЛЕНИЯ:")
			appendLine("   Тип утепления: $insulationTypeName")
			when (insulationType.toInt()) {
				1 -> appendLine("   Коэффициент утепления: 0.85 (хорошее утепление снижает мощность на 15%)")
				2 -> appendLine("   Коэффициент утепления: 1.0 (среднее утепление, без изменений)")
				3 -> appendLine("   Коэффициент утепления: 1.35 (слабое утепление увеличивает мощность на 35%)")
			}
			appendLine()
			appendLine("3. ОПРЕДЕЛЕНИЕ КОЭФФИЦИЕНТА ТЕМПЕРАТУРЫ:")
			appendLine("   Желаемая температура: ${String.format("%.0f", desiredTemperature)}°C")
			when {
				desiredTemperature <= 20.0 -> appendLine("   Коэффициент температуры: 0.9 (низкая температура, снижение мощности на 10%)")
				desiredTemperature <= 25.0 -> appendLine("   Коэффициент температуры: 1.0 (оптимальная температура, без изменений)")
				desiredTemperature <= 30.0 -> appendLine("   Коэффициент температуры: 1.1 (повышенная температура, увеличение мощности на 10%)")
				else -> appendLine("   Коэффициент температуры: 1.15 (высокая температура, увеличение мощности на 15%)")
			}
			appendLine()
			appendLine("4. РАСЧЁТ РЕКОМЕНДУЕМОЙ МОЩНОСТИ:")
			appendLine("   Формула: Рекомендуемая мощность = Базовая × Коэффициент утепления × Коэффициент температуры")
			appendLine("   Рекомендуемая мощность: ${String.format("%.0f", basePower)} × ${String.format("%.2f", insulationCoefficient)} × ${String.format("%.2f", temperatureCoefficient)} = ${String.format("%.2f", recommendedPower)} Вт/м²")
			appendLine()
			appendLine("5. РАСЧЁТ ОБЩЕЙ МОЩНОСТИ:")
			appendLine("   Площадь помещения: ${String.format("%.2f", floorArea)} м²")
			appendLine("   Формула: Общая мощность = Площадь × Рекомендуемая мощность")
			appendLine("   Общая мощность: ${String.format("%.2f", floorArea)} × ${String.format("%.2f", recommendedPower)} = ${String.format("%.2f", totalPowerWatts)} Вт")
			appendLine("   Общая мощность: ${String.format("%.2f", totalPowerKw)} кВт")
			appendLine()
			appendLine("6. РАСЧЁТ ПОТРЕБЛЕНИЯ ЭЛЕКТРОЭНЕРГИИ:")
			appendLine("   Часы работы в день: ${String.format("%.1f", usageHours)} ч")
			appendLine("   Формула: Потребление = Общая мощность × Часы работы")
			appendLine("   Потребление в день: ${String.format("%.2f", totalPowerKw)} × ${String.format("%.1f", usageHours)} = ${String.format("%.2f", dailyConsumption)} кВт⋅ч")
			appendLine("   Потребление в месяц: ${String.format("%.2f", dailyConsumption)} × 30 = ${String.format("%.2f", monthlyConsumption)} кВт⋅ч")
			appendLine()
			appendLine("7. РАСЧЁТ СТОИМОСТИ:")
			appendLine("   Стоимость электроэнергии: ${String.format("%.2f", electricityPrice)} ₽/кВт⋅ч")
			appendLine("   Формула: Стоимость = Потребление × Цена")
			appendLine("   Стоимость в день: ${String.format("%.2f", dailyConsumption)} × ${String.format("%.2f", electricityPrice)} = ${String.format("%.2f", dailyCost)} ₽")
			appendLine("   Стоимость в месяц: ${String.format("%.2f", monthlyConsumption)} × ${String.format("%.2f", electricityPrice)} = ${String.format("%.2f", monthlyCost)} ₽")
			appendLine()
			appendLine("8. РЕКОМЕНДАЦИИ:")
			appendLine("   • Для экономии электроэнергии используйте терморегулятор с программированием")
			appendLine("   • Улучшение утепления пола снизит потребление на 15-35%")
			appendLine("   • Оптимальная температура пола: 24-26°C для комфорта и экономии")
			appendLine("   • Учитывайте, что фактическое потребление может быть ниже из-за работы терморегулятора")
		}
	}
	
	/**
	 * Calculates water pipe parameters: diameter, flow rate, velocity, pressure loss.
	 * Formula: D = √(4Q/πV) - diameter by flow and velocity
	 *          Q = A×V = πD²V/4 - flow rate by diameter and velocity
	 *          ΔP = λ×(L/D)×(ρV²/2) - pressure loss (Darcy-Weisbach)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateWaterPipes(inputs: Map<String, Double>): Map<String, Double> {
		val calculationType = inputs["calculation_type"] ?: 1.0
		val pipeDiameter = inputs["pipe_diameter"] ?: 0.0
		val waterFlow = inputs["water_flow"] ?: 0.0
		val flowVelocity = inputs["flow_velocity"] ?: 2.0
		val pipeMaterial = inputs["pipe_material"] ?: 1.0
		val pipeLength = inputs["pipe_length"] ?: 0.0
		
		// Get roughness based on material (in mm)
		val roughness = when (pipeMaterial.toInt()) {
			1 -> 0.045 // Сталь
			2 -> 0.0015 // Медь
			3 -> 0.0015 // Пластик
			4 -> 0.1 // Чугун
			else -> 0.045 // Default: сталь
		}
		
		val roughnessMeters = roughness / 1000.0
		
		// Water properties at 20°C
		val waterDensity = 1000.0 // kg/m³
		val waterViscosity = 0.001 // Pa·s
		
		val calculatedDiameter: Double
		val calculatedFlowRate: Double
		val calculatedVelocity: Double
		val pipeArea: Double
		
		when (calculationType.toInt()) {
			1 -> { // По диаметру: расчёт расхода и скорости
				if (pipeDiameter <= 0) throw ArithmeticException("Pipe diameter must be positive")
				if (flowVelocity <= 0) throw ArithmeticException("Flow velocity must be positive")
				
				val diameterMeters = pipeDiameter / 1000.0
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = pipeArea * flowVelocity
				calculatedVelocity = flowVelocity
				calculatedDiameter = pipeDiameter
			}
			2 -> { // По расходу: расчёт диаметра и скорости
				if (waterFlow <= 0) throw ArithmeticException("Water flow must be positive")
				if (flowVelocity <= 0) throw ArithmeticException("Flow velocity must be positive")
				
				// D = √(4Q/πV)
				val diameterMeters = sqrt(4.0 * waterFlow / (PI * flowVelocity))
				calculatedDiameter = diameterMeters * 1000.0 // Convert to mm
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = waterFlow
				calculatedVelocity = flowVelocity
			}
			else -> { // По давлению (simplified - uses diameter and flow)
				if (pipeDiameter <= 0) throw ArithmeticException("Pipe diameter must be positive")
				if (waterFlow <= 0) throw ArithmeticException("Water flow must be positive")
				
				val diameterMeters = pipeDiameter / 1000.0
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = waterFlow
				calculatedVelocity = calculatedFlowRate / pipeArea
				calculatedDiameter = pipeDiameter
			}
		}
		
		// Calculate pressure loss using Darcy-Weisbach formula
		val pressureLoss: Double
		if (pipeLength > 0 && calculatedDiameter > 0) {
			val diameterMeters = calculatedDiameter / 1000.0
			
			// Calculate Reynolds number
			val reynoldsNumber = waterDensity * calculatedVelocity * diameterMeters / waterViscosity
			
			// Calculate friction factor (Blasius for turbulent flow, simplified)
			val frictionFactor = if (reynoldsNumber > 4000) {
				// Turbulent flow: Blasius formula
				0.316 / reynoldsNumber.pow(0.25)
			} else if (reynoldsNumber < 2300) {
				// Laminar flow
				64.0 / reynoldsNumber
			} else {
				// Transition zone (simplified)
				0.316 / reynoldsNumber.pow(0.25)
			}
			
			// Darcy-Weisbach: ΔP = λ×(L/D)×(ρV²/2)
			val pressureLossPa = frictionFactor * (pipeLength / diameterMeters) * (waterDensity * calculatedVelocity.pow(2) / 2.0)
			pressureLoss = pressureLossPa / 100000.0 // Convert Pa to bar
		} else {
			pressureLoss = 0.0
		}
		
		return mapOf(
			"calculated_diameter" to calculatedDiameter,
			"flow_rate_m3s" to calculatedFlowRate,
			"flow_rate_ls" to calculatedFlowRate * 1000.0, // Convert m³/s to l/s
			"flow_rate_m3h" to calculatedFlowRate * 3600.0, // Convert m³/s to m³/h
			"flow_velocity_calc" to calculatedVelocity,
			"pipe_area" to pipeArea,
			"pressure_loss" to pressureLoss
		)
	}
	
	/**
	 * Gets detailed calculation description for water pipes calculator.
	 */
	private fun getWaterPipesCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val calculationType = inputs["calculation_type"] ?: 1.0
		val pipeDiameter = inputs["pipe_diameter"] ?: 0.0
		val waterFlow = inputs["water_flow"] ?: 0.0
		val flowVelocity = inputs["flow_velocity"] ?: 2.0
		val pipeMaterial = inputs["pipe_material"] ?: 1.0
		val pipeLength = inputs["pipe_length"] ?: 0.0
		
		// Get roughness based on material
		val roughness = when (pipeMaterial.toInt()) {
			1 -> 0.045 // Сталь
			2 -> 0.0015 // Медь
			3 -> 0.0015 // Пластик
			4 -> 0.1 // Чугун
			else -> 0.045
		}
		
		val roughnessMeters = roughness / 1000.0
		
		// Water properties at 20°C
		val waterDensity = 1000.0 // kg/m³
		val waterViscosity = 0.001 // Pa·s
		
		val calculatedDiameter: Double
		val calculatedFlowRate: Double
		val calculatedVelocity: Double
		val pipeArea: Double
		
		when (calculationType.toInt()) {
			1 -> { // По диаметру: расчёт расхода и скорости
				val diameterMeters = pipeDiameter / 1000.0
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = pipeArea * flowVelocity
				calculatedVelocity = flowVelocity
				calculatedDiameter = pipeDiameter
			}
			2 -> { // По расходу: расчёт диаметра и скорости
				val diameterMeters = sqrt(4.0 * waterFlow / (PI * flowVelocity))
				calculatedDiameter = diameterMeters * 1000.0
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = waterFlow
				calculatedVelocity = flowVelocity
			}
			else -> { // По давлению
				val diameterMeters = pipeDiameter / 1000.0
				pipeArea = PI * (diameterMeters / 2.0).pow(2)
				calculatedFlowRate = waterFlow
				calculatedVelocity = calculatedFlowRate / pipeArea
				calculatedDiameter = pipeDiameter
			}
		}
		
		// Calculate pressure loss using Darcy-Weisbach formula
		val pressureLoss: Double
		val reynoldsNumber: Double
		val frictionFactor: Double
		if (pipeLength > 0 && calculatedDiameter > 0) {
			val diameterMeters = calculatedDiameter / 1000.0
			
			// Calculate Reynolds number
			reynoldsNumber = waterDensity * calculatedVelocity * diameterMeters / waterViscosity
			
			// Calculate friction factor
			frictionFactor = if (reynoldsNumber > 4000) {
				// Turbulent flow: Blasius formula
				0.316 / reynoldsNumber.pow(0.25)
			} else if (reynoldsNumber < 2300) {
				// Laminar flow
				64.0 / reynoldsNumber
			} else {
				// Transition zone
				0.316 / reynoldsNumber.pow(0.25)
			}
			
			// Darcy-Weisbach: ΔP = λ×(L/D)×(ρV²/2)
			val pressureLossPa = frictionFactor * (pipeLength / diameterMeters) * (waterDensity * calculatedVelocity.pow(2) / 2.0)
			pressureLoss = pressureLossPa / 100000.0 // Convert Pa to bar
		} else {
			reynoldsNumber = 0.0
			frictionFactor = 0.0
			pressureLoss = 0.0
		}
		
		// Get calculation type name
		val calculationTypeName = when (calculationType.toInt()) {
			1 -> "По диаметру"
			2 -> "По расходу"
			3 -> "По давлению"
			else -> "По диаметру"
		}
		
		// Get material name
		val materialName = when (pipeMaterial.toInt()) {
			1 -> "Сталь"
			2 -> "Медь"
			3 -> "Пластик"
			4 -> "Чугун"
			else -> "Сталь"
		}
		
		// Build description
		return buildString {
			appendLine("Тип расчёта: $calculationTypeName")
			appendLine("Материал трубы: $materialName")
			appendLine()
			appendLine("1. ИСХОДНЫЕ ДАННЫЕ:")
			when (calculationType.toInt()) {
				1 -> {
					appendLine("   Диаметр трубы: ${String.format("%.1f", pipeDiameter)} мм")
					appendLine("   Скорость потока: ${String.format("%.2f", flowVelocity)} м/с")
				}
				2 -> {
					appendLine("   Расход воды: ${String.format("%.4f", waterFlow)} м³/с (${String.format("%.2f", waterFlow * 1000.0)} л/с)")
					appendLine("   Скорость потока: ${String.format("%.2f", flowVelocity)} м/с")
				}
				3 -> {
					appendLine("   Диаметр трубы: ${String.format("%.1f", pipeDiameter)} мм")
					appendLine("   Расход воды: ${String.format("%.4f", waterFlow)} м³/с (${String.format("%.2f", waterFlow * 1000.0)} л/с)")
				}
			}
			appendLine("   Материал: $materialName (шероховатость: ${String.format("%.4f", roughness)} мм)")
			if (pipeLength > 0) {
				appendLine("   Длина трубы: ${String.format("%.2f", pipeLength)} м")
			}
			appendLine()
			appendLine("2. РАСЧЁТ ПЛОЩАДИ СЕЧЕНИЯ:")
			val diameterMeters = calculatedDiameter / 1000.0
			appendLine("   Диаметр: ${String.format("%.1f", calculatedDiameter)} мм = ${String.format("%.4f", diameterMeters)} м")
			appendLine("   Радиус: ${String.format("%.4f", diameterMeters / 2.0)} м")
			appendLine("   Формула: S = π × r²")
			appendLine("   Площадь: π × (${String.format("%.4f", diameterMeters / 2.0)})² = ${String.format("%.6f", pipeArea)} м²")
			appendLine()
			when (calculationType.toInt()) {
				1 -> {
					appendLine("3. РАСЧЁТ РАСХОДА ВОДЫ:")
					appendLine("   Формула: Q = S × V")
					appendLine("   Расход: ${String.format("%.6f", pipeArea)} × ${String.format("%.2f", flowVelocity)} = ${String.format("%.6f", calculatedFlowRate)} м³/с")
					appendLine("   Расход: ${String.format("%.2f", calculatedFlowRate * 1000.0)} л/с")
					appendLine("   Расход: ${String.format("%.2f", calculatedFlowRate * 3600.0)} м³/ч")
					appendLine()
					appendLine("4. СКОРОСТЬ ПОТОКА:")
					appendLine("   Скорость: ${String.format("%.2f", calculatedVelocity)} м/с (задана)")
				}
				2 -> {
					appendLine("3. РАСЧЁТ ДИАМЕТРА ТРУБЫ:")
					appendLine("   Формула: D = √(4Q/πV)")
					appendLine("   Диаметр: √(4 × ${String.format("%.6f", waterFlow)} / (π × ${String.format("%.2f", flowVelocity)}))")
					appendLine("   Диаметр: ${String.format("%.4f", diameterMeters)} м = ${String.format("%.1f", calculatedDiameter)} мм")
					appendLine()
					appendLine("4. СКОРОСТЬ ПОТОКА:")
					appendLine("   Скорость: ${String.format("%.2f", calculatedVelocity)} м/с (задана)")
				}
				3 -> {
					appendLine("3. РАСЧЁТ СКОРОСТИ ПОТОКА:")
					appendLine("   Формула: V = Q / S")
					appendLine("   Скорость: ${String.format("%.6f", calculatedFlowRate)} / ${String.format("%.6f", pipeArea)} = ${String.format("%.2f", calculatedVelocity)} м/с")
					appendLine()
					appendLine("4. РАСХОД ВОДЫ:")
					appendLine("   Расход: ${String.format("%.6f", calculatedFlowRate)} м³/с (${String.format("%.2f", calculatedFlowRate * 1000.0)} л/с)")
					appendLine("   Расход: ${String.format("%.2f", calculatedFlowRate * 3600.0)} м³/ч")
				}
			}
			appendLine()
			if (pipeLength > 0 && calculatedDiameter > 0) {
				appendLine("5. РАСЧЁТ ПОТЕРЬ ДАВЛЕНИЯ (формула Дарси-Вейсбаха):")
				appendLine("   Свойства воды (при 20°C):")
				appendLine("   • Плотность: ${String.format("%.0f", waterDensity)} кг/м³")
				appendLine("   • Вязкость: ${String.format("%.3f", waterViscosity)} Па·с")
				appendLine()
				appendLine("   Число Рейнольдса:")
				appendLine("   Формула: Re = ρ × V × D / μ")
				appendLine("   Re = ${String.format("%.0f", waterDensity)} × ${String.format("%.2f", calculatedVelocity)} × ${String.format("%.4f", diameterMeters)} / ${String.format("%.3f", waterViscosity)}")
				appendLine("   Re = ${String.format("%.0f", reynoldsNumber)}")
				when {
					reynoldsNumber < 2300 -> appendLine("   Режим течения: Ламинарный (Re < 2300)")
					reynoldsNumber > 4000 -> appendLine("   Режим течения: Турбулентный (Re > 4000)")
					else -> appendLine("   Режим течения: Переходный (2300 < Re < 4000)")
				}
				appendLine()
				appendLine("   Коэффициент трения:")
				when {
					reynoldsNumber < 2300 -> {
						appendLine("   Формула: λ = 64 / Re (ламинарное течение)")
						appendLine("   λ = 64 / ${String.format("%.0f", reynoldsNumber)} = ${String.format("%.4f", frictionFactor)}")
					}
					reynoldsNumber > 4000 -> {
						appendLine("   Формула: λ = 0.316 / Re^0.25 (Блазиус, турбулентное течение)")
						appendLine("   λ = 0.316 / ${String.format("%.0f", reynoldsNumber)}^0.25 = ${String.format("%.4f", frictionFactor)}")
					}
					else -> {
						appendLine("   Формула: λ = 0.316 / Re^0.25 (переходная зона)")
						appendLine("   λ = 0.316 / ${String.format("%.0f", reynoldsNumber)}^0.25 = ${String.format("%.4f", frictionFactor)}")
					}
				}
				appendLine()
				appendLine("   Потери давления:")
				appendLine("   Формула: ΔP = λ × (L/D) × (ρV²/2)")
				appendLine("   ΔP = ${String.format("%.4f", frictionFactor)} × (${String.format("%.2f", pipeLength)} / ${String.format("%.4f", diameterMeters)}) × (${String.format("%.0f", waterDensity)} × ${String.format("%.2f", calculatedVelocity)}² / 2)")
				val pressureLossPa = pressureLoss * 100000.0
				appendLine("   ΔP = ${String.format("%.2f", pressureLossPa)} Па = ${String.format("%.4f", pressureLoss)} бар")
			} else {
				appendLine("5. ПОТЕРИ ДАВЛЕНИЯ:")
				appendLine("   Не рассчитаны (не указана длина трубы)")
			}
			appendLine()
			appendLine("6. РЕКОМЕНДАЦИИ:")
			appendLine("   • Оптимальная скорость потока: 1.5-2.5 м/с для водопровода")
			appendLine("   • При скорости < 0.5 м/с возможны застойные зоны")
			appendLine("   • При скорости > 3 м/с увеличиваются потери давления и шум")
			if (pipeLength > 0 && pressureLoss > 0) {
				appendLine("   • Потери давления ${String.format("%.4f", pressureLoss)} бар ${if (pressureLoss > 0.5) "высокие" else "приемлемые"} для данной длины")
			}
		}
	}
	
	// Metal & Electricity Calculations
	
	/**
	 * Data class for rebar calculation result with 4 values.
	 */
	private data class RebarCalculationResult(
		val longitudinalBars: Double,
		val longitudinalLength: Double,
		val transverseBars: Double,
		val transverseLength: Double
	)
	
	/**
	 * Calculates rebar quantity, length, and mass based on structure dimensions and mesh step.
	 * Formula: Bars count = (Dimension / Step + 1) × Layers
	 * Length = Bars count × Dimension (perpendicular)
	 * Mass = Length × Mass per meter (GOST)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateRebar(inputs: Map<String, Double>): Map<String, Double> {
		val structureType = inputs["structure_type"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val height = inputs["height"] ?: 0.0
		val meshStep = inputs["mesh_step"] ?: 20.0 // in cm
		val rebarDiameter = inputs["rebar_diameter"] ?: 12.0
		val layersCount = inputs["layers_count"] ?: 2.0
		val overlap = inputs["overlap"] ?: 0.0 // in cm
		
		// Convert mesh step from cm to m
		val meshStepMeters = meshStep / 100.0
		if (meshStepMeters <= 0) throw ArithmeticException("Mesh step must be positive")
		
		// Get mass per meter according to GOST
		val massPerMeter = getRebarMassPerMeter(rebarDiameter)
		
		// Calculate based on structure type
		// Formula: Bars count = (Dimension / Step + 1) × Layers (rounded up to integer)
		val (longitudinalBars, longitudinalLength, transverseBars, transverseLength) = when (structureType.toInt()) {
			1 -> {
				// Foundation: longitudinal along length, transverse along width
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
			2 -> {
				// Slab: bars along length and width
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
			3 -> {
				// Wall: vertical along height, horizontal along length
				val vertBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val vertLength = vertBars * height
				val horBars = kotlin.math.ceil(height / meshStepMeters + 1.0) * layersCount
				val horLength = horBars * length
				RebarCalculationResult(vertBars, vertLength, horBars, horLength)
			}
			4 -> {
				// Column: longitudinal along height, transverse (hoops) around perimeter
				val longBars = 4.0 * layersCount // Standard 4 bars per layer
				val longLength = longBars * height
				val hoopStep = meshStepMeters
				val hoopCount = kotlin.math.ceil(height / hoopStep + 1.0)
				val hoopPerimeter = 2.0 * (length + width)
				val transLength = hoopCount * hoopPerimeter
				RebarCalculationResult(longBars, longLength, hoopCount, transLength)
			}
			else -> {
				// Default: same as foundation
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
		}
		
		// Add overlap if specified (convert from cm to m)
		val overlapMeters = overlap / 100.0
		val totalBars = longitudinalBars + transverseBars
		val overlapLength = if (overlapMeters > 0.0) totalBars * overlapMeters else 0.0
		
		// Calculate total length
		val totalLength: Double = longitudinalLength + transverseLength + overlapLength
		
		// Calculate total mass
		val totalMass = totalLength * massPerMeter
		
		return mapOf(
			"total_length" to totalLength,
			"total_mass" to totalMass,
			"rebar_count" to totalBars,
			"mass_per_meter" to massPerMeter
		)
	}
	
	/**
	 * Gets detailed calculation description for rebar calculator.
	 */
	private fun getRebarCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val structureType = inputs["structure_type"] ?: 1.0
		val length = inputs["length"] ?: 0.0
		val width = inputs["width"] ?: 0.0
		val height = inputs["height"] ?: 0.0
		val meshStep = inputs["mesh_step"] ?: 20.0
		val rebarDiameter = inputs["rebar_diameter"] ?: 12.0
		val layersCount = inputs["layers_count"] ?: 2.0
		val overlap = inputs["overlap"] ?: 0.0
		
		val meshStepMeters = meshStep / 100.0
		val massPerMeter = getRebarMassPerMeter(rebarDiameter)
		
		// Calculate based on structure type
		val (longitudinalBars, longitudinalLength, transverseBars, transverseLength) = when (structureType.toInt()) {
			1 -> {
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
			2 -> {
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
			3 -> {
				val vertBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val vertLength = vertBars * height
				val horBars = kotlin.math.ceil(height / meshStepMeters + 1.0) * layersCount
				val horLength = horBars * length
				RebarCalculationResult(vertBars, vertLength, horBars, horLength)
			}
			4 -> {
				val longBars = 4.0 * layersCount
				val longLength = longBars * height
				val hoopStep = meshStepMeters
				val hoopCount = kotlin.math.ceil(height / hoopStep + 1.0)
				val hoopPerimeter = 2.0 * (length + width)
				val transLength = hoopCount * hoopPerimeter
				RebarCalculationResult(longBars, longLength, hoopCount, transLength)
			}
			else -> {
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				RebarCalculationResult(longBars, longLength, transBars, transLength)
			}
		}
		
		val overlapMeters = overlap / 100.0
		val totalBars = longitudinalBars + transverseBars
		val overlapLength = if (overlapMeters > 0.0) totalBars * overlapMeters else 0.0
		val totalLength = longitudinalLength + transverseLength + overlapLength
		val totalMass = totalLength * massPerMeter
		
		// Get structure type name
		val structureTypeName = when (structureType.toInt()) {
			1 -> "Фундамент"
			2 -> "Плита"
			3 -> "Стена"
			4 -> "Колонна"
			else -> "Фундамент"
		}
		
		// Build description
		return buildString {
			appendLine("Тип конструкции: $structureTypeName")
			appendLine("Диаметр арматуры: ${rebarDiameter.toInt()} мм")
			appendLine()
			appendLine("1. ИСХОДНЫЕ ДАННЫЕ:")
			appendLine("   Длина: ${String.format("%.2f", length)} м")
			appendLine("   Ширина: ${String.format("%.2f", width)} м")
			appendLine("   Высота: ${String.format("%.2f", height)} м")
			appendLine("   Шаг сетки: ${String.format("%.0f", meshStep)} см (${String.format("%.2f", meshStepMeters)} м)")
			appendLine("   Диаметр арматуры: ${rebarDiameter.toInt()} мм")
			appendLine("   Количество слоёв: ${layersCount.toInt()}")
			if (overlap > 0) {
				appendLine("   Перехлёст: ${String.format("%.0f", overlap)} см (${String.format("%.2f", overlapMeters)} м)")
			}
			appendLine()
			appendLine("2. МАССА АРМАТУРЫ НА МЕТР (по ГОСТ 5781-82):")
			appendLine("   Диаметр ${rebarDiameter.toInt()} мм: ${String.format("%.3f", massPerMeter)} кг/м")
			appendLine()
			when (structureType.toInt()) {
				1, 2 -> {
					appendLine("3. РАСЧЁТ ПРОДОЛЬНЫХ СТЕРЖНЕЙ:")
					appendLine("   Формула: Количество = ⌈(Длина / Шаг) + 1⌉ × Слоёв")
					appendLine("   Количество: ⌈(${String.format("%.2f", length)} / ${String.format("%.2f", meshStepMeters)}) + 1⌉ × ${layersCount.toInt()}")
					appendLine("   Количество: ⌈${String.format("%.2f", length / meshStepMeters + 1.0)}⌉ × ${layersCount.toInt()} = ${longitudinalBars.toInt()} стержней")
					appendLine("   Длина одного стержня: ${String.format("%.2f", width)} м")
					appendLine("   Общая длина продольных: ${longitudinalBars.toInt()} × ${String.format("%.2f", width)} = ${String.format("%.2f", longitudinalLength)} м")
					appendLine()
					appendLine("4. РАСЧЁТ ПОПЕРЕЧНЫХ СТЕРЖНЕЙ:")
					appendLine("   Формула: Количество = ⌈(Ширина / Шаг) + 1⌉ × Слоёв")
					appendLine("   Количество: ⌈(${String.format("%.2f", width)} / ${String.format("%.2f", meshStepMeters)}) + 1⌉ × ${layersCount.toInt()}")
					appendLine("   Количество: ⌈${String.format("%.2f", width / meshStepMeters + 1.0)}⌉ × ${layersCount.toInt()} = ${transverseBars.toInt()} стержней")
					appendLine("   Длина одного стержня: ${String.format("%.2f", length)} м")
					appendLine("   Общая длина поперечных: ${transverseBars.toInt()} × ${String.format("%.2f", length)} = ${String.format("%.2f", transverseLength)} м")
				}
				3 -> {
					appendLine("3. РАСЧЁТ ВЕРТИКАЛЬНЫХ СТЕРЖНЕЙ:")
					appendLine("   Формула: Количество = ⌈(Длина / Шаг) + 1⌉ × Слоёв")
					appendLine("   Количество: ⌈(${String.format("%.2f", length)} / ${String.format("%.2f", meshStepMeters)}) + 1⌉ × ${layersCount.toInt()}")
					appendLine("   Количество: ⌈${String.format("%.2f", length / meshStepMeters + 1.0)}⌉ × ${layersCount.toInt()} = ${longitudinalBars.toInt()} стержней")
					appendLine("   Длина одного стержня: ${String.format("%.2f", height)} м")
					appendLine("   Общая длина вертикальных: ${longitudinalBars.toInt()} × ${String.format("%.2f", height)} = ${String.format("%.2f", longitudinalLength)} м")
					appendLine()
					appendLine("4. РАСЧЁТ ГОРИЗОНТАЛЬНЫХ СТЕРЖНЕЙ:")
					appendLine("   Формула: Количество = ⌈(Высота / Шаг) + 1⌉ × Слоёв")
					appendLine("   Количество: ⌈(${String.format("%.2f", height)} / ${String.format("%.2f", meshStepMeters)}) + 1⌉ × ${layersCount.toInt()}")
					appendLine("   Количество: ⌈${String.format("%.2f", height / meshStepMeters + 1.0)}⌉ × ${layersCount.toInt()} = ${transverseBars.toInt()} стержней")
					appendLine("   Длина одного стержня: ${String.format("%.2f", length)} м")
					appendLine("   Общая длина горизонтальных: ${transverseBars.toInt()} × ${String.format("%.2f", length)} = ${String.format("%.2f", transverseLength)} м")
				}
				4 -> {
					appendLine("3. РАСЧЁТ ПРОДОЛЬНЫХ СТЕРЖНЕЙ:")
					appendLine("   Стандартное количество: 4 стержня на слой")
					appendLine("   Количество: 4 × ${layersCount.toInt()} = ${longitudinalBars.toInt()} стержней")
					appendLine("   Длина одного стержня: ${String.format("%.2f", height)} м")
					appendLine("   Общая длина продольных: ${longitudinalBars.toInt()} × ${String.format("%.2f", height)} = ${String.format("%.2f", longitudinalLength)} м")
					appendLine()
					appendLine("4. РАСЧЁТ ХОМУТОВ:")
					appendLine("   Формула: Количество = ⌈(Высота / Шаг) + 1⌉")
					appendLine("   Количество: ⌈(${String.format("%.2f", height)} / ${String.format("%.2f", meshStepMeters)}) + 1⌉")
					appendLine("   Количество: ⌈${String.format("%.2f", height / meshStepMeters + 1.0)}⌉ = ${transverseBars.toInt()} хомутов")
					val hoopPerimeter = 2.0 * (length + width)
					appendLine("   Периметр колонны: 2 × (${String.format("%.2f", length)} + ${String.format("%.2f", width)}) = ${String.format("%.2f", hoopPerimeter)} м")
					appendLine("   Длина одного хомута: ${String.format("%.2f", hoopPerimeter)} м")
					appendLine("   Общая длина хомутов: ${transverseBars.toInt()} × ${String.format("%.2f", hoopPerimeter)} = ${String.format("%.2f", transverseLength)} м")
				}
			}
			appendLine()
			if (overlap > 0) {
				appendLine("5. РАСЧЁТ ПЕРЕХЛЁСТА:")
				appendLine("   Общее количество стержней: ${totalBars.toInt()} шт")
				appendLine("   Перехлёст на стержень: ${String.format("%.2f", overlapMeters)} м")
				appendLine("   Общая длина перехлёста: ${totalBars.toInt()} × ${String.format("%.2f", overlapMeters)} = ${String.format("%.2f", overlapLength)} м")
				appendLine()
			}
			val overlapSection = if (overlap > 0) 5 else 4
			appendLine("${if (overlap > 0) "6" else "5"}. ОБЩАЯ ДЛИНА АРМАТУРЫ:")
			appendLine("   Продольные/вертикальные: ${String.format("%.2f", longitudinalLength)} м")
			appendLine("   Поперечные/горизонтальные/хомуты: ${String.format("%.2f", transverseLength)} м")
			if (overlap > 0) {
				appendLine("   Перехлёст: ${String.format("%.2f", overlapLength)} м")
			}
			appendLine("   Общая длина: ${String.format("%.2f", longitudinalLength)} + ${String.format("%.2f", transverseLength)}${if (overlap > 0) " + ${String.format("%.2f", overlapLength)}" else ""} = ${String.format("%.2f", totalLength)} м")
			appendLine()
			appendLine("${if (overlap > 0) "7" else "6"}. ОБЩАЯ МАССА АРМАТУРЫ:")
			appendLine("   Формула: Масса = Длина × Масса на метр")
			appendLine("   Масса: ${String.format("%.2f", totalLength)} × ${String.format("%.3f", massPerMeter)} = ${String.format("%.2f", totalMass)} кг")
			appendLine("   Масса: ${String.format("%.2f", totalMass / 1000.0)} т")
			appendLine()
			appendLine("${if (overlap > 0) "8" else "7"}. РЕКОМЕНДАЦИИ:")
			when (structureType.toInt()) {
				1 -> {
					appendLine("   • Для ленточного фундамента оптимальный шаг сетки: 15-25 см")
					appendLine("   • Рекомендуемый диаметр: 10-12 мм для частных домов, 12-16 мм для больших нагрузок")
					appendLine("   • Используйте 2 слоя для верхнего и нижнего поясов армирования")
				}
				2 -> {
					appendLine("   • Для плиты перекрытия оптимальный шаг сетки: 10-20 см")
					appendLine("   • Рекомендуемый диаметр: 8-12 мм для плит до 20 см, 12-14 мм для толстых плит")
					appendLine("   • Обязательно используйте 2 слоя для сопротивления изгибу в обе стороны")
				}
				3 -> {
					appendLine("   • Для стены оптимальный шаг сетки: 15-30 см")
					appendLine("   • Рекомендуемый диаметр: 6-10 мм для тонких стен, 10-12 мм для толстых")
					appendLine("   • Для тонких стен достаточно 1 слоя, для толстых - 2 слоя")
				}
				4 -> {
					appendLine("   • Для колонны стандартное количество продольных стержней: 4 шт на слой")
					appendLine("   • Рекомендуемый диаметр продольной: 12-20 мм в зависимости от нагрузки")
					appendLine("   • Шаг хомутов: 10-20 см для обеспечения поперечного армирования")
				}
			}
			appendLine("   • Перехлёст при соединении должен быть не менее 30-50 диаметров арматуры")
			appendLine("   • Учитывайте защитный слой бетона 2-5 см при монтаже")
		}
	}
	
	/**
	 * Returns mass per meter of rebar according to GOST based on diameter.
	 * ГОСТ 5781-82: Standard weights for reinforcing steel bars.
	 */
	private fun getRebarMassPerMeter(diameter: Double): Double {
		return when (diameter.toInt()) {
			6 -> 0.222
			8 -> 0.395
			10 -> 0.617
			12 -> 0.888
			14 -> 1.21
			16 -> 1.58
			18 -> 2.0
			20 -> 2.47
			22 -> 2.98
			25 -> 3.85
			else -> {
				// Calculate from formula: m = π × (d/2)² × ρ
				// where d is diameter in mm, ρ = 7850 kg/m³
				val diameterMeters = diameter / 1000.0
				PI * (diameterMeters / 2.0).pow(2) * 7850.0
			}
		}
	}
	
	/**
	 * Calculates cable section based on power or current, voltage, length, material, network type.
	 * Formula: I = P / (U × cosφ) for single-phase, I = P / (√3 × U × cosφ) for three-phase
	 *          S = I / (J × k) - section by current density
	 *          ΔU = 2 × I × R × L / 1000 - voltage drop (single-phase)
	 *          ΔU = √3 × I × R × L / 1000 - voltage drop (three-phase)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateCableSection(inputs: Map<String, Double>): Map<String, Double> {
		val calculationType = inputs["calculation_type"] ?: 1.0
		val power = inputs["power"] ?: 0.0
		val current = inputs["current"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val cableLength = inputs["cable_length"] ?: 0.0
		val conductorMaterial = inputs["conductor_material"] ?: 1.0
		val networkType = inputs["network_type"] ?: 1.0
		val powerFactor = inputs["power_factor"] ?: 0.8
		val voltageDropPercent = inputs["voltage_drop_percent"] ?: 3.0
		val ambientTemperature = inputs["ambient_temperature"] ?: 25.0
		val installationType = inputs["installation_type"] ?: 1.0
		
		if (voltage <= 0) throw ArithmeticException("Voltage must be positive")
		if (powerFactor <= 0) throw ArithmeticException("Power factor must be positive")
		
		// Calculate current
		val calculatedCurrent = when (calculationType.toInt()) {
			1 -> { // По мощности
				if (power <= 0) throw ArithmeticException("Power must be positive")
		val powerWatts = power * 1000.0
				if (networkType.toInt() == 2) {
					// Three-phase: I = P / (√3 × U × cosφ)
					powerWatts / (sqrt(3.0) * voltage * powerFactor)
				} else {
					// Single-phase: I = P / (U × cosφ)
					powerWatts / (voltage * powerFactor)
				}
			}
			else -> { // По току
				if (current <= 0) throw ArithmeticException("Current must be positive")
				current
			}
		}
		
		// Get material properties
		val resistivity = when (conductorMaterial.toInt()) {
			1 -> 0.0175 // Медь (Ом·мм²/м)
			2 -> 0.0283 // Алюминий (Ом·мм²/м)
			else -> 0.0175
		}
		
		// Get current density based on material and installation type
		val baseCurrentDensity = when {
			conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> 10.0 // Медь, открытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> 8.0  // Медь, закрытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> 7.0  // Медь, подземная
			conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> 6.0  // Алюминий, открытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> 5.0  // Алюминий, закрытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> 4.5  // Алюминий, подземная
			else -> 8.0
		}
		
		// Temperature correction factor
		val temperatureFactor = when {
			ambientTemperature <= 25.0 -> 1.0
			ambientTemperature <= 30.0 -> 0.95
			ambientTemperature <= 35.0 -> 0.9
			ambientTemperature <= 40.0 -> 0.85
			else -> 0.8
		}
		
		val currentDensity = baseCurrentDensity * temperatureFactor
		
		// Calculate section by current density
		val cableSection = calculatedCurrent / currentDensity
		
		// Round to standard section
		val standardSections = listOf(1.0, 1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0)
		val standardSection = standardSections.firstOrNull { it >= cableSection } ?: standardSections.last()
		
		// Calculate voltage drop
		val resistance = (resistivity * cableLength * 2.0) / standardSection // 2 for forward and return
		val voltageDrop = if (networkType.toInt() == 2) {
			// Three-phase: ΔU = √3 × I × R × L / 1000
			sqrt(3.0) * calculatedCurrent * resistance
		} else {
			// Single-phase: ΔU = 2 × I × R × L / 1000
			2.0 * calculatedCurrent * resistance
		}
		val voltageDropPercentCalc = (voltageDrop / voltage) * 100.0
		
		// If voltage drop exceeds allowed, increase section
		val finalSection = if (voltageDropPercentCalc > voltageDropPercent && standardSection < 50.0) {
			val nextStandardSection = standardSections.firstOrNull { it > standardSection } ?: standardSection
			nextStandardSection
		} else {
			standardSection
		}
		
		// Recalculate voltage drop with final section
		val finalResistance = (resistivity * cableLength * 2.0) / finalSection
		val finalVoltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * finalResistance
		} else {
			2.0 * calculatedCurrent * finalResistance
		}
		val finalVoltageDropPercent = (finalVoltageDrop / voltage) * 100.0
		
		return mapOf(
			"calculated_current" to calculatedCurrent,
			"cable_section" to cableSection,
			"standard_section" to finalSection,
			"voltage_drop" to finalVoltageDrop,
			"voltage_drop_percent_calc" to finalVoltageDropPercent
		)
	}
	
	/**
	 * Gets detailed calculation description for cable section calculator.
	 */
	private fun getCableSectionCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val calculationType = inputs["calculation_type"] ?: 1.0
		val power = inputs["power"] ?: 0.0
		val current = inputs["current"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val cableLength = inputs["cable_length"] ?: 0.0
		val conductorMaterial = inputs["conductor_material"] ?: 1.0
		val networkType = inputs["network_type"] ?: 1.0
		val powerFactor = inputs["power_factor"] ?: 0.8
		val voltageDropPercent = inputs["voltage_drop_percent"] ?: 3.0
		val ambientTemperature = inputs["ambient_temperature"] ?: 25.0
		val installationType = inputs["installation_type"] ?: 1.0
		
		// Calculate current
		val calculatedCurrent = when (calculationType.toInt()) {
			1 -> { // По мощности
				val powerWatts = power * 1000.0
				if (networkType.toInt() == 2) {
					powerWatts / (sqrt(3.0) * voltage * powerFactor)
				} else {
					powerWatts / (voltage * powerFactor)
				}
			}
			else -> { // По току
				current
			}
		}
		
		// Get material properties
		val resistivity = when (conductorMaterial.toInt()) {
			1 -> 0.0175 // Медь
			2 -> 0.0283 // Алюминий
			else -> 0.0175
		}
		
		// Get base current density
		val baseCurrentDensity = when {
			conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> 10.0 // Медь, открытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> 8.0  // Медь, закрытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> 9.0  // Медь, подземная
			conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> 6.0  // Алюминий, открытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> 5.0  // Алюминий, закрытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> 5.5  // Алюминий, подземная
			else -> 10.0
		}
		
		// Temperature correction factor
		val temperatureFactor = when {
			ambientTemperature <= 25.0 -> 1.0
			ambientTemperature <= 30.0 -> 0.95
			ambientTemperature <= 35.0 -> 0.9
			ambientTemperature <= 40.0 -> 0.85
			else -> 0.8
		}
		
		val currentDensity = baseCurrentDensity * temperatureFactor
		
		// Calculate section by current density
		val cableSection = calculatedCurrent / currentDensity
		
		// Round to standard section
		val standardSections = listOf(1.0, 1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0)
		val standardSection = standardSections.firstOrNull { it >= cableSection } ?: standardSections.last()
		
		// Calculate voltage drop
		val resistance = (resistivity * cableLength * 2.0) / standardSection
		val voltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * resistance
		} else {
			2.0 * calculatedCurrent * resistance
		}
		val voltageDropPercentCalc = (voltageDrop / voltage) * 100.0
		
		// If voltage drop exceeds allowed, increase section
		val finalSection = if (voltageDropPercentCalc > voltageDropPercent && standardSection < 50.0) {
			val nextStandardSection = standardSections.firstOrNull { it > standardSection } ?: standardSection
			nextStandardSection
		} else {
			standardSection
		}
		
		// Recalculate voltage drop with final section
		val finalResistance = (resistivity * cableLength * 2.0) / finalSection
		val finalVoltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * finalResistance
		} else {
			2.0 * calculatedCurrent * finalResistance
		}
		val finalVoltageDropPercent = (finalVoltageDrop / voltage) * 100.0
		
		// Get calculation type name
		val calculationTypeName = when (calculationType.toInt()) {
			1 -> "По мощности"
			2 -> "По току"
			else -> "По мощности"
		}
		
		// Get material name
		val materialName = when (conductorMaterial.toInt()) {
			1 -> "Медь"
			2 -> "Алюминий"
			else -> "Медь"
		}
		
		// Get network type name
		val networkTypeName = when (networkType.toInt()) {
			1 -> "Однофазная"
			2 -> "Трёхфазная"
			else -> "Однофазная"
		}
		
		// Get installation type name
		val installationTypeName = when (installationType.toInt()) {
			1 -> "Открытая прокладка"
			2 -> "Закрытая прокладка"
			3 -> "Подземная прокладка"
			else -> "Открытая прокладка"
		}
		
		// Build description
		return buildString {
			appendLine("Тип расчёта: $calculationTypeName")
			appendLine("Материал: $materialName")
			appendLine("Тип сети: $networkTypeName")
			appendLine()
			appendLine("1. ИСХОДНЫЕ ДАННЫЕ:")
			when (calculationType.toInt()) {
				1 -> {
					appendLine("   Мощность: ${String.format("%.2f", power)} кВт (${String.format("%.0f", power * 1000.0)} Вт)")
					appendLine("   Напряжение: ${String.format("%.0f", voltage)} В")
					appendLine("   Коэффициент мощности: ${String.format("%.2f", powerFactor)}")
				}
				else -> {
					appendLine("   Ток: ${String.format("%.2f", current)} А")
					appendLine("   Напряжение: ${String.format("%.0f", voltage)} В")
				}
			}
			appendLine("   Длина линии: ${String.format("%.2f", cableLength)} м")
			appendLine("   Материал: $materialName")
			appendLine("   Тип сети: $networkTypeName")
			appendLine("   Способ прокладки: $installationTypeName")
			appendLine("   Температура окружающей среды: ${String.format("%.0f", ambientTemperature)}°C")
			appendLine("   Допустимые потери напряжения: ${String.format("%.1f", voltageDropPercent)}%")
			appendLine()
			appendLine("2. РАСЧЁТ ТОКА НАГРУЗКИ:")
			when (calculationType.toInt()) {
				1 -> {
					if (networkType.toInt() == 2) {
						appendLine("   Формула: I = P / (√3 × U × cosφ)")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / (√3 × ${String.format("%.0f", voltage)} × ${String.format("%.2f", powerFactor)})")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / ${String.format("%.2f", sqrt(3.0) * voltage * powerFactor)} = ${String.format("%.2f", calculatedCurrent)} А")
					} else {
						appendLine("   Формула: I = P / (U × cosφ)")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / (${String.format("%.0f", voltage)} × ${String.format("%.2f", powerFactor)})")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / ${String.format("%.2f", voltage * powerFactor)} = ${String.format("%.2f", calculatedCurrent)} А")
					}
				}
				else -> {
					appendLine("   Ток: ${String.format("%.2f", calculatedCurrent)} А (задан)")
				}
			}
			appendLine()
			appendLine("3. ОПРЕДЕЛЕНИЕ ДОПУСТИМОЙ ПЛОТНОСТИ ТОКА:")
			appendLine("   Материал: $materialName")
			appendLine("   Способ прокладки: $installationTypeName")
			when {
				conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> appendLine("   Базовая плотность тока: 10 А/мм² (медь, открытая прокладка)")
				conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> appendLine("   Базовая плотность тока: 8 А/мм² (медь, закрытая прокладка)")
				conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> appendLine("   Базовая плотность тока: 9 А/мм² (медь, подземная прокладка)")
				conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> appendLine("   Базовая плотность тока: 6 А/мм² (алюминий, открытая прокладка)")
				conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> appendLine("   Базовая плотность тока: 5 А/мм² (алюминий, закрытая прокладка)")
				conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> appendLine("   Базовая плотность тока: 5.5 А/мм² (алюминий, подземная прокладка)")
			}
			when {
				ambientTemperature <= 25.0 -> appendLine("   Коэффициент температуры: 1.0 (температура ≤ 25°C)")
				ambientTemperature <= 30.0 -> appendLine("   Коэффициент температуры: 0.95 (температура 25-30°C)")
				ambientTemperature <= 35.0 -> appendLine("   Коэффициент температуры: 0.9 (температура 30-35°C)")
				ambientTemperature <= 40.0 -> appendLine("   Коэффициент температуры: 0.85 (температура 35-40°C)")
				else -> appendLine("   Коэффициент температуры: 0.8 (температура > 40°C)")
			}
			appendLine("   Допустимая плотность тока: ${String.format("%.1f", baseCurrentDensity)} × ${String.format("%.2f", temperatureFactor)} = ${String.format("%.2f", currentDensity)} А/мм²")
			appendLine()
			appendLine("4. РАСЧЁТ СЕЧЕНИЯ КАБЕЛЯ:")
			appendLine("   Формула: S = I / J")
			appendLine("   Сечение: ${String.format("%.2f", calculatedCurrent)} / ${String.format("%.2f", currentDensity)} = ${String.format("%.2f", cableSection)} мм²")
			appendLine("   Стандартное сечение: ${String.format("%.1f", standardSection)} мм²")
			appendLine()
			appendLine("5. РАСЧЁТ ПОТЕРЬ НАПРЯЖЕНИЯ:")
			when (conductorMaterial.toInt()) {
				1 -> appendLine("   Удельное сопротивление меди: 0.0175 Ом·мм²/м")
				2 -> appendLine("   Удельное сопротивление алюминия: 0.0283 Ом·мм²/м")
			}
			appendLine("   Сопротивление линии: R = (ρ × L × 2) / S")
			appendLine("   Сопротивление: (${String.format("%.4f", resistivity)} × ${String.format("%.2f", cableLength)} × 2) / ${String.format("%.1f", finalSection)}")
			appendLine("   Сопротивление: ${String.format("%.4f", finalResistance)} Ом")
			appendLine()
			if (networkType.toInt() == 2) {
				appendLine("   Формула потерь (трёхфазная): ΔU = √3 × I × R")
				appendLine("   Потери: √3 × ${String.format("%.2f", calculatedCurrent)} × ${String.format("%.4f", finalResistance)}")
				appendLine("   Потери: ${String.format("%.2f", finalVoltageDrop)} В")
			} else {
				appendLine("   Формула потерь (однофазная): ΔU = 2 × I × R")
				appendLine("   Потери: 2 × ${String.format("%.2f", calculatedCurrent)} × ${String.format("%.4f", finalResistance)}")
				appendLine("   Потери: ${String.format("%.2f", finalVoltageDrop)} В")
			}
			appendLine("   Потери в процентах: (${String.format("%.2f", finalVoltageDrop)} / ${String.format("%.0f", voltage)}) × 100 = ${String.format("%.2f", finalVoltageDropPercent)}%")
			if (finalVoltageDropPercent > voltageDropPercent) {
				appendLine("   ⚠️ Потери превышают допустимые (${String.format("%.1f", voltageDropPercent)}%)")
				appendLine("   Увеличено сечение до ${String.format("%.1f", finalSection)} мм²")
			} else {
				appendLine("   ✓ Потери в пределах допустимых (${String.format("%.1f", voltageDropPercent)}%)")
			}
			appendLine()
			appendLine("6. РЕКОМЕНДАЦИИ:")
			appendLine("   • Рекомендуемое сечение: ${String.format("%.1f", finalSection)} мм²")
			when (finalSection) {
				1.0, 1.5 -> appendLine("   • Применение: освещение, маломощные потребители")
				2.5 -> appendLine("   • Применение: розетки в квартире, бытовая техника")
				4.0, 6.0 -> appendLine("   • Применение: электроплиты, водонагреватели, мощные потребители")
				10.0, 16.0 -> appendLine("   • Применение: ввод в дом, распределительные линии")
				else -> appendLine("   • Применение: мощные нагрузки, промышленное использование")
			}
			when (conductorMaterial.toInt()) {
				1 -> appendLine("   • Медный кабель предпочтительнее: безопаснее и долговечнее")
				2 -> appendLine("   • Алюминиевый кабель требует большего сечения, используется для наружной прокладки")
			}
			appendLine("   • Установите автоматический выключатель с номиналом не выше допустимого тока для сечения ${String.format("%.1f", finalSection)} мм²")
			if (finalVoltageDropPercent > 3.0) {
				appendLine("   • ⚠️ Потери напряжения высокие - рассмотрите увеличение сечения или уменьшение длины линии")
			}
		}
	}
	
	/**
	 * Calculates electrical parameters: current, breaker rating, cable section.
	 * Formula: I = P / (U × cosφ) for single-phase, I = P / (√3 × U × cosφ) for three-phase
	 *          S = I / J - section by current density
	 *          Breaker = I × 1.25 (25% reserve)
	 *          ΔU = 2 × I × R × L / 1000 - voltage drop (single-phase)
	 *          ΔU = √3 × I × R × L / 1000 - voltage drop (three-phase)
	 * Based on calc1.ru calculation method
	 */
	private fun calculateElectrical(inputs: Map<String, Double>): Map<String, Double> {
		val calculationType = inputs["calculation_type"] ?: 3.0
		val inputMethod = inputs["input_method"] ?: 1.0
		val power = inputs["power"] ?: 0.0
		val current = inputs["current"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val networkType = inputs["network_type"] ?: 1.0
		val cableLength = inputs["cable_length"] ?: 15.0
		val conductorMaterial = inputs["conductor_material"] ?: 1.0
		val installationType = inputs["installation_type"] ?: 1.0
		val powerFactor = inputs["power_factor"] ?: 0.9
		val voltageDropPercent = inputs["voltage_drop_percent"] ?: 3.0
		val breakerType = inputs["breaker_type"] ?: 1.0
		
		if (voltage <= 0) throw ArithmeticException("Voltage must be positive")
		if (powerFactor <= 0) throw ArithmeticException("Power factor must be positive")
		
		// Calculate current
		val calculatedCurrent = when (inputMethod.toInt()) {
			1 -> { // По мощности
				if (power <= 0) throw ArithmeticException("Power must be positive")
				val powerWatts = power * 1000.0
				if (networkType.toInt() == 2) {
					// Three-phase: I = P / (√3 × U × cosφ)
					powerWatts / (sqrt(3.0) * voltage * powerFactor)
		} else {
					// Single-phase: I = P / (U × cosφ)
					powerWatts / (voltage * powerFactor)
				}
			}
			else -> { // По току
				if (current <= 0) throw ArithmeticException("Current must be positive")
				current
			}
		}
		
		// Get material properties
		val resistivity = when (conductorMaterial.toInt()) {
			1 -> 0.0175 // Медь (Ом·мм²/м)
			2 -> 0.0283 // Алюминий (Ом·мм²/м)
			else -> 0.0175
		}
		
		// Get current density based on material and installation type (according to calc1.ru)
		val currentDensity = when {
			conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> 4.0 // Медь, открытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> 3.0  // Медь, скрытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> 3.5  // Медь, в кабель-канале
			conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> 3.0  // Алюминий, открытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> 2.0  // Алюминий, скрытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> 2.5  // Алюминий, в кабель-канале
			else -> 4.0
		}
		
		// Calculate section by current density
		val cableSection = calculatedCurrent / currentDensity
		
		// Round to standard section
		val standardSections = listOf(0.75, 1.0, 1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0)
		var standardSection = standardSections.firstOrNull { it >= cableSection } ?: standardSections.last()
		
		// Calculate voltage drop
		val resistance = (resistivity * cableLength * 2.0) / standardSection // 2 for forward and return
		val voltageDrop = if (networkType.toInt() == 2) {
			// Three-phase: ΔU = √3 × I × R
			sqrt(3.0) * calculatedCurrent * resistance
		} else {
			// Single-phase: ΔU = 2 × I × R
			2.0 * calculatedCurrent * resistance
		}
		val voltageDropPercentCalc = (voltageDrop / voltage) * 100.0
		
		// If voltage drop exceeds allowed, increase section
		if (voltageDropPercentCalc > voltageDropPercent && standardSection < 50.0) {
			val nextStandardSection = standardSections.firstOrNull { it > standardSection }
			if (nextStandardSection != null) {
				standardSection = nextStandardSection
			}
		}
		
		// Recalculate voltage drop with final section
		val finalResistance = (resistivity * cableLength * 2.0) / standardSection
		val finalVoltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * finalResistance
		} else {
			2.0 * calculatedCurrent * finalResistance
		}
		val finalVoltageDropPercent = (finalVoltageDrop / voltage) * 100.0
		
		// Calculate breaker current with 25% reserve
		val breakerCurrent = calculatedCurrent * 1.25
		
		// Round breaker current to standard values
		val standardBreakerCurrents = listOf(6.0, 10.0, 16.0, 20.0, 25.0, 32.0, 40.0, 50.0, 63.0, 80.0, 100.0)
		val standardBreakerCurrent = standardBreakerCurrents.firstOrNull { it >= breakerCurrent } ?: standardBreakerCurrents.last()
		
		// Breaker type label
		val breakerTypeLabel = when (breakerType.toInt()) {
			1 -> "C"
			2 -> "B"
			3 -> "D"
			else -> "C"
		}
		
		// Build result map based on calculation type
		val result = mutableMapOf<String, Double>()
		
		if (calculationType.toInt() == 1 || calculationType.toInt() == 3) {
			// Cable section calculation
			result["calculated_current"] = calculatedCurrent
			result["cable_section"] = cableSection
			result["standard_section"] = standardSection
			result["voltage_drop"] = finalVoltageDrop
			result["voltage_drop_percent_calc"] = finalVoltageDropPercent
		}
		
		if (calculationType.toInt() == 2 || calculationType.toInt() == 3) {
			// Breaker calculation
			result["calculated_current"] = calculatedCurrent
			result["breaker_current"] = standardBreakerCurrent
			// Store breaker type as number for display (will be converted to string in UI)
			result["breaker_type_result"] = breakerType
		}
		
		// Always include current
		if (!result.containsKey("calculated_current")) {
			result["calculated_current"] = calculatedCurrent
		}
		
		return result
	}
	
	/**
	 * Gets detailed calculation description for electrical calculator.
	 */
	private fun getElectricalCalculationDetails(inputs: Map<String, Double>): String {
		// Reuse the same calculation logic to get intermediate values
		val calculationType = inputs["calculation_type"] ?: 3.0
		val inputMethod = inputs["input_method"] ?: 1.0
		val power = inputs["power"] ?: 0.0
		val current = inputs["current"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val networkType = inputs["network_type"] ?: 1.0
		val cableLength = inputs["cable_length"] ?: 15.0
		val conductorMaterial = inputs["conductor_material"] ?: 1.0
		val installationType = inputs["installation_type"] ?: 1.0
		val powerFactor = inputs["power_factor"] ?: 0.9
		val voltageDropPercent = inputs["voltage_drop_percent"] ?: 3.0
		val breakerType = inputs["breaker_type"] ?: 1.0
		
		// Calculate current
		val calculatedCurrent = when (inputMethod.toInt()) {
			1 -> { // По мощности
				val powerWatts = power * 1000.0
				if (networkType.toInt() == 2) {
					powerWatts / (sqrt(3.0) * voltage * powerFactor)
				} else {
					powerWatts / (voltage * powerFactor)
				}
			}
			else -> { // По току
				current
			}
		}
		
		// Get material properties
		val resistivity = when (conductorMaterial.toInt()) {
			1 -> 0.0175 // Медь
			2 -> 0.0283 // Алюминий
			else -> 0.0175
		}
		
		// Get current density
		val currentDensity = when {
			conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> 4.0 // Медь, открытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> 3.0  // Медь, скрытая
			conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> 3.5  // Медь, в кабель-канале
			conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> 3.0  // Алюминий, открытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> 2.0  // Алюминий, скрытая
			conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> 2.5  // Алюминий, в кабель-канале
			else -> 4.0
		}
		
		// Calculate section by current density
		val cableSection = calculatedCurrent / currentDensity
		
		// Round to standard section
		val standardSections = listOf(0.75, 1.0, 1.5, 2.5, 4.0, 6.0, 10.0, 16.0, 25.0, 35.0, 50.0)
		var standardSection = standardSections.firstOrNull { it >= cableSection } ?: standardSections.last()
		
		// Calculate voltage drop
		val resistance = (resistivity * cableLength * 2.0) / standardSection
		val voltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * resistance
		} else {
			2.0 * calculatedCurrent * resistance
		}
		val voltageDropPercentCalc = (voltageDrop / voltage) * 100.0
		
		// If voltage drop exceeds allowed, increase section
		if (voltageDropPercentCalc > voltageDropPercent && standardSection < 50.0) {
			val nextStandardSection = standardSections.firstOrNull { it > standardSection }
			if (nextStandardSection != null) {
				standardSection = nextStandardSection
			}
		}
		
		// Recalculate voltage drop with final section
		val finalResistance = (resistivity * cableLength * 2.0) / standardSection
		val finalVoltageDrop = if (networkType.toInt() == 2) {
			sqrt(3.0) * calculatedCurrent * finalResistance
		} else {
			2.0 * calculatedCurrent * finalResistance
		}
		val finalVoltageDropPercent = (finalVoltageDrop / voltage) * 100.0
		
		// Calculate breaker current with 25% reserve
		val breakerCurrent = calculatedCurrent * 1.25
		
		// Round breaker current to standard values
		val standardBreakerCurrents = listOf(6.0, 10.0, 16.0, 20.0, 25.0, 32.0, 40.0, 50.0, 63.0, 80.0, 100.0)
		val standardBreakerCurrent = standardBreakerCurrents.firstOrNull { it >= breakerCurrent } ?: standardBreakerCurrents.last()
		
		// Get calculation type name
		val calculationTypeName = when (calculationType.toInt()) {
			1 -> "Сечение кабеля"
			2 -> "Автомат"
			3 -> "Оба расчёта"
			else -> "Оба расчёта"
		}
		
		// Get input method name
		val inputMethodName = when (inputMethod.toInt()) {
			1 -> "По мощности"
			2 -> "По току"
			else -> "По мощности"
		}
		
		// Get material name
		val materialName = when (conductorMaterial.toInt()) {
			1 -> "Медь"
			2 -> "Алюминий"
			else -> "Медь"
		}
		
		// Get network type name
		val networkTypeName = when (networkType.toInt()) {
			1 -> "Однофазная"
			2 -> "Трёхфазная"
			else -> "Однофазная"
		}
		
		// Get installation type name
		val installationTypeName = when (installationType.toInt()) {
			1 -> "Открытая прокладка"
			2 -> "Скрытая прокладка"
			3 -> "В кабель-канале"
			else -> "Открытая прокладка"
		}
		
		// Get breaker type name
		val breakerTypeName = when (breakerType.toInt()) {
			1 -> "Тип C (бытовые)"
			2 -> "Тип B (слабоиндуктивные)"
			3 -> "Тип D (высокие пусковые токи)"
			else -> "Тип C"
		}
		
		// Build description
		return buildString {
			appendLine("Тип расчёта: $calculationTypeName")
			appendLine("Способ ввода: $inputMethodName")
			appendLine()
			appendLine("1. ИСХОДНЫЕ ДАННЫЕ:")
			when (inputMethod.toInt()) {
				1 -> {
					appendLine("   Мощность: ${String.format("%.2f", power)} кВт (${String.format("%.0f", power * 1000.0)} Вт)")
					appendLine("   Напряжение: ${String.format("%.0f", voltage)} В")
					appendLine("   Коэффициент мощности: ${String.format("%.2f", powerFactor)}")
				}
				else -> {
					appendLine("   Ток: ${String.format("%.2f", current)} А")
					appendLine("   Напряжение: ${String.format("%.0f", voltage)} В")
				}
			}
			appendLine("   Длина кабеля: ${String.format("%.2f", cableLength)} м")
			appendLine("   Материал: $materialName")
			appendLine("   Тип сети: $networkTypeName")
			appendLine("   Способ прокладки: $installationTypeName")
			appendLine("   Допустимое падение напряжения: ${String.format("%.1f", voltageDropPercent)}%")
			if (calculationType.toInt() == 2 || calculationType.toInt() == 3) {
				appendLine("   Тип автомата: $breakerTypeName")
			}
			appendLine()
			appendLine("2. РАСЧЁТ ТОКА НАГРУЗКИ:")
			when (inputMethod.toInt()) {
				1 -> {
					if (networkType.toInt() == 2) {
						appendLine("   Формула (трёхфазная): I = P / (√3 × U × cosφ)")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / (√3 × ${String.format("%.0f", voltage)} × ${String.format("%.2f", powerFactor)})")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / ${String.format("%.2f", sqrt(3.0) * voltage * powerFactor)} = ${String.format("%.2f", calculatedCurrent)} А")
					} else {
						appendLine("   Формула (однофазная): I = P / (U × cosφ)")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / (${String.format("%.0f", voltage)} × ${String.format("%.2f", powerFactor)})")
						appendLine("   Ток: ${String.format("%.0f", power * 1000.0)} / ${String.format("%.2f", voltage * powerFactor)} = ${String.format("%.2f", calculatedCurrent)} А")
					}
				}
				else -> {
					appendLine("   Ток: ${String.format("%.2f", calculatedCurrent)} А (задан)")
				}
			}
			appendLine()
			if (calculationType.toInt() == 1 || calculationType.toInt() == 3) {
				appendLine("3. РАСЧЁТ СЕЧЕНИЯ КАБЕЛЯ:")
				appendLine("   Материал: $materialName")
				appendLine("   Способ прокладки: $installationTypeName")
				when {
					conductorMaterial.toInt() == 1 && installationType.toInt() == 1 -> appendLine("   Плотность тока: 4 А/мм² (медь, открытая прокладка)")
					conductorMaterial.toInt() == 1 && installationType.toInt() == 2 -> appendLine("   Плотность тока: 3 А/мм² (медь, скрытая прокладка)")
					conductorMaterial.toInt() == 1 && installationType.toInt() == 3 -> appendLine("   Плотность тока: 3.5 А/мм² (медь, в кабель-канале)")
					conductorMaterial.toInt() == 2 && installationType.toInt() == 1 -> appendLine("   Плотность тока: 3 А/мм² (алюминий, открытая прокладка)")
					conductorMaterial.toInt() == 2 && installationType.toInt() == 2 -> appendLine("   Плотность тока: 2 А/мм² (алюминий, скрытая прокладка)")
					conductorMaterial.toInt() == 2 && installationType.toInt() == 3 -> appendLine("   Плотность тока: 2.5 А/мм² (алюминий, в кабель-канале)")
				}
				appendLine()
				appendLine("   Формула: S = I / J")
				appendLine("   Сечение: ${String.format("%.2f", calculatedCurrent)} / ${String.format("%.1f", currentDensity)} = ${String.format("%.2f", cableSection)} мм²")
				appendLine("   Стандартное сечение: ${String.format("%.2f", standardSection)} мм²")
				appendLine()
				appendLine("4. РАСЧЁТ ПОТЕРЬ НАПРЯЖЕНИЯ:")
				when (conductorMaterial.toInt()) {
					1 -> appendLine("   Удельное сопротивление меди: 0.0175 Ом·мм²/м")
					2 -> appendLine("   Удельное сопротивление алюминия: 0.0283 Ом·мм²/м")
				}
				appendLine("   Сопротивление линии: R = (ρ × L × 2) / S")
				appendLine("   Сопротивление: (${String.format("%.4f", resistivity)} × ${String.format("%.2f", cableLength)} × 2) / ${String.format("%.2f", standardSection)}")
				appendLine("   Сопротивление: ${String.format("%.4f", finalResistance)} Ом")
				appendLine()
				if (networkType.toInt() == 2) {
					appendLine("   Формула потерь (трёхфазная): ΔU = √3 × I × R")
					appendLine("   Потери: √3 × ${String.format("%.2f", calculatedCurrent)} × ${String.format("%.4f", finalResistance)}")
					appendLine("   Потери: ${String.format("%.2f", finalVoltageDrop)} В")
				} else {
					appendLine("   Формула потерь (однофазная): ΔU = 2 × I × R")
					appendLine("   Потери: 2 × ${String.format("%.2f", calculatedCurrent)} × ${String.format("%.4f", finalResistance)}")
					appendLine("   Потери: ${String.format("%.2f", finalVoltageDrop)} В")
				}
				appendLine("   Потери в процентах: (${String.format("%.2f", finalVoltageDrop)} / ${String.format("%.0f", voltage)}) × 100 = ${String.format("%.2f", finalVoltageDropPercent)}%")
				if (finalVoltageDropPercent > voltageDropPercent) {
					appendLine("   ⚠️ Потери превышают допустимые (${String.format("%.1f", voltageDropPercent)}%)")
					appendLine("   Увеличено сечение до ${String.format("%.2f", standardSection)} мм²")
				} else {
					appendLine("   ✓ Потери в пределах допустимых (${String.format("%.1f", voltageDropPercent)}%)")
				}
				appendLine()
			}
			if (calculationType.toInt() == 2 || calculationType.toInt() == 3) {
				val sectionNumber = if (calculationType.toInt() == 3) 5 else 3
				appendLine("${if (calculationType.toInt() == 3) "5" else "3"}. РАСЧЁТ НОМИНАЛА АВТОМАТА:")
				appendLine("   Формула: Номинал = I × 1.25 (запас 25%)")
				appendLine("   Номинал: ${String.format("%.2f", calculatedCurrent)} × 1.25 = ${String.format("%.2f", breakerCurrent)} А")
				appendLine("   Стандартный номинал: ${String.format("%.0f", standardBreakerCurrent)} А")
				appendLine("   Тип автомата: $breakerTypeName")
				when (breakerType.toInt()) {
					1 -> appendLine("   Характеристика: Срабатывание при 5-10 × номинальный ток (для бытовых нагрузок)")
					2 -> appendLine("   Характеристика: Срабатывание при 3-5 × номинальный ток (для слабоиндуктивных нагрузок)")
					3 -> appendLine("   Характеристика: Срабатывание при 10-20 × номинальный ток (для высоких пусковых токов)")
				}
				appendLine()
			}
			appendLine("${if (calculationType.toInt() == 3) "6" else if (calculationType.toInt() == 1) "5" else "4"}. РЕКОМЕНДАЦИИ:")
			if (calculationType.toInt() == 1 || calculationType.toInt() == 3) {
				appendLine("   • Рекомендуемое сечение: ${String.format("%.2f", standardSection)} мм²")
				when {
					standardSection <= 1.5 -> appendLine("   • Применение: освещение, маломощные потребители")
					standardSection == 2.5 -> appendLine("   • Применение: розетки в квартире, бытовая техника")
					standardSection <= 6.0 -> appendLine("   • Применение: электроплиты, водонагреватели, мощные потребители")
					standardSection <= 16.0 -> appendLine("   • Применение: ввод в дом, распределительные линии")
					else -> appendLine("   • Применение: мощные нагрузки, промышленное использование")
				}
				when (conductorMaterial.toInt()) {
					1 -> appendLine("   • Медный кабель предпочтительнее: безопаснее и долговечнее")
					2 -> appendLine("   • Алюминиевый кабель требует большего сечения, используется для наружной прокладки")
				}
				if (finalVoltageDropPercent > 3.0) {
					appendLine("   • ⚠️ Потери напряжения высокие - рассмотрите увеличение сечения или уменьшение длины линии")
				}
			}
			if (calculationType.toInt() == 2 || calculationType.toInt() == 3) {
				appendLine("   • Установите автоматический выключатель ${String.format("%.0f", standardBreakerCurrent)} А ($breakerTypeName)")
				appendLine("   • Номинал автомата не должен превышать допустимый ток для выбранного сечения кабеля")
				when (breakerType.toInt()) {
					1 -> appendLine("   • Тип C подходит для большинства бытовых нагрузок (розетки, освещение)")
					2 -> appendLine("   • Тип B используется для слабоиндуктивных нагрузок (лампы, обогреватели)")
					3 -> appendLine("   • Тип D необходим для нагрузок с высокими пусковыми токами (двигатели, компрессоры)")
				}
			}
		}
	}
	
}

