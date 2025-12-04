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
		
		// Calculate area based on input method
		val calculatedArea = when (inputMethod.toInt()) {
			1 -> { // Length × Width
				if (length <= 0 || width <= 0) throw ArithmeticException("Length and width must be positive")
				length * width
			}
			2 -> { // Area
				if (area <= 0) throw ArithmeticException("Area must be positive")
				area
			}
			else -> throw ArithmeticException("Invalid input method")
		}
		
		// Calculate volume
		val gravelVolume = when (inputMethod.toInt()) {
			3 -> { // Volume directly
				if (volume <= 0) throw ArithmeticException("Volume must be positive")
				volume
			}
			else -> {
				if (layerThickness <= 0) throw ArithmeticException("Layer thickness must be positive")
				calculatedArea * layerThickness
			}
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
		val airExchangeRate = inputs["air_exchange_rate"]
		
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
		val effectiveAirExchangeRate = airExchangeRate ?: defaultAirExchangeRate
		
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
	
}

