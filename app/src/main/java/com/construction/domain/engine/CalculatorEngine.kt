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
	 * Placeholder: volume calculations
	 */
	private fun calculateFoundation(inputs: Map<String, Double>): Map<String, Double> {
		val foundationLength = inputs["foundation_length"] ?: 0.0
		val foundationWidth = inputs["foundation_width"] ?: 0.0
		val foundationHeight = inputs["foundation_height"] ?: 0.0
		val rebarDiameter = inputs["rebar_diameter"] ?: 12.0
		
		val concreteVolume = foundationLength * foundationWidth * foundationHeight
		
		// Approximate rebar: 100 kg per m³ of concrete
		val rebarMass = concreteVolume * 100.0
		
		// Formwork area: perimeter * height
		val formworkArea = 2 * (foundationLength + foundationWidth) * foundationHeight
		
		return mapOf(
			"concrete_volume" to concreteVolume,
			"rebar_mass" to rebarMass,
			"formwork_area" to formworkArea
		)
	}
	
	/**
	 * Calculates concrete mix components.
	 * Formula: Component = Volume × Density × Ratio / Sum of ratios
	 * Water = Cement × Water-cement ratio
	 * Based on calc1.ru calculation method
	 */
	private fun calculateConcrete(inputs: Map<String, Double>): Map<String, Double> {
		val concreteVolume = inputs["concrete_volume"] ?: 0.0
		val cementRatio = inputs["cement_ratio"] ?: 1.0
		val sandRatio = inputs["sand_ratio"] ?: 2.5
		val gravelRatio = inputs["gravel_ratio"] ?: 4.5
		val waterCementRatio = inputs["water_cement_ratio"] ?: 0.5
		
		// Densities (kg/m³)
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
		val waterVolume = cementMass * waterCementRatio / 1000.0 // Convert to liters
		
		return mapOf(
			"cement_mass" to cementMass,
			"sand_mass" to sandMass,
			"gravel_mass" to gravelMass,
			"water_volume" to waterVolume
		)
	}
	
	/**
	 * Calculates roof area, material area, and weight.
	 * Placeholder: area with angle, then material with waste
	 */
	private fun calculateRoof(inputs: Map<String, Double>): Map<String, Double> {
		val roofLength = inputs["roof_length"] ?: 0.0
		val roofWidth = inputs["roof_width"] ?: 0.0
		val roofAngle = inputs["roof_angle"] ?: 30.0
		val materialWeight = inputs["material_weight"] ?: 5.0
		val wastePercent = inputs["waste_percent"] ?: 10.0
		
		// Calculate roof area considering angle (simplified)
		val angleRad = roofAngle * PI / 180.0
		val roofArea = roofLength * roofWidth / cos(angleRad)
		
		val materialArea = roofArea * (1 + wastePercent / 100.0)
		val totalWeight = roofArea * materialWeight
		
		return mapOf(
			"roof_area" to roofArea,
			"material_area" to materialArea,
			"total_weight" to totalWeight
		)
	}
	
	/**
	 * Calculates number of bricks/blocks needed.
	 * Placeholder: wall_volume / block_volume with waste
	 */
	private fun calculateBrickBlocks(inputs: Map<String, Double>): Map<String, Double> {
		val wallLength = inputs["wall_length"] ?: 0.0
		val wallHeight = inputs["wall_height"] ?: 0.0
		val wallThickness = inputs["wall_thickness"] ?: 0.4
		val blockLength = inputs["block_length"] ?: 20.0
		val blockWidth = inputs["block_width"] ?: 30.0
		val blockHeight = inputs["block_height"] ?: 20.0
		val wastePercent = inputs["waste_percent"] ?: 5.0
		
		val wallVolume = wallLength * wallHeight * wallThickness
		val blockVolume = (blockLength / 100.0) * (blockWidth / 100.0) * (blockHeight / 100.0)
		if (blockVolume <= 0) throw ArithmeticException("Block volume must be positive")
		val blockCount = kotlin.math.ceil((wallVolume / blockVolume) * (1 + wastePercent / 100.0)) // Rounded UP
		
		return mapOf(
			"block_count" to blockCount,
			"wall_volume" to wallVolume
		)
	}
	
	/**
	 * Calculates stairs parameters: steps count, flight length, angle.
	 * Placeholder: based on floor height and step dimensions
	 */
	private fun calculateStairs(inputs: Map<String, Double>): Map<String, Double> {
		val floorHeight = inputs["floor_height"] ?: 0.0
		val stepHeight = inputs["step_height"] ?: 17.0
		val stepWidth = inputs["step_width"] ?: 30.0
		
		if (stepHeight <= 0) throw ArithmeticException("Step height must be positive")
		if (stepWidth <= 0) throw ArithmeticException("Step width must be positive")
		val stepsCount = (floorHeight * 100 / stepHeight).coerceAtLeast(1.0)
		val flightLength = (stepsCount * stepWidth) / 100.0 // Convert cm to m
		val angle = atan(stepHeight / stepWidth) * 180.0 / PI
		
		return mapOf(
			"steps_count" to stepsCount,
			"flight_length" to flightLength,
			"angle" to angle
		)
	}
	
	/**
	 * Calculates gravel volume and mass needed.
	 * Placeholder: area * thickness * density * compaction
	 */
	private fun calculateGravel(inputs: Map<String, Double>): Map<String, Double> {
		val area = inputs["area"] ?: 0.0
		val layerThickness = inputs["layer_thickness"] ?: 20.0
		val gravelDensity = inputs["gravel_density"] ?: 1500.0
		val compactionCoefficient = inputs["compaction_coefficient"] ?: 1.3
		
		val thicknessMeters = layerThickness / 100.0 // Convert cm to m
		val gravelVolume = area * thicknessMeters * compactionCoefficient
		val gravelMass = gravelVolume * gravelDensity
		
		return mapOf(
			"gravel_volume" to gravelVolume,
			"gravel_mass" to gravelMass
		)
	}
	
	// Engineering Systems Calculations
	
	/**
	 * Calculates ventilation airflow rate.
	 * Placeholder: volume * air exchange rate
	 */
	private fun calculateVentilation(inputs: Map<String, Double>): Map<String, Double> {
		val roomLength = inputs["room_length"] ?: 0.0
		val roomWidth = inputs["room_width"] ?: 0.0
		val roomHeight = inputs["room_height"] ?: 0.0
		val airExchangeRate = inputs["air_exchange_rate"] ?: 1.0
		
		val roomVolume = roomLength * roomWidth * roomHeight
		val airflowRate = roomVolume * airExchangeRate
		
		return mapOf(
			"airflow_rate" to airflowRate,
			"room_volume" to roomVolume
		)
	}
	
	/**
	 * Calculates heated floor power and consumption.
	 * Placeholder: area * power_per_sqm, then consumption
	 */
	private fun calculateHeatedFloor(inputs: Map<String, Double>): Map<String, Double> {
		val floorArea = inputs["floor_area"] ?: 0.0
		val powerPerSqm = inputs["power_per_sqm"] ?: 150.0
		val usageHours = inputs["usage_hours"] ?: 8.0
		val electricityPrice = inputs["electricity_price"] ?: 5.0
		
		val totalPower = (floorArea * powerPerSqm) / 1000.0 // Convert to kW
		val dailyConsumption = totalPower * usageHours
		val monthlyCost = dailyConsumption * 30.0 * electricityPrice
		
		return mapOf(
			"total_power" to totalPower,
			"daily_consumption" to dailyConsumption,
			"monthly_cost" to monthlyCost
		)
	}
	
	/**
	 * Calculates water pipe flow rate and pressure loss.
	 * Placeholder: simplified hydraulic calculations
	 */
	private fun calculateWaterPipes(inputs: Map<String, Double>): Map<String, Double> {
		val pipeDiameter = inputs["pipe_diameter"] ?: 0.0
		val pipeLength = inputs["pipe_length"] ?: 0.0
		val flowVelocity = inputs["flow_velocity"] ?: 1.5
		val roughness = inputs["roughness"] ?: 0.1
		
		val diameterMeters = pipeDiameter / 1000.0 // Convert mm to m
		val pipeArea = PI * (diameterMeters / 2.0).pow(2)
		val flowRate = pipeArea * flowVelocity * 1000.0 * 60.0 // Convert to l/min
		
		// Simplified pressure loss (Darcy-Weisbach approximation)
		val pressureLoss = (0.02 * pipeLength * flowVelocity.pow(2)) / (2 * diameterMeters) * 1000.0
		
		return mapOf(
			"flow_rate" to flowRate,
			"pressure_loss" to pressureLoss,
			"pipe_area" to pipeArea
		)
	}
	
	// Metal & Electricity Calculations
	
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
				Pair(longBars, longLength, transBars, transLength)
			}
			2 -> {
				// Slab: bars along length and width
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				Pair(longBars, longLength, transBars, transLength)
			}
			3 -> {
				// Wall: vertical along height, horizontal along length
				val vertBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val vertLength = vertBars * height
				val horBars = kotlin.math.ceil(height / meshStepMeters + 1.0) * layersCount
				val horLength = horBars * length
				Pair(vertBars, vertLength, horBars, horLength)
			}
			4 -> {
				// Column: longitudinal along height, transverse (hoops) around perimeter
				val longBars = 4.0 * layersCount // Standard 4 bars per layer
				val longLength = longBars * height
				val hoopStep = meshStepMeters
				val hoopCount = kotlin.math.ceil(height / hoopStep + 1.0)
				val hoopPerimeter = 2 * (length + width)
				val transLength = hoopCount * hoopPerimeter
				Pair(longBars, longLength, hoopCount, transLength)
			}
			else -> {
				// Default: same as foundation
				val longBars = kotlin.math.ceil(length / meshStepMeters + 1.0) * layersCount
				val longLength = longBars * width
				val transBars = kotlin.math.ceil(width / meshStepMeters + 1.0) * layersCount
				val transLength = transBars * length
				Pair(longBars, longLength, transBars, transLength)
			}
		}
		
		// Add overlap if specified (convert from cm to m)
		val overlapMeters = overlap / 100.0
		val totalBars = longitudinalBars + transverseBars
		val overlapLength = if (overlapMeters > 0) totalBars * overlapMeters else 0.0
		
		// Calculate total length
		val totalLength = longitudinalLength + transverseLength + overlapLength
		
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
	 * Calculates cable section, current, and voltage drop.
	 * Placeholder: simplified electrical calculations
	 */
	private fun calculateCableSection(inputs: Map<String, Double>): Map<String, Double> {
		val power = inputs["power"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val cableLength = inputs["cable_length"] ?: 0.0
		val powerFactor = inputs["power_factor"] ?: 0.9
		val voltageDropPercent = inputs["voltage_drop_percent"] ?: 5.0
		
		if (voltage <= 0) throw ArithmeticException("Voltage must be positive")
		if (powerFactor <= 0) throw ArithmeticException("Power factor must be positive")
		val powerWatts = power * 1000.0
		val current = powerWatts / (voltage * powerFactor)
		
		// Simplified cable section calculation (1.5 mm² per 10A for short distances)
		val cableSection = (current / 10.0) * 1.5
		
		// Simplified voltage drop (resistance * current)
		val resistance = if (cableSection > 0) {
			(0.0175 * cableLength * 2) / cableSection // Copper resistivity
		} else {
			0.0
		}
		val voltageDrop = current * resistance
		
		return mapOf(
			"current" to current,
			"cable_section" to cableSection,
			"voltage_drop" to voltageDrop
		)
	}
	
	/**
	 * Calculates electrical parameters: current, breaker rating, cable section.
	 * Placeholder: simplified electrical calculations
	 */
	private fun calculateElectrical(inputs: Map<String, Double>): Map<String, Double> {
		val totalPower = inputs["total_power"] ?: 0.0
		val voltage = inputs["voltage"] ?: 220.0
		val phaseCount = inputs["phase_count"] ?: 1.0
		val cableLength = inputs["cable_length"] ?: 0.0
		val safetyFactor = inputs["safety_factor"] ?: 1.25
		
		val powerWatts = totalPower * 1000.0
		val current = if (phaseCount == 3.0) {
			powerWatts / (voltage * sqrt(3.0))
		} else {
			powerWatts / voltage
		}
		
		val breakerCurrent = current * safetyFactor
		
		// Simplified cable section (1.5 mm² per 10A)
		val cableSection = (current / 10.0) * 1.5
		
		return mapOf(
			"current" to current,
			"breaker_current" to breakerCurrent,
			"cable_section" to cableSection
		)
	}
	
}

