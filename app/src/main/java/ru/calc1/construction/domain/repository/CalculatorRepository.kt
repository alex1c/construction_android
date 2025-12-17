package ru.calc1.construction.domain.repository

import ru.calc1.construction.domain.model.AccessLevel
import ru.calc1.construction.domain.model.CalculatorCategory
import ru.calc1.construction.domain.model.CalculatorDefinition
import ru.calc1.construction.domain.model.InputFieldDefinition
import ru.calc1.construction.domain.model.InputFieldType
import ru.calc1.construction.domain.model.ResultFieldDefinition
import ru.calc1.construction.domain.model.UsageExample

/**
 * Static in-memory repository for construction calculators.
 * 
 * This repository provides access to all calculator definitions and categories.
 * It acts as a single source of truth for calculator metadata.
 * 
 * Architecture:
 * - Uses object singleton pattern (no instance needed)
 * - All data is defined in-memory (no database required)
 * - Each calculator is created via factory methods (create*Calculator)
 * 
 * Calculator Structure:
 * Each calculator contains:
 * - Input fields: Definition of all required input parameters
 * - Result fields: Definition of all calculated output values
 * - Usage examples: Practical examples showing typical use cases
 * - Help text: Optional detailed instructions
 * 
 * Data Source:
 * All calculator definitions are based on calc1.ru website specifications.
 * Formulas and parameters are aligned with industry standards (ГОСТ, СНиП).
 * 
 * Future Enhancements:
 * - Could be migrated to database for dynamic updates
 * - Could support remote configuration
 * - Could support user-defined calculators
 */
object CalculatorRepository {
	
	/**
	 * Category identifiers for organizing calculators.
	 * Categories are used for navigation and filtering.
	 */
	// Category IDs
	private const val CATEGORY_FINISHING = "finishing_interior"
	private const val CATEGORY_STRUCTURES = "structures_concrete"
	private const val CATEGORY_ENGINEERING = "engineering_systems"
	private const val CATEGORY_METAL_ELECTRICITY = "metal_electricity"
	
	/**
	 * Returns all available calculator categories.
	 * 
	 * Categories are used to organize calculators into logical groups:
	 * - Finishing & Interior: Materials for interior decoration (FREE)
	 * - Structures & Concrete: Structural elements and concrete work (PREMIUM)
	 * - Engineering Systems: HVAC, plumbing, electrical systems (PREMIUM)
	 * - Metal & Electricity: Metal structures and electrical calculations (PREMIUM)
	 * 
	 * Access Control:
	 * - "Отделка и интерьер" is always FREE
	 * - All other categories are PREMIUM-only
	 * 
	 * TODO: Enable Premium gating after RuStore billing launch
	 * 
	 * @return List of all calculator categories with descriptions and access levels
	 */
	fun getCategories(): List<CalculatorCategory> {
		return listOf(
			CalculatorCategory(
				id = CATEGORY_FINISHING,
				name = "Отделка и интерьер",
				description = "Калькуляторы для расчёта материалов для внутренней отделки помещений",
				accessLevel = AccessLevel.FREE // Always free
			),
			CalculatorCategory(
				id = CATEGORY_STRUCTURES,
				name = "Конструкции и бетон",
				description = "Калькуляторы для расчёта материалов для строительных конструкций и бетонных работ",
				accessLevel = AccessLevel.PREMIUM
			),
			CalculatorCategory(
				id = CATEGORY_ENGINEERING,
				name = "Инженерные системы",
				description = "Калькуляторы для расчёта параметров инженерных систем",
				accessLevel = AccessLevel.PREMIUM
			),
			CalculatorCategory(
				id = CATEGORY_METAL_ELECTRICITY,
				name = "Металл и электрика",
				description = "Калькуляторы для расчёта металлических конструкций и электрических систем",
				accessLevel = AccessLevel.PREMIUM
			)
		)
	}
	
	/**
	 * Returns all available calculators.
	 * 
	 * This method creates and returns all 21 calculators in the application.
	 * Each calculator is created via a dedicated factory method that defines:
	 * - Input fields with types, units, hints, and default values
	 * - Result fields with labels and units
	 * - Usage examples for common scenarios
	 * - Optional help text
	 * 
	 * Calculator Organization:
	 * - Finishing & Interior: 9 calculators (wallpaper, paint, tile, etc.)
	 * - Structures & Concrete: 6 calculators (foundation, concrete, roof, etc.)
	 * - Engineering Systems: 3 calculators (ventilation, heated floor, pipes)
	 * - Metal & Electricity: 3 calculators (rebar, cable, electrical)
	 * 
	 * Total: 21 calculators
	 * 
	 * @return List of all calculator definitions
	 */
	fun getCalculators(): List<CalculatorDefinition> {
		return listOf(
			// Finishing & Interior (9 calculators)
			createWallpaperCalculator(),      // Расчёт количества обоев
			createPaintCalculator(),          // Расчёт количества краски
			createTileAdhesiveCalculator(),  // Расчёт плиточного клея
			createPuttyCalculator(),         // Расчёт шпатлёвки
			createPrimerCalculator(),        // Расчёт грунтовки
			createPlasterCalculator(),       // Расчёт штукатурки
			createWallAreaCalculator(),      // Расчёт площади стен
			createTileCalculator(),          // Расчёт количества плитки
			createLaminateCalculator(),      // Расчёт ламината
			
			// Structures & Concrete (6 calculators)
			createFoundationCalculator(),    // Расчёт материалов для фундамента
			createConcreteCalculator(),      // Расчёт состава бетона
			createRoofCalculator(),          // Расчёт кровли
			createBrickBlocksCalculator(),  // Расчёт кирпича и блоков
			createStairsCalculator(),        // Расчёт лестниц
			createGravelCalculator(),        // Расчёт щебня
			
			// Engineering Systems (3 calculators)
			createVentilationCalculator(),   // Расчёт вентиляции
			createHeatedFloorCalculator(),   // Расчёт тёплого пола
			createWaterPipesCalculator(),    // Расчёт водопроводных труб
			
			// Metal & Electricity (3 calculators)
			createRebarCalculator(),         // Расчёт арматуры
			createCableSectionCalculator(),  // Расчёт сечения кабеля
			createElectricalCalculator()     // Расчёт электрики
		)
	}
	
	/**
	 * Returns all calculators belonging to the specified category.
	 * 
	 * Used for filtering calculators by category on CategoryScreen.
	 * 
	 * @param categoryId ID of the category (e.g., "finishing_interior")
	 * @return List of calculators in the specified category
	 */
	fun getCalculatorsByCategory(categoryId: String): List<CalculatorDefinition> {
		return getCalculators().filter { it.categoryId == categoryId }
	}
	
	/**
	 * Returns a calculator by its unique ID, or null if not found.
	 * 
	 * Used to load calculator definition when navigating to CalculatorScreen.
	 * 
	 * @param id Unique calculator ID (e.g., "wallpaper", "concrete", "foundation")
	 * @return Calculator definition if found, null otherwise
	 */
	fun getCalculatorById(id: String): CalculatorDefinition? {
		return getCalculators().firstOrNull { it.id == id }
	}
	
	// Finishing & Interior Calculators
	
	private fun createWallpaperCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "wallpaper",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор обоев",
			shortDescription = "Расчёт количества рулонов обоев для оклейки комнаты",
			inputFields = listOf(
				InputFieldDefinition(
					id = "room_length",
					label = "Длина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_width",
					label = "Ширина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_height",
					label = "Высота стен",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота стен в метрах"
				),
				InputFieldDefinition(
					id = "openings_area",
					label = "Площадь проёмов",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.0,
					hint = "Общая площадь окон и дверей"
				),
				InputFieldDefinition(
					id = "roll_width",
					label = "Ширина рулона",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.53,
					hint = "Ширина рулона обоев (стандарт: 0.53 м)"
				),
				InputFieldDefinition(
					id = "roll_length",
					label = "Длина рулона",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 10.05,
					hint = "Длина рулона обоев (стандарт: 10.05 м)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас обоев",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на подгонку рисунка (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "rolls_count",
					label = "Количество рулонов",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "wallpaper_small_room",
					title = "Спальня 4×3 м",
					description = "Расчёт количества рулонов обоев для спальни 4×3 м с высотой стен 2,7 м.",
					inputSummary = "Длина 4 м, ширина 3 м, высота 2,7 м, проёмы 3,6 м², рулон 0,53×10,05 м, запас 10%",
					resultSummary = "Получается 8 рулонов стандартных обоев"
				),
				UsageExample(
					id = "wallpaper_large_room",
					title = "Гостиная 6×5 м",
					description = "Расчёт обоев для просторной гостиной с высокими потолками.",
					inputSummary = "Длина 6 м, ширина 5 м, высота 3 м, проёмы 11,1 м², рулон 0,53×10,05 м, запас 10%",
					resultSummary = "Потребуется 15–16 рулонов с учётом запаса"
				)
			)
		)
	}
	
	private fun createPaintCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "paint",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор краски",
			shortDescription = "Расчёт количества краски для стен",
			inputFields = listOf(
				InputFieldDefinition(
					id = "room_length",
					label = "Длина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_width",
					label = "Ширина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_height",
					label = "Высота стен",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота стен в метрах"
				),
				InputFieldDefinition(
					id = "openings_area",
					label = "Площадь дверей и окон",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.0,
					hint = "Общая площадь окон и дверей"
				),
				InputFieldDefinition(
					id = "coats_count",
					label = "Количество слоёв",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 2.0,
					hint = "Количество слоёв краски"
				),
				InputFieldDefinition(
					id = "paint_consumption",
					label = "Норма расхода",
					unit = "л/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 0.12,
					hint = "Расход краски на квадратный метр (обычно 0.1-0.15 л/м²)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на потери (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "paint_volume",
					label = "Количество краски",
					unit = "л"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "paint_room",
					title = "Комната 4×3 м",
					description = "Расчёт краски для покраски стен в комнате 4×3 м с высотой потолков 2,7 м.",
					inputSummary = "Длина 4 м, ширина 3 м, высота 2,7 м, проёмы 4,5 м², 2 слоя, расход 0,12 л/м², запас 10%",
					resultSummary = "Потребуется 8,8 л краски (4 банки по 2,5 л)"
				),
				UsageExample(
					id = "paint_large_room",
					title = "Большая гостиная 6×5 м",
					description = "Расчёт краски для просторной гостиной с большими окнами.",
					inputSummary = "Длина 6 м, ширина 5 м, высота 3 м, проёмы 12 м², 2 слоя, расход 0,15 л/м², запас 15%",
					resultSummary = "Потребуется 18,6 л краски (8 банок по 2,5 л)"
				)
			)
		)
	}
	
	private fun createTileAdhesiveCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "tile_adhesive",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор плиточного клея",
			shortDescription = "Расчёт количества плиточного клея для укладки плитки",
			inputFields = listOf(
				InputFieldDefinition(
					id = "floor_area",
					label = "Площадь пола",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь пола для укладки плитки"
				),
				InputFieldDefinition(
					id = "openings_area",
					label = "Площадь дверей и окон",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.0,
					hint = "Общая площадь проёмов"
				),
				InputFieldDefinition(
					id = "tile_size",
					label = "Размер плитки",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.09,
					hint = "Площадь одной плитки (например, 0.09 для 30×30 см)"
				),
				InputFieldDefinition(
					id = "seam_width",
					label = "Ширина шва",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 2.0,
					hint = "Ширина шва между плитками"
				),
				InputFieldDefinition(
					id = "adhesive_consumption",
					label = "Норма расхода",
					unit = "кг/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 4.0,
					hint = "Расход клея на квадратный метр (обычно 3-6 кг/м²)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на потери (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "adhesive_mass",
					label = "Количество клея",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "tile_adhesive_bathroom",
					title = "Ванная комната 3×2 м",
					description = "Расчёт клея для укладки керамической плитки 30×30 см в ванной комнате.",
					inputSummary = "Площадь пола 6 м², проёмы 0,5 м², плитка 0,09 м², шов 2 мм, расход 4 кг/м², запас 10%",
					resultSummary = "Потребуется 24,2 кг плиточного клея (1 упаковка 25 кг)"
				),
				UsageExample(
					id = "tile_adhesive_kitchen",
					title = "Кухня 4×3 м",
					description = "Расчёт клея для укладки керамогранита 60×60 см на кухне.",
					inputSummary = "Площадь пола 12 м², проёмы 1,2 м², плитка 0,36 м², шов 3 мм, расход 5 кг/м², запас 15%",
					resultSummary = "Потребуется 62,1 кг клея (3 упаковки по 25 кг)"
				)
			)
		)
	}
	
	private fun createPuttyCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "putty",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор шпатлёвки",
			shortDescription = "Расчёт количества шпатлёвки для выравнивания стен",
			inputFields = listOf(
				InputFieldDefinition(
					id = "wall_area",
					label = "Площадь стен",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь стен для шпатлевания"
				),
				InputFieldDefinition(
					id = "putty_consumption",
					label = "Расход шпатлёвки",
					unit = "кг/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 1.2,
					hint = "Расход шпатлёвки на квадратный метр (обычно 1-1.5 кг/м²)"
				),
				InputFieldDefinition(
					id = "layer_thickness",
					label = "Толщина слоя",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 2.0,
					hint = "Толщина слоя шпатлёвки"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на потери (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "putty_mass",
					label = "Количество шпатлёвки",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "putty_room",
					title = "Шпатлёвка комнаты 25 м²",
					description = "Расчёт шпатлёвки для выравнивания стен в комнате перед покраской.",
					inputSummary = "Площадь 25 м², расход 1,2 кг/м², толщина слоя 2 мм",
					resultSummary = "Потребуется 30 кг шпатлёвки"
				),
				UsageExample(
					id = "putty_apartment",
					title = "Шпатлёвка всей квартиры",
					description = "Расчёт шпатлёвки для подготовки всех стен в квартире.",
					inputSummary = "Площадь 100 м², расход 1,2 кг/м², толщина слоя 2 мм",
					resultSummary = "Потребуется 120 кг шпатлёвки (примерно 6 мешков по 20 кг)"
				)
			)
		)
	}
	
	private fun createPrimerCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "primer",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор грунтовки",
			shortDescription = "Расчёт количества грунтовки для подготовки поверхности",
			inputFields = listOf(
				InputFieldDefinition(
					id = "surface_area",
					label = "Площадь поверхности",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь поверхности для грунтования"
				),
				InputFieldDefinition(
					id = "primer_consumption",
					label = "Расход грунтовки",
					unit = "л/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 0.2,
					hint = "Расход грунтовки на квадратный метр (обычно 0.1-0.3 л/м²)"
				),
				InputFieldDefinition(
					id = "coats_count",
					label = "Количество слоёв",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "Количество слоёв грунтовки"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на потери (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "primer_volume",
					label = "Количество грунтовки",
					unit = "л"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "primer_walls",
					title = "Грунтовка стен 30 м²",
					description = "Расчёт грунтовки для подготовки стен перед покраской или оклейкой обоями.",
					inputSummary = "Площадь 30 м², расход 0,2 л/м², 1 слой",
					resultSummary = "Потребуется 6 литров грунтовки"
				),
				UsageExample(
					id = "primer_ceiling",
					title = "Грунтовка потолка 20 м²",
					description = "Расчёт грунтовки для подготовки потолка перед покраской.",
					inputSummary = "Площадь 20 м², расход 0,2 л/м², 1 слой",
					resultSummary = "Потребуется 4 литра грунтовки"
				)
			)
		)
	}
	
	private fun createPlasterCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "plaster",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор штукатурки",
			shortDescription = "Расчёт количества штукатурки для стен",
			inputFields = listOf(
				InputFieldDefinition(
					id = "wall_area",
					label = "Площадь стен",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь стен для оштукатуривания"
				),
				InputFieldDefinition(
					id = "plaster_consumption",
					label = "Расход штукатурки",
					unit = "кг/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 8.5,
					hint = "Расход штукатурки на квадратный метр (обычно 8-10 кг/м² на 10 мм)"
				),
				InputFieldDefinition(
					id = "layer_thickness",
					label = "Толщина слоя",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 10.0,
					hint = "Толщина слоя штукатурки"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на потери (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "plaster_mass",
					label = "Количество штукатурки",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "plaster_room",
					title = "Штукатурка комнаты 30 м²",
					description = "Расчёт штукатурки для выравнивания стен в комнате.",
					inputSummary = "Площадь 30 м², расход 8,5 кг/м², толщина слоя 10 мм",
					resultSummary = "Потребуется 255 кг штукатурки (примерно 13 мешков по 20 кг)"
				),
				UsageExample(
					id = "plaster_thick_layer",
					title = "Штукатурка с толстым слоем",
					description = "Расчёт штукатурки для сильно неровных стен с толстым слоем.",
					inputSummary = "Площадь 25 м², расход 8,5 кг/м², толщина слоя 20 мм",
					resultSummary = "Потребуется 425 кг штукатурки"
				)
			)
		)
	}
	
	private fun createWallAreaCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "wall_area",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор площади стен",
			shortDescription = "Расчёт площади стен с учётом проёмов и запаса",
			inputFields = listOf(
				InputFieldDefinition(
					id = "room_length",
					label = "Длина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_width",
					label = "Ширина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_height",
					label = "Высота потолка",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота потолка в метрах"
				),
				InputFieldDefinition(
					id = "openings_area",
					label = "Площадь проёмов",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.0,
					hint = "Общая площадь окон и дверей"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса материала"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "total_area",
					label = "Общая площадь",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "area_with_waste",
					label = "Площадь с запасом",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "wall_area_room",
					title = "Комната 4×3 м с окном",
					description = "Расчёт площади стен в комнате с учётом оконного проёма.",
					inputSummary = "Длина 4 м, ширина 3 м, высота 2,7 м, проёмы 2 м², запас 10%",
					resultSummary = "Общая площадь 35,8 м², с запасом 39,4 м²"
				),
				UsageExample(
					id = "wall_area_apartment",
					title = "Квартира-студия 30 м²",
					description = "Расчёт площади всех стен в квартире-студии для планирования отделки.",
					inputSummary = "Длина 6 м, ширина 5 м, высота 2,7 м, проёмы 5 м², запас 10%",
					resultSummary = "Общая площадь 54,4 м², с запасом 59,8 м²"
				)
			)
		)
	}
	
	private fun createTileCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "tile",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор плитки",
			shortDescription = "Расчёт количества плитки для пола и стен",
			inputFields = listOf(
				InputFieldDefinition(
					id = "floor_area",
					label = "Площадь пола",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь пола для укладки плитки"
				),
				InputFieldDefinition(
					id = "openings_area",
					label = "Площадь дверей и окон",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 0.0,
					hint = "Общая площадь проёмов"
				),
				InputFieldDefinition(
					id = "tile_length",
					label = "Длина плитки",
					unit = "см",
					type = InputFieldType.LENGTH,
					hint = "Длина одной плитки"
				),
				InputFieldDefinition(
					id = "tile_width",
					label = "Ширина плитки",
					unit = "см",
					type = InputFieldType.LENGTH,
					hint = "Ширина одной плитки"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса на подрезку (рекомендуется 10-15%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "tile_count",
					label = "Количество плитки",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "tile_bathroom",
					title = "Ванная комната 3×2 м",
					description = "Расчёт количества керамической плитки 30×30 см для пола в ванной комнате.",
					inputSummary = "Площадь пола 6 м², проёмы 0,5 м², плитка 30×30 см, запас 10%",
					resultSummary = "Потребуется 73 плитки"
				),
				UsageExample(
					id = "tile_kitchen",
					title = "Кухня 4×3 м",
					description = "Расчёт плитки для пола на кухне.",
					inputSummary = "Площадь пола 12 м², проёмы 1,2 м², плитка 60×60 см, запас 15%",
					resultSummary = "Потребуется 32 плитки"
				)
			)
		)
	}
	
	private fun createLaminateCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "laminate",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор ламината",
			shortDescription = "Расчёт количества ламината для пола",
			inputFields = listOf(
				InputFieldDefinition(
					id = "room_length",
					label = "Длина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "room_width",
					label = "Ширина комнаты",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина комнаты в метрах"
				),
				InputFieldDefinition(
					id = "laminate_length",
					label = "Длина доски",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 1.3,
					hint = "Длина одной доски ламината"
				),
				InputFieldDefinition(
					id = "laminate_width",
					label = "Ширина доски",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.2,
					hint = "Ширина одной доски ламината"
				),
				InputFieldDefinition(
					id = "pack_count",
					label = "Досок в упаковке",
					unit = "шт",
					type = InputFieldType.INTEGER,
					defaultValue = 8.0,
					hint = "Количество досок в одной упаковке"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 5.0,
					hint = "Процент запаса на подрезку"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "pack_count",
					label = "Количество упаковок",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "total_area",
					label = "Общая площадь",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "laminate_room",
					title = "Ламинат в комнате 12 м²",
					description = "Расчёт ламината для укладки в комнате стандартного размера.",
					inputSummary = "Длина 4 м, ширина 3 м, доска 1,3×0,2 м, 8 досок в упаковке, запас 5%",
					resultSummary = "Потребуется 2 упаковки ламината"
				),
				UsageExample(
					id = "laminate_apartment",
					title = "Ламинат во всей квартире",
					description = "Расчёт ламината для укладки во всех комнатах квартиры.",
					inputSummary = "Длина 6 м, ширина 5 м, доска 1,3×0,2 м, 8 досок в упаковке, запас 5%",
					resultSummary = "Потребуется 3 упаковки ламината"
				)
			)
		)
	}
	
	// Structures & Concrete Calculators
	
	private fun createFoundationCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "foundation",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор фундамента",
			shortDescription = "Расчёт материалов для фундамента: бетон, арматура, опалубка",
			inputFields = listOf(
				InputFieldDefinition(
					id = "foundation_type",
					label = "Тип фундамента",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип фундамента",
					options = listOf(
						Pair(1.0, "Ленточный"),
						Pair(2.0, "Плитный"),
						Pair(3.0, "Столбчатый")
					)
				),
				InputFieldDefinition(
					id = "length",
					label = "Длина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина фундамента (для столбчатого - длина одного столба)"
				),
				InputFieldDefinition(
					id = "width",
					label = "Ширина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина фундамента (для столбчатого - ширина одного столба)"
				),
				InputFieldDefinition(
					id = "height",
					label = "Высота",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота фундамента (для плитного - толщина плиты)"
				),
				InputFieldDefinition(
					id = "wall_thickness",
					label = "Толщина стены",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.3,
					hint = "Толщина стены (только для ленточного фундамента)"
				),
				InputFieldDefinition(
					id = "pillars_count",
					label = "Количество столбов",
					unit = "шт",
					type = InputFieldType.INTEGER,
					defaultValue = 9.0,
					hint = "Количество столбов (только для столбчатого фундамента)"
				),
				InputFieldDefinition(
					id = "concrete_grade",
					label = "Марка бетона",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 200.0,
					hint = "Выберите марку бетона",
					options = listOf(
						Pair(200.0, "М200"),
						Pair(250.0, "М250"),
						Pair(300.0, "М300")
					)
				),
				InputFieldDefinition(
					id = "rebar_diameter",
					label = "Диаметр арматуры",
					unit = "мм",
					type = InputFieldType.DROPDOWN,
					defaultValue = 12.0,
					hint = "Выберите диаметр арматуры",
					options = listOf(
						Pair(8.0, "8 мм"),
						Pair(10.0, "10 мм"),
						Pair(12.0, "12 мм"),
						Pair(14.0, "14 мм"),
						Pair(16.0, "16 мм"),
						Pair(18.0, "18 мм"),
						Pair(20.0, "20 мм")
					)
				),
				InputFieldDefinition(
					id = "mesh_step",
					label = "Шаг арматуры",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 20.0,
					hint = "Расстояние между арматурными стержнями"
				),
				InputFieldDefinition(
					id = "layers_count",
					label = "Количество слоёв арматуры",
					unit = "шт",
					type = InputFieldType.INTEGER,
					defaultValue = 2.0,
					hint = "Количество слоёв арматуры (обычно 2)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса материалов"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "concrete_volume",
					label = "Объём бетона",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "cement_mass",
					label = "Цемент",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "cement_bags",
					label = "Цемент",
					unit = "мешков"
				),
				ResultFieldDefinition(
					id = "sand_mass",
					label = "Песок",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "gravel_mass",
					label = "Щебень",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "rebar_mass",
					label = "Арматура",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "formwork_area",
					label = "Площадь опалубки",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "foundation_strip_house",
					title = "Ленточный фундамент для дома 6×8 м",
					description = "Ленточный фундамент для одноэтажного дома, глубина 1.5 м",
					inputSummary = "Тип: 1, Длина: 8 м, Ширина: 6 м, Высота: 1.5 м, Толщина стены: 0.4 м, Марка: М200, Арматура: 12 мм, шаг 20 см, 2 слоя, Запас: 10%",
					resultSummary = "Бетон: 16.37 м³, Цемент: 92 мешка, Арматура: 1,640 кг"
				),
				UsageExample(
					id = "foundation_slab_house",
					title = "Плитный фундамент для дома 10×10 м",
					description = "Монолитная плита для двухэтажного дома, толщина 30 см",
					inputSummary = "Тип: 2, Длина: 10 м, Ширина: 10 м, Высота: 0.3 м, Марка: М250, Арматура: 14 мм, шаг 20 см, 2 слоя, Запас: 10%",
					resultSummary = "Бетон: 33 м³, Цемент: 218 мешков, Арматура: 3,300 кг"
				),
				UsageExample(
					id = "foundation_column_bath",
					title = "Столбчатый фундамент для бани 4×4 м",
					description = "Столбчатый фундамент для бани, 9 столбов",
					inputSummary = "Тип: 3, Длина: 0.4 м, Ширина: 0.4 м, Высота: 1.2 м, Количество: 9, Марка: М200, Арматура: 10 мм, шаг 15 см, 1 слой, Запас: 10%",
					resultSummary = "Бетон: 1.9 м³, Цемент: 11 мешков, Арматура: 190 кг"
				)
			)
		)
	}
	
	private fun createConcreteCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "concrete",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор бетона",
			shortDescription = "Расчёт количества цемента, песка, щебня и воды",
			inputFields = listOf(
				InputFieldDefinition(
					id = "concrete_volume",
					label = "Объём бетона",
					unit = "м³",
					type = InputFieldType.VOLUME,
					hint = "Требуемый объём бетона"
				),
				InputFieldDefinition(
					id = "concrete_grade",
					label = "Марка бетона",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 200.0,
					hint = "Выберите марку бетона",
					options = listOf(
						Pair(100.0, "М100"),
						Pair(150.0, "М150"),
						Pair(200.0, "М200"),
						Pair(250.0, "М250"),
						Pair(300.0, "М300"),
						Pair(400.0, "М400")
					)
				),
				InputFieldDefinition(
					id = "cement_ratio",
					label = "Пропорция цемента",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 1.0,
					hint = "Пропорция цемента (обычно 1). М100:1, М150:1, М200:1, М250:1, М300:1, М400:1"
				),
				InputFieldDefinition(
					id = "sand_ratio",
					label = "Пропорция песка",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 2.5,
					hint = "Пропорция песка. М100:4, М150:3, М200:2.5, М250:2, М300:1.5, М400:1"
				),
				InputFieldDefinition(
					id = "gravel_ratio",
					label = "Пропорция щебня",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 4.5,
					hint = "Пропорция щебня. М100:6, М150:5, М200:4.5, М250:4, М300:3, М400:2"
				),
				InputFieldDefinition(
					id = "water_cement_ratio",
					label = "Водоцементное соотношение",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 0.5,
					hint = "Отношение воды к цементу (обычно 0.4-0.7)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "cement_mass",
					label = "Цемент",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "cement_bags",
					label = "Цемент",
					unit = "мешков"
				),
				ResultFieldDefinition(
					id = "sand_mass",
					label = "Песок",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "gravel_mass",
					label = "Щебень",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "water_volume",
					label = "Вода",
					unit = "л"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "concrete_foundation",
					title = "Фундамент 10 м³ М200",
					description = "Фундамент для дома, бетон марки М200, пропорции 1:2.5:4.5",
					inputSummary = "Объём: 10 м³, Марка: М200, Пропорции: 1:2.5:4.5 (цемент:песок:щебень), В/Ц: 0.5",
					resultSummary = "Цемент: 1750 кг (35 мешков), Песок: 5000 кг, Щебень: 7875 кг, Вода: 875 л"
				),
				UsageExample(
					id = "concrete_floor",
					title = "Стяжка пола 5 м³ М150",
					description = "Стяжка пола, бетон марки М150, пропорции 1:3:5",
					inputSummary = "Объём: 5 м³, Марка: М150, Пропорции: 1:3:5 (цемент:песок:щебень), В/Ц: 0.6",
					resultSummary = "Цемент: 778 кг (16 мешков), Песок: 2667 кг, Щебень: 3889 кг, Вода: 467 л"
				),
				UsageExample(
					id = "concrete_slab",
					title = "Перекрытие 15 м³ М300",
					description = "Перекрытие между этажами, бетон марки М300, пропорции 1:1.5:3",
					inputSummary = "Объём: 15 м³, Марка: М300, Пропорции: 1:1.5:3 (цемент:песок:щебень), В/Ц: 0.45",
					resultSummary = "Цемент: 3818 кг (77 мешков), Песок: 6545 кг, Щебень: 11455 кг, Вода: 1718 л"
				),
				UsageExample(
					id = "concrete_walkway",
					title = "Отмостка 2 м³ М100",
					description = "Отмостка вокруг дома, бетон марки М100, пропорции 1:4:6",
					inputSummary = "Объём: 2 м³, Марка: М100, Пропорции: 1:4:6 (цемент:песок:щебень), В/Ц: 0.7",
					resultSummary = "Цемент: 255 кг (6 мешков), Песок: 1164 кг, Щебень: 1527 кг, Вода: 179 л"
				),
				UsageExample(
					id = "concrete_column",
					title = "Колонна 3 м³ М250",
					description = "Несущая колонна, бетон марки М250, пропорции 1:2:4",
					inputSummary = "Объём: 3 м³, Марка: М250, Пропорции: 1:2:4 (цемент:песок:щебень), В/Ц: 0.5",
					resultSummary = "Цемент: 600 кг (12 мешков), Песок: 1371 кг, Щебень: 2400 кг, Вода: 300 л"
				),
				UsageExample(
					id = "concrete_slab_high",
					title = "Плита перекрытия 20 м³ М400",
					description = "Особо ответственная плита, бетон марки М400, пропорции 1:1:2",
					inputSummary = "Объём: 20 м³, Марка: М400, Пропорции: 1:1:2 (цемент:песок:щебень), В/Ц: 0.4",
					resultSummary = "Цемент: 7000 кг (140 мешков), Песок: 8000 кг, Щебень: 14000 кг, Вода: 2800 л"
				)
			)
		)
	}
	
	private fun createRoofCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "roof",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор кровли",
			shortDescription = "Расчёт площади крыши, количества кровельных материалов и веса",
			inputFields = listOf(
				InputFieldDefinition(
					id = "house_length",
					label = "Длина дома",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина дома"
				),
				InputFieldDefinition(
					id = "house_width",
					label = "Ширина дома",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина дома"
				),
				InputFieldDefinition(
					id = "roof_type",
					label = "Тип крыши",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 2.0,
					hint = "Выберите тип крыши",
					options = listOf(
						Pair(1.0, "Односкатная"),
						Pair(2.0, "Двускатная"),
						Pair(3.0, "Вальмовая"),
						Pair(4.0, "Мансардная")
					)
				),
				InputFieldDefinition(
					id = "roof_angle",
					label = "Угол наклона крыши",
					unit = "°",
					type = InputFieldType.NUMBER,
					defaultValue = 30.0,
					hint = "Угол наклона крыши в градусах"
				),
				InputFieldDefinition(
					id = "overhang",
					label = "Свес",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.5,
					hint = "Длина свеса по периметру"
				),
				InputFieldDefinition(
					id = "sheet_length",
					label = "Длина листа",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 1.18,
					hint = "Длина листа кровельного материала"
				),
				InputFieldDefinition(
					id = "sheet_width",
					label = "Ширина листа",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.35,
					hint = "Ширина листа кровельного материала"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас материала",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 10.0,
					hint = "Процент запаса материала (10% для простых форм, 15% для сложных)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "roof_area",
					label = "Площадь крыши",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "material_area",
					label = "Площадь материала",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "sheets_count",
					label = "Количество листов",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "roof_gable_house",
					title = "Двускатная крыша 6×8 м",
					description = "Дом 6×8 метров, двускатная крыша, металлочерепица",
					inputSummary = "Длина дома: 8 м, Ширина дома: 6 м, Тип: Двускатная, Угол: 30°, Свес: 0.5 м, Лист: 1.18×0.35 м, Запас: 10%",
					resultSummary = "Площадь: 76.38 м², Листов: 185 шт"
				),
				UsageExample(
					id = "roof_hip_house",
					title = "Вальмовая крыша 10×10 м",
					description = "Дом 10×10 метров, вальмовая крыша, профнастил",
					inputSummary = "Длина дома: 10 м, Ширина дома: 10 м, Тип: Вальмовая, Угол: 25°, Свес: 0.6 м, Лист: 1.0×1.2 м, Запас: 10%",
					resultSummary = "Площадь: 196.26 м², Листов: 164 шт"
				),
				UsageExample(
					id = "roof_single_garage",
					title = "Односкатная крыша 5×4 м",
					description = "Гараж 5×4 метра, односкатная крыша, ондулин",
					inputSummary = "Длина дома: 5 м, Ширина дома: 4 м, Тип: Односкатная, Угол: 15°, Свес: 0.3 м, Лист: 2.0×0.95 м, Запас: 10%",
					resultSummary = "Площадь: 28.71 м², Листов: 16 шт"
				),
				UsageExample(
					id = "roof_mansard_house",
					title = "Мансардная крыша 8×6 м",
					description = "Дом 8×6 метров, мансардная крыша, мягкая черепица",
					inputSummary = "Длина дома: 8 м, Ширина дома: 6 м, Тип: Мансардная, Угол: 35°, Свес: 0.5 м, Лист: 1.0×1.0 м, Запас: 15%",
					resultSummary = "Площадь: 123.74 м², Листов: 124 шт"
				)
			)
		)
	}
	
	private fun createBrickBlocksCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "brick_blocks",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор кирпича и блоков",
			shortDescription = "Расчёт количества кирпичей, газоблоков и пеноблоков для стены",
			inputFields = listOf(
				InputFieldDefinition(
					id = "wall_length",
					label = "Длина стены",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина стены"
				),
				InputFieldDefinition(
					id = "wall_height",
					label = "Высота стены",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота стены"
				),
				InputFieldDefinition(
					id = "wall_thickness_bricks",
					label = "Толщина стены",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите толщину стены",
					options = listOf(
						Pair(0.5, "0.5 кирпича (120 мм)"),
						Pair(1.0, "1 кирпич (250 мм)"),
						Pair(1.5, "1.5 кирпича (380 мм)"),
						Pair(2.0, "2 кирпича (510 мм)")
					)
				),
				InputFieldDefinition(
					id = "material_type",
					label = "Тип материала",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип материала",
					options = listOf(
						Pair(1.0, "Кирпич одинарный"),
						Pair(2.0, "Кирпич полуторный"),
						Pair(3.0, "Кирпич двойной"),
						Pair(4.0, "Газоблок"),
						Pair(5.0, "Пеноблок")
					)
				),
				InputFieldDefinition(
					id = "material_length",
					label = "Длина",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 250.0,
					hint = "Длина материала в миллиметрах"
				),
				InputFieldDefinition(
					id = "material_width",
					label = "Ширина",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 120.0,
					hint = "Ширина материала в миллиметрах"
				),
				InputFieldDefinition(
					id = "material_height",
					label = "Высота",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 65.0,
					hint = "Высота материала в миллиметрах"
				),
				InputFieldDefinition(
					id = "joint_thickness",
					label = "Толщина шва",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 10.0,
					hint = "Толщина шва раствора или клея (для кирпича 10 мм, для блоков 3-5 мм)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас материала",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 5.0,
					hint = "Процент запаса материала (5-10%)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "material_count",
					label = "Количество",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "wall_volume",
					label = "Объём стены",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "mortar_volume",
					label = "Объём раствора/клея",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "brick_single_wall",
					title = "Стена 10×3 м, кирпич одинарный",
					description = "Несущая стена, одинарный кирпич, толщина 1.5 кирпича",
					inputSummary = "Длина: 10 м, Высота: 3 м, Толщина: 1.5 кирпича, Материал: Кирпич одинарный (250×120×65 мм), Шов: 10 мм, Запас: 5%",
					resultSummary = "Количество: 2394 шт, Раствор: 1.14 м³"
				),
				UsageExample(
					id = "gas_block_wall",
					title = "Стена 8×2.5 м, газоблок",
					description = "Наружная стена, газоблок, толщина 1 блок",
					inputSummary = "Длина: 8 м, Высота: 2.5 м, Толщина: 1 блок, Материал: Газоблок (600×300×200 мм), Шов: 3 мм, Запас: 5%",
					resultSummary = "Количество: 164 шт, Клей: 0.06 м³"
				),
				UsageExample(
					id = "brick_half_partition",
					title = "Перегородка 5×2.5 м, кирпич полуторный",
					description = "Внутренняя перегородка, полуторный кирпич, толщина 0.5 кирпича",
					inputSummary = "Длина: 5 м, Высота: 2.5 м, Толщина: 0.5 кирпича, Материал: Кирпич полуторный (250×120×88 мм), Шов: 10 мм, Запас: 5%",
					resultSummary = "Количество: 250 шт, Раствор: 0.15 м³"
				),
				UsageExample(
					id = "foam_block_wall",
					title = "Стена 12×3.5 м, пеноблок",
					description = "Наружная стена, пеноблок, толщина 1 блок",
					inputSummary = "Длина: 12 м, Высота: 3.5 м, Толщина: 1 блок, Материал: Пеноблок (600×300×200 мм), Шов: 3 мм, Запас: 5%",
					resultSummary = "Количество: 357 шт, Клей: 0.126 м³"
				),
				UsageExample(
					id = "brick_double_wall",
					title = "Стена 6×2.8 м, кирпич двойной",
					description = "Несущая стена, двойной кирпич, толщина 2 кирпича",
					inputSummary = "Длина: 6 м, Высота: 2.8 м, Толщина: 2 кирпича, Материал: Кирпич двойной (250×120×138 мм), Шов: 10 мм, Запас: 5%",
					resultSummary = "Количество: 918 шт, Раствор: 0.86 м³"
				),
				UsageExample(
					id = "gas_block_partition",
					title = "Перегородка 4×2.2 м, газоблок",
					description = "Внутренняя перегородка, газоблок, толщина 1 блок",
					inputSummary = "Длина: 4 м, Высота: 2.2 м, Толщина: 1 блок, Материал: Газоблок (600×300×200 мм), Шов: 3 мм, Запас: 5%",
					resultSummary = "Количество: 81 шт, Клей: 0.026 м³"
				)
			)
		)
	}
	
	private fun createStairsCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "stairs",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор лестницы",
			shortDescription = "Онлайн-калькулятор лестницы рассчитает угол наклона, количество ступеней, высоту и длину пролёта",
			inputFields = listOf(
				InputFieldDefinition(
					id = "total_height",
					label = "Общая высота подъёма",
					unit = "мм",
					type = InputFieldType.LENGTH,
					hint = "Общая высота подъёма от пола до пола следующего этажа"
				),
				InputFieldDefinition(
					id = "step_depth",
					label = "Глубина ступени",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 300.0,
					hint = "Глубина проступи (горизонтальная часть ступени)"
				),
				InputFieldDefinition(
					id = "step_height",
					label = "Высота ступени",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 180.0,
					hint = "Высота подступенка (вертикальная часть ступени)"
				),
				InputFieldDefinition(
					id = "stairs_type",
					label = "Тип лестницы",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип лестницы",
					options = listOf(
						Pair(1.0, "Прямая"),
						Pair(2.0, "Поворотная 90°"),
						Pair(3.0, "Поворотная 180°")
					)
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "steps_count",
					label = "Количество ступеней",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "flights_count",
					label = "Количество пролётов",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "steps_per_flight",
					label = "Ступеней в пролёте",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "flight_length",
					label = "Длина одного пролёта",
					unit = "мм"
				),
				ResultFieldDefinition(
					id = "total_length",
					label = "Общая длина лестницы",
					unit = "мм"
				),
				ResultFieldDefinition(
					id = "landing_depth",
					label = "Глубина площадки",
					unit = "мм"
				),
				ResultFieldDefinition(
					id = "angle",
					label = "Угол наклона",
					unit = "°"
				),
				ResultFieldDefinition(
					id = "comfort_formula",
					label = "Формула удобной лестницы",
					unit = "мм"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "stairs_optimal_house",
					title = "Оптимальная лестница для частного дома",
					description = "Стандартная лестница на второй этаж в частном доме с оптимальными параметрами комфорта",
					inputSummary = "Высота подъёма: 3000 мм, Высота ступени: 180 мм, Глубина ступени: 300 мм, Тип: Прямая",
					resultSummary = "17 ступеней, 5100 мм, угол 30.96°, формула 660 мм"
				),
				UsageExample(
					id = "stairs_cottage",
					title = "Лестница для коттеджа с высокими потолками",
					description = "Лестница для помещения с высотой потолков 3.5 метра",
					inputSummary = "Высота подъёма: 3500 мм, Высота ступени: 175 мм, Глубина ступени: 280 мм, Тип: Прямая",
					resultSummary = "20 ступеней, 5600 мм, угол 32.01°, формула 630 мм"
				),
				UsageExample(
					id = "stairs_compact_dacha",
					title = "Компактная лестница для дачи",
					description = "Экономия пространства с сохранением комфорта использования",
					inputSummary = "Высота подъёма: 2800 мм, Высота ступени: 200 мм, Глубина ступени: 250 мм, Тип: Поворотная 90°",
					resultSummary = "14 ступеней, 3500 мм, угол 38.66°, формула 650 мм"
				),
				UsageExample(
					id = "stairs_basement",
					title = "Лестница в подвал",
					description = "Крутая лестница для спуска в подвальное помещение",
					inputSummary = "Высота подъёма: 2200 мм, Высота ступени: 220 мм, Глубина ступени: 200 мм, Тип: Прямая",
					resultSummary = "10 ступеней, 2000 мм, угол 47.73°, формула 640 мм"
				),
				UsageExample(
					id = "stairs_elderly",
					title = "Лестница для пожилых людей",
					description = "Пологий подъём с комфортными параметрами для людей с ограниченной подвижностью",
					inputSummary = "Высота подъёма: 3200 мм, Высота ступени: 150 мм, Глубина ступени: 350 мм, Тип: Прямая",
					resultSummary = "22 ступени, 7700 мм, угол 23.20°, формула 650 мм"
				),
				UsageExample(
					id = "stairs_spiral",
					title = "Винтовая лестница",
					description = "Компактное решение для ограниченного пространства",
					inputSummary = "Высота подъёма: 3000 мм, Высота ступени: 200 мм, Глубина ступени: 200 мм, Тип: Поворотная 180°",
					resultSummary = "15 ступеней, 3000 мм, угол 45.00°, формула 600 мм"
				)
			)
		)
	}
	
	private fun createGravelCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "gravel",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор щебня",
			shortDescription = "Расчёт количества щебня для фундамента, дорожек, отмостки и подсыпки",
			inputFields = listOf(
				InputFieldDefinition(
					id = "work_type",
					label = "Тип работ",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип работ",
					options = listOf(
						Pair(1.0, "Фундамент - Щебень для подушки фундамента"),
						Pair(2.0, "Дорожка - Щебень для садовой дорожки"),
						Pair(3.0, "Отмостка - Щебень для отмостки вокруг дома"),
						Pair(4.0, "Подсыпка - Щебень для выравнивания и подсыпки"),
						Pair(5.0, "Свой вариант - Произвольный расчёт")
					)
				),
				InputFieldDefinition(
					id = "input_method",
					label = "Способ ввода данных",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите способ ввода данных",
					options = listOf(
						Pair(1.0, "Длина × Ширина"),
						Pair(2.0, "Площадь"),
						Pair(3.0, "Объём")
					)
				),
				InputFieldDefinition(
					id = "length",
					label = "Длина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина участка (используется при способе 'Длина × Ширина')"
				),
				InputFieldDefinition(
					id = "width",
					label = "Ширина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина участка (используется при способе 'Длина × Ширина')"
				),
				InputFieldDefinition(
					id = "area",
					label = "Площадь",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь участка (используется при способе 'Площадь')"
				),
				InputFieldDefinition(
					id = "volume",
					label = "Объём",
					unit = "м³",
					type = InputFieldType.VOLUME,
					hint = "Объём (используется при способе 'Объём')"
				),
				InputFieldDefinition(
					id = "layer_thickness",
					label = "Толщина слоя",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.2,
					hint = "Толщина слоя щебня"
				),
				InputFieldDefinition(
					id = "fraction_type",
					label = "Фракция щебня",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 3.0,
					hint = "Выберите фракцию щебня",
					options = listOf(
						Pair(1.0, "5-10 мм - Мелкий щебень для декора"),
						Pair(2.0, "10-20 мм - Стандартный для фундамента"),
						Pair(3.0, "20-40 мм - Крупный для дорожек"),
						Pair(4.0, "40-70 мм - Очень крупный для подсыпки"),
						Pair(5.0, "Своя фракция - Укажите плотность вручную")
					)
				),
				InputFieldDefinition(
					id = "gravel_density",
					label = "Плотность щебня",
					unit = "кг/м³",
					type = InputFieldType.NUMBER,
					defaultValue = 1400.0,
					hint = "Плотность щебня (автоматически обновляется при выборе фракции)"
				),
				InputFieldDefinition(
					id = "waste_percent",
					label = "Запас материала",
					unit = "%",
					type = InputFieldType.NUMBER,
					defaultValue = 10.0,
					hint = "Запас материала для компенсации усадки при трамбовке"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "gravel_volume",
					label = "Объём щебня",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "gravel_mass",
					label = "Вес щебня",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "gravel_volume_with_waste",
					label = "Объём с запасом",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "gravel_mass_with_waste",
					label = "Вес с запасом",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "gravel_foundation",
					title = "Подушка под фундамент 8×10 м",
					description = "Стандартная подушка под ленточный фундамент, щебень фракции 20-40 мм",
					inputSummary = "Тип: Фундамент, Способ: Длина × Ширина, Длина: 8 м, Ширина: 10 м, Толщина: 0.2 м, Фракция: 20-40 мм, Плотность: 1400 кг/м³, Запас: 10%",
					resultSummary = "Объём: 16 м³ (22,400 кг), с запасом: 17.6 м³ (24,640 кг)"
				),
				UsageExample(
					id = "gravel_path",
					title = "Садовая дорожка 15×1 м",
					description = "Садовая дорожка из щебня, фракция 10-20 мм",
					inputSummary = "Тип: Дорожка, Способ: Длина × Ширина, Длина: 15 м, Ширина: 1 м, Толщина: 0.1 м, Фракция: 10-20 мм, Плотность: 1450 кг/м³, Запас: 15%",
					resultSummary = "Объём: 1.5 м³ (2,175 кг), с запасом: 1.725 м³ (2,501 кг)"
				),
				UsageExample(
					id = "gravel_blind_area",
					title = "Отмостка вокруг дома",
					description = "Отмостка шириной 1 м вокруг дома 10×12 м, щебень 20-40 мм",
					inputSummary = "Тип: Отмостка, Способ: Длина × Ширина, Длина: 44 м (периметр), Ширина: 1 м, Толщина: 0.15 м, Фракция: 20-40 мм, Плотность: 1400 кг/м³, Запас: 10%",
					resultSummary = "Объём: 6.6 м³ (9,240 кг), с запасом: 7.26 м³ (10,164 кг)"
				),
				UsageExample(
					id = "gravel_fill",
					title = "Подсыпка под плитку 20×15 м",
					description = "Выравнивающая подсыпка под тротуарную плитку, крупная фракция",
					inputSummary = "Тип: Подсыпка, Способ: Площадь, Площадь: 300 м², Толщина: 0.08 м, Фракция: 20-40 мм, Плотность: 1400 кг/м³, Запас: 5%",
					resultSummary = "Объём: 24 м³ (33,600 кг), с запасом: 25.2 м³ (35,280 кг)"
				),
				UsageExample(
					id = "gravel_decorative",
					title = "Декоративная дорожка с мелким щебнем",
					description = "Декоративная дорожка из мелкого щебня 5-10 мм",
					inputSummary = "Тип: Дорожка, Способ: Длина × Ширина, Длина: 10 м, Ширина: 0.8 м, Толщина: 0.05 м, Фракция: 5-10 мм, Плотность: 1500 кг/м³, Запас: 20%",
					resultSummary = "Объём: 0.4 м³ (600 кг), с запасом: 0.48 м³ (720 кг)"
				),
				UsageExample(
					id = "gravel_large_volume",
					title = "Большой участок подсыпки",
					description = "Выравнивание большого участка, объём 50 м³",
					inputSummary = "Тип: Подсыпка, Способ: Объём, Объём: 50 м³, Фракция: 40-70 мм, Плотность: 1350 кг/м³, Запас: 10%",
					resultSummary = "Объём: 50 м³ (67,500 кг), с запасом: 55 м³ (74,250 кг)"
				)
			)
		)
	}
	
	// Engineering Systems Calculators
	
	private fun createVentilationCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "ventilation",
			categoryId = CATEGORY_ENGINEERING,
			name = "Калькулятор вентиляции",
			shortDescription = "Расчёт производительности вентиляции для помещений",
			inputFields = listOf(
				InputFieldDefinition(
					id = "room_length",
					label = "Длина помещения",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина помещения"
				),
				InputFieldDefinition(
					id = "room_width",
					label = "Ширина помещения",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина помещения"
				),
				InputFieldDefinition(
					id = "room_height",
					label = "Высота помещения",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 2.7,
					hint = "Высота помещения"
				),
				InputFieldDefinition(
					id = "room_type",
					label = "Тип помещения",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип помещения (кратность воздухообмена установится автоматически)",
					options = listOf(
						Pair(1.0, "Жилая комната - 1 раз/ч"),
						Pair(2.0, "Кухня - 3 раз/ч"),
						Pair(3.0, "Ванная/туалет - 3 раз/ч"),
						Pair(4.0, "Офис - 2 раз/ч"),
						Pair(5.0, "Ресторан/кафе - 5 раз/ч"),
						Pair(6.0, "Спортзал - 4 раз/ч"),
						Pair(7.0, "Учебный класс - 2 раз/ч"),
						Pair(8.0, "Склад - 1 раз/ч"),
						Pair(9.0, "Производство - 3 раз/ч")
					)
				),
				InputFieldDefinition(
					id = "people_count",
					label = "Количество людей",
					unit = "чел",
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "Количество людей в помещении"
				),
				InputFieldDefinition(
					id = "air_exchange_rate",
					label = "Кратность воздухообмена",
					unit = "раз/ч",
					type = InputFieldType.NUMBER,
					hint = "Кратность воздухообмена (рекомендуемое значение устанавливается автоматически по типу помещения, можно изменить вручную)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "room_volume",
					label = "Объём помещения",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "airflow_by_volume",
					label = "Производительность по объёму",
					unit = "м³/ч"
				),
				ResultFieldDefinition(
					id = "airflow_by_people",
					label = "Производительность по людям",
					unit = "м³/ч"
				),
				ResultFieldDefinition(
					id = "required_airflow",
					label = "Требуемая производительность",
					unit = "м³/ч"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "ventilation_living_room",
					title = "Жилая комната 4×5 м",
					description = "Спальня или гостиная стандартного размера, высота 2.7 м",
					inputSummary = "Длина: 4 м, Ширина: 5 м, Высота: 2.7 м, Тип: Жилая комната, Количество людей: 2, Кратность: 1 раз/ч (авто), Норма на человека: 30 м³/ч (авто)",
					resultSummary = "Объём: 54 м³, По объёму: 54 м³/ч, По людям: 60 м³/ч, Требуемая: 60 м³/ч"
				),
				UsageExample(
					id = "ventilation_kitchen",
					title = "Кухня 3×4 м с вытяжкой",
					description = "Кухня стандартного размера с газовой плитой, высота 2.7 м",
					inputSummary = "Длина: 3 м, Ширина: 4 м, Высота: 2.7 м, Тип: Кухня, Количество людей: 1, Кратность: 3 раз/ч (авто), Норма на человека: 60 м³/ч (авто)",
					resultSummary = "Объём: 32.4 м³, По объёму: 97.2 м³/ч, По людям: 60 м³/ч, Требуемая: 97.2 м³/ч"
				),
				UsageExample(
					id = "ventilation_bathroom",
					title = "Ванная комната 2×2 м",
					description = "Санузел небольшого размера, высота 2.5 м",
					inputSummary = "Длина: 2 м, Ширина: 2 м, Высота: 2.5 м, Тип: Ванная/туалет, Количество людей: 1, Кратность: 3 раз/ч (авто), Норма на человека: 25 м³/ч (авто)",
					resultSummary = "Объём: 10 м³, По объёму: 30 м³/ч, По людям: 25 м³/ч, Требуемая: 30 м³/ч"
				),
				UsageExample(
					id = "ventilation_office",
					title = "Офисное помещение 6×8 м",
					description = "Офис открытого типа для 10 сотрудников, высота 3 м",
					inputSummary = "Длина: 6 м, Ширина: 8 м, Высота: 3 м, Тип: Офис, Количество людей: 10, Кратность: 2 раз/ч (авто), Норма на человека: 40 м³/ч (авто)",
					resultSummary = "Объём: 144 м³, По объёму: 288 м³/ч, По людям: 400 м³/ч, Требуемая: 400 м³/ч"
				),
				UsageExample(
					id = "ventilation_restaurant",
					title = "Ресторан зал 10×12 м",
					description = "Зал ресторана на 50 посетителей, высота 3.5 м",
					inputSummary = "Длина: 10 м, Ширина: 12 м, Высота: 3.5 м, Тип: Ресторан/кафе, Количество людей: 50, Кратность: 5 раз/ч (авто), Норма на человека: 60 м³/ч (авто)",
					resultSummary = "Объём: 420 м³, По объёму: 2100 м³/ч, По людям: 3000 м³/ч, Требуемая: 3000 м³/ч"
				),
				UsageExample(
					id = "ventilation_gym",
					title = "Спортзал 8×10 м",
					description = "Спортзал для тренировок, высота 4 м",
					inputSummary = "Длина: 8 м, Ширина: 10 м, Высота: 4 м, Тип: Спортзал, Количество людей: 15, Кратность: 4 раз/ч (авто), Норма на человека: 80 м³/ч (авто)",
					resultSummary = "Объём: 320 м³, По объёму: 1280 м³/ч, По людям: 1200 м³/ч, Требуемая: 1280 м³/ч"
				)
			)
		)
	}
	
	private fun createHeatedFloorCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "heated_floor",
			categoryId = CATEGORY_ENGINEERING,
			name = "Калькулятор тёплого пола",
			shortDescription = "Расчёт мощности и потребления электрического тёплого пола",
			inputFields = listOf(
				InputFieldDefinition(
					id = "floor_area",
					label = "Площадь помещения",
					unit = "м²",
					type = InputFieldType.AREA,
					defaultValue = 10.0,
					hint = "Площадь обогреваемого пола"
				),
				InputFieldDefinition(
					id = "room_type",
					label = "Тип помещения",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "1 - Ванная комната, 2 - Кухня, 3 - Гостиная, 4 - Спальня, 5 - Балкон/лоджия"
				),
				InputFieldDefinition(
					id = "insulation_type",
					label = "Тип утепления пола",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 2.0,
					hint = "1 - Хорошее утепление, 2 - Среднее утепление, 3 - Слабое утепление"
				),
				InputFieldDefinition(
					id = "desired_temperature",
					label = "Желаемая температура",
					unit = "°C",
					type = InputFieldType.NUMBER,
					defaultValue = 25.0,
					hint = "Желаемая температура пола"
				),
				InputFieldDefinition(
					id = "usage_hours",
					label = "Часы работы в день",
					unit = "ч",
					type = InputFieldType.NUMBER,
					defaultValue = 8.0,
					hint = "Количество часов работы в сутки"
				),
				InputFieldDefinition(
					id = "electricity_price",
					label = "Стоимость электроэнергии",
					unit = "₽/кВт⋅ч",
					type = InputFieldType.NUMBER,
					defaultValue = 5.5,
					hint = "Стоимость одного киловатт-часа"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "recommended_power",
					label = "Рекомендуемая мощность",
					unit = "Вт/м²"
				),
				ResultFieldDefinition(
					id = "total_power_watts",
					label = "Общая мощность",
					unit = "Вт"
				),
				ResultFieldDefinition(
					id = "total_power_kw",
					label = "Общая мощность",
					unit = "кВт"
				),
				ResultFieldDefinition(
					id = "daily_consumption",
					label = "Потребление в день",
					unit = "кВт⋅ч"
				),
				ResultFieldDefinition(
					id = "monthly_consumption",
					label = "Потребление в месяц",
					unit = "кВт⋅ч"
				),
				ResultFieldDefinition(
					id = "daily_cost",
					label = "Стоимость в день",
					unit = "₽"
				),
				ResultFieldDefinition(
					id = "monthly_cost",
					label = "Стоимость в месяц",
					unit = "₽"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "heated_floor_bathroom",
					title = "Ванная комната 5 м²",
					description = "Ванная комната с хорошим утеплением, температура 28°C",
					inputSummary = "Площадь: 5 м², Тип: Ванная, Утепление: Хорошее, Температура: 28°C, Часы работы: 8 ч/день, Стоимость: 5.5 ₽/кВт⋅ч",
					resultSummary = "Мощность: ~127.5 Вт/м², Общая: ~637.5 Вт, Потребление: ~25.5 кВт⋅ч/день, Стоимость: ~140 ₽/день"
				),
				UsageExample(
					id = "heated_floor_kitchen",
					title = "Кухня 12 м²",
					description = "Кухня со средним утеплением, температура 25°C",
					inputSummary = "Площадь: 12 м², Тип: Кухня, Утепление: Среднее, Температура: 25°C, Часы работы: 10 ч/день, Стоимость: 5.5 ₽/кВт⋅ч",
					resultSummary = "Мощность: ~120 Вт/м², Общая: ~1440 Вт, Потребление: ~172.8 кВт⋅ч/день, Стоимость: ~950 ₽/день"
				),
				UsageExample(
					id = "heated_floor_living_room",
					title = "Гостиная 20 м²",
					description = "Гостиная с хорошим утеплением, температура 24°C",
					inputSummary = "Площадь: 20 м², Тип: Гостиная, Утепление: Хорошее, Температура: 24°C, Часы работы: 12 ч/день, Стоимость: 5.5 ₽/кВт⋅ч",
					resultSummary = "Мощность: ~90 Вт/м², Общая: ~1800 Вт, Потребление: ~432 кВт⋅ч/день, Стоимость: ~2376 ₽/день"
				),
				UsageExample(
					id = "heated_floor_bedroom",
					title = "Спальня 15 м²",
					description = "Спальня со средним утеплением, температура 23°C",
					inputSummary = "Площадь: 15 м², Тип: Спальня, Утепление: Среднее, Температура: 23°C, Часы работы: 6 ч/день, Стоимость: 5.5 ₽/кВт⋅ч",
					resultSummary = "Мощность: ~90 Вт/м², Общая: ~1350 Вт, Потребление: ~121.5 кВт⋅ч/день, Стоимость: ~668 ₽/день"
				),
				UsageExample(
					id = "heated_floor_balcony",
					title = "Балкон 6 м²",
					description = "Балкон со слабым утеплением, температура 20°C",
					inputSummary = "Площадь: 6 м², Тип: Балкон, Утепление: Слабое, Температура: 20°C, Часы работы: 24 ч/день, Стоимость: 5.5 ₽/кВт⋅ч",
					resultSummary = "Мощность: ~234 Вт/м², Общая: ~1404 Вт, Потребление: ~202.2 кВт⋅ч/день, Стоимость: ~1112 ₽/день"
				)
			)
		)
	}
	
	private fun createWaterPipesCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "water_pipes",
			categoryId = CATEGORY_ENGINEERING,
			name = "Калькулятор водопроводных труб",
			shortDescription = "Расчёт диаметра, пропускной способности и гидравлических параметров водопроводных труб",
			inputFields = listOf(
				InputFieldDefinition(
					id = "calculation_type",
					label = "Тип расчёта",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип расчёта",
					options = listOf(
						Pair(1.0, "По диаметру - расчёт расхода и скорости"),
						Pair(2.0, "По расходу - расчёт диаметра и скорости"),
						Pair(3.0, "По давлению - расчёт потерь давления")
					)
				),
				InputFieldDefinition(
					id = "pipe_diameter",
					label = "Диаметр трубы",
					unit = "мм",
					type = InputFieldType.LENGTH,
					hint = "Внутренний диаметр трубы (используется при расчёте по диаметру)"
				),
				InputFieldDefinition(
					id = "water_flow",
					label = "Расход воды",
					unit = "м³/с",
					type = InputFieldType.NUMBER,
					hint = "Расход воды (используется при расчёте по расходу)"
				),
				InputFieldDefinition(
					id = "flow_velocity",
					label = "Скорость потока",
					unit = "м/с",
					type = InputFieldType.NUMBER,
					defaultValue = 2.0,
					hint = "Скорость движения воды в трубе"
				),
				InputFieldDefinition(
					id = "pipe_material",
					label = "Материал трубы",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите материал трубы",
					options = listOf(
						Pair(1.0, "Сталь - шероховатость 0.045 мм"),
						Pair(2.0, "Медь - шероховатость 0.0015 мм"),
						Pair(3.0, "Пластик - шероховатость 0.0015 мм"),
						Pair(4.0, "Чугун - шероховатость 0.1 мм")
					)
				),
				InputFieldDefinition(
					id = "pipe_length",
					label = "Длина трубы",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина трубопровода (для расчёта потерь давления)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "calculated_diameter",
					label = "Диаметр трубы",
					unit = "мм"
				),
				ResultFieldDefinition(
					id = "flow_rate_m3s",
					label = "Расход воды",
					unit = "м³/с"
				),
				ResultFieldDefinition(
					id = "flow_rate_ls",
					label = "Расход воды",
					unit = "л/с"
				),
				ResultFieldDefinition(
					id = "flow_rate_m3h",
					label = "Расход воды",
					unit = "м³/ч"
				),
				ResultFieldDefinition(
					id = "flow_velocity_calc",
					label = "Скорость потока",
					unit = "м/с"
				),
				ResultFieldDefinition(
					id = "pipe_area",
					label = "Площадь сечения",
					unit = "м²"
				),
				ResultFieldDefinition(
					id = "pressure_loss",
					label = "Потери давления",
					unit = "бар"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "water_pipes_private_house",
					title = "Расчёт диаметра для частного дома",
					description = "Водопровод для частного дома с расходом 50 л/с",
					inputSummary = "Тип: По расходу, Расход: 0.05 м³/с (50 л/с), Скорость: 2 м/с, Материал: Пластик, Длина: 50 м",
					resultSummary = "Диаметр: ~180 мм, Скорость: 2 м/с, Расход: 0.05 м³/с, Потери: ~0.01 бар"
				),
				UsageExample(
					id = "water_pipes_existing_pipe",
					title = "Пропускная способность существующей трубы",
					description = "Проверка пропускной способности трубы 150 мм",
					inputSummary = "Тип: По диаметру, Диаметр: 150 мм, Скорость: 1.5 м/с, Материал: Сталь, Длина: 100 м",
					resultSummary = "Расход: ~26.5 л/с (95.4 м³/ч), Скорость: 1.5 м/с, Потери: ~0.02 бар"
				),
				UsageExample(
					id = "water_pipes_long_pipeline",
					title = "Потери давления в длинном трубопроводе",
					description = "Расчёт потерь давления в водопроводе 200 м",
					inputSummary = "Тип: По диаметру, Диаметр: 100 мм, Расход: 0.02 м³/с (20 л/с), Материал: Пластик, Длина: 200 м",
					resultSummary = "Скорость: ~2.55 м/с, Расход: 0.02 м³/с, Потери: ~0.1 бар"
				),
				UsageExample(
					id = "water_pipes_material_comparison",
					title = "Сравнение материалов труб",
					description = "Сравнение стальной и пластиковой трубы",
					inputSummary = "Тип: По диаметру, Диаметр: 100 мм, Расход: 0.01 м³/с, Материал: Сталь/Пластик, Длина: 100 м",
					resultSummary = "Сталь: потери ~0.2 бар, Пластик: потери ~0.14 бар"
				),
				UsageExample(
					id = "water_pipes_multi_story",
					title = "Расчёт для многоэтажного дома",
					description = "Водопровод для 10-этажного дома",
					inputSummary = "Тип: По расходу, Расход: 0.3 м³/с (300 л/с), Скорость: 2.5 м/с, Материал: Сталь, Длина: 500 м",
					resultSummary = "Диаметр: ~390 мм (округляем до 400 мм), Скорость: ~2.39 м/с"
				),
				UsageExample(
					id = "water_pipes_heating",
					title = "Расчёт для системы отопления",
					description = "Трубопровод для системы отопления",
					inputSummary = "Тип: По расходу, Расход: 0.08 м³/с (80 л/с), Скорость: 1.8 м/с, Материал: Медь, Длина: 150 м",
					resultSummary = "Диаметр: ~240 мм (округляем до 250 мм), Скорость: ~1.63 м/с"
				)
			)
		)
	}
	
	// Metal & Electricity Calculators
	
	private fun createRebarCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "rebar",
			categoryId = CATEGORY_METAL_ELECTRICITY,
			name = "Калькулятор арматуры",
			shortDescription = "Расчёт количества, длины и массы арматуры для фундамента, плиты, колонн и стен",
			inputFields = listOf(
				InputFieldDefinition(
					id = "structure_type",
					label = "Тип конструкции",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип конструкции",
					options = listOf(
						Pair(1.0, "Фундамент"),
						Pair(2.0, "Плита"),
						Pair(3.0, "Стена"),
						Pair(4.0, "Колонна")
					)
				),
				InputFieldDefinition(
					id = "length",
					label = "Длина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина конструкции"
				),
				InputFieldDefinition(
					id = "width",
					label = "Ширина",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина конструкции"
				),
				InputFieldDefinition(
					id = "height",
					label = "Высота",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота конструкции"
				),
				InputFieldDefinition(
					id = "mesh_step",
					label = "Шаг сетки",
					unit = "см",
					type = InputFieldType.DROPDOWN,
					defaultValue = 20.0,
					hint = "Расстояние между арматурными стержнями (обычно 15-25 см)",
					options = listOf(
						Pair(10.0, "10 см"),
						Pair(15.0, "15 см"),
						Pair(20.0, "20 см"),
						Pair(25.0, "25 см"),
						Pair(30.0, "30 см")
					)
				),
				InputFieldDefinition(
					id = "rebar_diameter",
					label = "Диаметр арматуры",
					unit = "мм",
					type = InputFieldType.DROPDOWN,
					defaultValue = 12.0,
					hint = "Выберите диаметр арматурного стержня",
					options = listOf(
						Pair(6.0, "6 мм"),
						Pair(8.0, "8 мм"),
						Pair(10.0, "10 мм"),
						Pair(12.0, "12 мм"),
						Pair(14.0, "14 мм"),
						Pair(16.0, "16 мм"),
						Pair(18.0, "18 мм"),
						Pair(20.0, "20 мм")
					)
				),
				InputFieldDefinition(
					id = "layers_count",
					label = "Количество слоёв",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 2.0,
					hint = "Количество слоёв армирования (обычно 1-2)"
				),
				InputFieldDefinition(
					id = "overlap",
					label = "Перехлёст",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 0.0,
					hint = "Перехлёст стержней при соединении (обычно 30-50 диаметров)"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "total_length",
					label = "Общая длина",
					unit = "м"
				),
				ResultFieldDefinition(
					id = "total_mass",
					label = "Общая масса",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "rebar_count",
					label = "Количество стержней",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "mass_per_meter",
					label = "Масса на метр",
					unit = "кг/м"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "rebar_foundation",
					title = "Ленточный фундамент 10×0.5×0.6 м",
					description = "Расчёт арматуры для армирования ленточного фундамента.",
					inputSummary = "Тип: Фундамент, длина 10 м, ширина 0.5 м, высота 0.6 м, шаг 20 см, диаметр 12 мм, слоёв 2",
					resultSummary = "Общая длина 131 м, масса 116.3 кг, количество стержней 102"
				),
				UsageExample(
					id = "rebar_slab",
					title = "Плита перекрытия 6×4 м",
					description = "Расчёт арматуры для армирования бетонной плиты перекрытия.",
					inputSummary = "Тип: Плита, длина 6 м, ширина 4 м, высота 0.2 м, шаг 15 см, диаметр 10 мм, слоёв 2",
					resultSummary = "Общая длина 664 м, масса 409.7 кг, количество стержней 138"
				),
				UsageExample(
					id = "rebar_wall",
					title = "Монолитная стена 3×2.5×0.3 м",
					description = "Расчёт арматуры для армирования монолитной стены.",
					inputSummary = "Тип: Стена, длина 3 м, ширина 2.5 м, высота 0.3 м, шаг 20 см, диаметр 8 мм, слоёв 1",
					resultSummary = "Общая длина 82 м, масса 32.4 кг, количество стержней 30"
				)
			)
		)
	}
	
	private fun createCableSectionCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "cable_section",
			categoryId = CATEGORY_METAL_ELECTRICITY,
			name = "Калькулятор сечения кабеля",
			shortDescription = "Онлайн-калькулятор сечения кабеля поможет рассчитать оптимальное сечение проводника по мощности, длине линии и току нагрузки",
			inputFields = listOf(
				InputFieldDefinition(
					id = "calculation_type",
					label = "Тип расчёта",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип расчёта",
					options = listOf(
						Pair(1.0, "По мощности"),
						Pair(2.0, "По току")
					)
				),
				InputFieldDefinition(
					id = "power",
					label = "Мощность",
					unit = "кВт",
					type = InputFieldType.POWER,
					hint = "Мощность нагрузки (используется при расчёте по мощности)"
				),
				InputFieldDefinition(
					id = "current",
					label = "Ток",
					unit = "А",
					type = InputFieldType.NUMBER,
					hint = "Ток нагрузки (используется при расчёте по току)"
				),
				InputFieldDefinition(
					id = "voltage",
					label = "Напряжение",
					unit = "В",
					type = InputFieldType.NUMBER,
					defaultValue = 220.0,
					hint = "Напряжение сети"
				),
				InputFieldDefinition(
					id = "cable_length",
					label = "Длина линии",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина кабеля"
				),
				InputFieldDefinition(
					id = "conductor_material",
					label = "Материал проводника",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите материал проводника",
					options = listOf(
						Pair(1.0, "Медь"),
						Pair(2.0, "Алюминий")
					)
				),
				InputFieldDefinition(
					id = "network_type",
					label = "Тип сети",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип сети",
					options = listOf(
						Pair(1.0, "Однофазная"),
						Pair(2.0, "Трёхфазная")
					)
				),
				InputFieldDefinition(
					id = "power_factor",
					label = "Коэффициент мощности",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 0.8,
					hint = "Коэффициент мощности (cosφ)"
				),
				InputFieldDefinition(
					id = "voltage_drop_percent",
					label = "Допустимые потери напряжения",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 3.0,
					hint = "Допустимые потери напряжения"
				),
				InputFieldDefinition(
					id = "ambient_temperature",
					label = "Температура окружающей среды",
					unit = "°C",
					type = InputFieldType.NUMBER,
					defaultValue = 25.0,
					hint = "Температура окружающей среды"
				),
				InputFieldDefinition(
					id = "installation_type",
					label = "Способ прокладки",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите способ прокладки",
					options = listOf(
						Pair(1.0, "Открытая прокладка"),
						Pair(2.0, "Закрытая прокладка"),
						Pair(3.0, "Подземная прокладка")
					)
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "calculated_current",
					label = "Ток нагрузки",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "cable_section",
					label = "Рекомендуемое сечение",
					unit = "мм²"
				),
				ResultFieldDefinition(
					id = "standard_section",
					label = "Стандартное сечение",
					unit = "мм²"
				),
				ResultFieldDefinition(
					id = "voltage_drop",
					label = "Потери напряжения",
					unit = "В"
				),
				ResultFieldDefinition(
					id = "voltage_drop_percent_calc",
					label = "Потери напряжения",
					unit = "%"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "cable_section_sockets",
					title = "Кабель для розеток в квартире",
					description = "Однофазная сеть 220В, нагрузка 3.5 кВт, длина 20 м",
					inputSummary = "Тип: По мощности, Мощность: 3.5 кВт, Напряжение: 220 В, Длина: 20 м, Материал: Медь, Тип сети: Однофазная, cosφ: 0.9, Потери: 3%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток: ~17.7 А, Сечение: ~2.5 мм², Потери: ~0.005%"
				),
				UsageExample(
					id = "cable_section_lighting",
					title = "Кабель для освещения",
					description = "Однофазная сеть, нагрузка 1.2 кВт, длина 30 м",
					inputSummary = "Тип: По мощности, Мощность: 1.2 кВт, Напряжение: 220 В, Длина: 30 м, Материал: Медь, Тип сети: Однофазная, cosφ: 0.95, Потери: 3%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток: ~5.74 А, Сечение: ~1.5 мм², Потери: ~0.002%"
				),
				UsageExample(
					id = "cable_section_stove",
					title = "Кабель для электроплиты 7 кВт",
					description = "Однофазная сеть, плита мощностью 7 кВт, длина 15 м",
					inputSummary = "Тип: По мощности, Мощность: 7 кВт, Напряжение: 220 В, Длина: 15 м, Материал: Медь, Тип сети: Однофазная, cosφ: 1.0, Потери: 3%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток: ~31.8 А, Сечение: ~4-6 мм², Потери: ~0.002%"
				),
				UsageExample(
					id = "cable_section_three_phase",
					title = "Трёхфазная линия для коттеджа",
					description = "Трёхфазная сеть 380В, нагрузка 15 кВт, длина 50 м",
					inputSummary = "Тип: По мощности, Мощность: 15 кВт, Напряжение: 380 В, Длина: 50 м, Материал: Медь, Тип сети: Трёхфазная, cosφ: 0.9, Потери: 3%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток фазы: ~25.3 А, Сечение: ~4 мм², Потери: ~0.003%"
				),
				UsageExample(
					id = "cable_section_aluminum",
					title = "Алюминиевый кабель для гаража",
					description = "Однофазная сеть, нагрузка 5 кВт, длина 40 м, алюминий",
					inputSummary = "Тип: По мощности, Мощность: 5 кВт, Напряжение: 220 В, Длина: 40 м, Материал: Алюминий, Тип сети: Однофазная, cosφ: 0.9, Потери: 3%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток: ~25.25 А, Сечение: ~6 мм² (алюминий), Потери: ~0.002%"
				),
				UsageExample(
					id = "cable_section_long_line",
					title = "Длинная линия с большими потерями",
					description = "Однофазная сеть, нагрузка 2 кВт, длина 100 м, допустимые потери 5%",
					inputSummary = "Тип: По мощности, Мощность: 2 кВт, Напряжение: 220 В, Длина: 100 м, Материал: Медь, Тип сети: Однофазная, cosφ: 0.9, Потери: 5%, Температура: 25°C, Прокладка: Открытая",
					resultSummary = "Ток: ~10.1 А, Сечение: ~10 мм², Потери: ~5%"
				)
			)
		)
	}
	
	private fun createElectricalCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "electrical",
			categoryId = CATEGORY_METAL_ELECTRICITY,
			name = "Калькулятор электрики",
			shortDescription = "Расчёт сечения кабеля и мощности автоматов для электропроводки",
			inputFields = listOf(
				InputFieldDefinition(
					id = "calculation_type",
					label = "Тип расчёта",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 3.0,
					hint = "Выберите тип расчёта",
					options = listOf(
						Pair(1.0, "Сечение кабеля"),
						Pair(2.0, "Автомат"),
						Pair(3.0, "Оба расчёта")
					)
				),
				InputFieldDefinition(
					id = "input_method",
					label = "Способ ввода данных",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите способ ввода данных",
					options = listOf(
						Pair(1.0, "По мощности (кВт)"),
						Pair(2.0, "По току (А)")
					)
				),
				InputFieldDefinition(
					id = "power",
					label = "Мощность нагрузки",
					unit = "кВт",
					type = InputFieldType.POWER,
					defaultValue = 2.5,
					hint = "Мощность нагрузки (используется при вводе по мощности)"
				),
				InputFieldDefinition(
					id = "current",
					label = "Ток нагрузки",
					unit = "А",
					type = InputFieldType.NUMBER,
					hint = "Ток нагрузки (используется при вводе по току)"
				),
				InputFieldDefinition(
					id = "voltage",
					label = "Напряжение",
					unit = "В",
					type = InputFieldType.DROPDOWN,
					defaultValue = 220.0,
					hint = "Выберите напряжение",
					options = listOf(
						Pair(12.0, "12 В (низковольтное)"),
						Pair(24.0, "24 В (низковольтное)"),
						Pair(220.0, "220 В (однофазная сеть)"),
						Pair(380.0, "380 В (трёхфазная сеть)")
					)
				),
				InputFieldDefinition(
					id = "network_type",
					label = "Тип сети",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип сети",
					options = listOf(
						Pair(1.0, "Однофазная (220 В)"),
						Pair(2.0, "Трёхфазная (380 В)")
					)
				),
				InputFieldDefinition(
					id = "cable_length",
					label = "Длина кабеля",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 15.0,
					hint = "Длина кабеля"
				),
				InputFieldDefinition(
					id = "conductor_material",
					label = "Материал проводника",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите материал проводника",
					options = listOf(
						Pair(1.0, "Медь"),
						Pair(2.0, "Алюминий")
					)
				),
				InputFieldDefinition(
					id = "installation_type",
					label = "Способ прокладки",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите способ прокладки",
					options = listOf(
						Pair(1.0, "Открытая прокладка"),
						Pair(2.0, "Скрытая прокладка"),
						Pair(3.0, "В кабель-канале")
					)
				),
				InputFieldDefinition(
					id = "power_factor",
					label = "Коэффициент мощности",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 0.9,
					hint = "Коэффициент мощности (cos φ), 0.8-0.9 для бытовых нагрузок"
				),
				InputFieldDefinition(
					id = "voltage_drop_percent",
					label = "Допустимое падение напряжения",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 3.0,
					hint = "Обычно 3% для силовых сетей, до 5% для освещения"
				),
				InputFieldDefinition(
					id = "breaker_type",
					label = "Тип автомата",
					unit = null,
					type = InputFieldType.DROPDOWN,
					defaultValue = 1.0,
					hint = "Выберите тип автомата",
					options = listOf(
						Pair(1.0, "Тип C (бытовые)"),
						Pair(2.0, "Тип B (слабоиндуктивные)"),
						Pair(3.0, "Тип D (высокие пусковые токи)")
					)
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "calculated_current",
					label = "Ток нагрузки",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "cable_section",
					label = "Рекомендуемое сечение",
					unit = "мм²"
				),
				ResultFieldDefinition(
					id = "standard_section",
					label = "Стандартное сечение",
					unit = "мм²"
				),
				ResultFieldDefinition(
					id = "breaker_current",
					label = "Номинал автомата",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "breaker_type_result",
					label = "Тип автомата",
					unit = null
				),
				ResultFieldDefinition(
					id = "voltage_drop",
					label = "Потери напряжения",
					unit = "В"
				),
				ResultFieldDefinition(
					id = "voltage_drop_percent_calc",
					label = "Потери напряжения",
					unit = "%"
				),
				ResultFieldDefinition(
					id = "calculation_details",
					label = "Подробности расчёта",
					unit = null
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "electrical_sockets",
					title = "Розетки в квартире",
					description = "Группа розеток в квартире, мощность 2.5 кВт",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 2.5 кВт, Напряжение: 220 В, Тип сети: Однофазная, Длина: 15 м, Материал: Медь, Прокладка: Открытая, cosφ: 0.9, Потери: 3%, Автомат: Тип C",
					resultSummary = "Ток: 12.6 А, Сечение: 4 мм² (медь), Автомат: 16 А (тип C), Потери: 0.8%"
				),
				UsageExample(
					id = "electrical_lighting",
					title = "Освещение в доме",
					description = "Группа освещения, мощность 0.5 кВт",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 0.5 кВт, Напряжение: 220 В, Тип сети: Однофазная, Длина: 25 м, Материал: Медь, Прокладка: Открытая, cosφ: 0.95, Потери: 3%, Автомат: Тип C",
					resultSummary = "Ток: 2.4 А, Сечение: 1.5 мм² (медь), Автомат: 6 А (тип C), Потери: 0.5%"
				),
				UsageExample(
					id = "electrical_stove",
					title = "Электрическая плита",
					description = "Электрическая плита мощностью 7.5 кВт",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 7.5 кВт, Напряжение: 220 В, Тип сети: Однофазная, Длина: 8 м, Материал: Медь, Прокладка: Открытая, cosφ: 0.9, Потери: 3%, Автомат: Тип C",
					resultSummary = "Ток: 37.9 А, Сечение: 10 мм² (медь), Автомат: 50 А (тип C), Потери: 1.2%"
				),
				UsageExample(
					id = "electrical_motor",
					title = "Трёхфазный двигатель",
					description = "Трёхфазный асинхронный двигатель 5.5 кВт",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 5.5 кВт, Напряжение: 380 В, Тип сети: Трёхфазная, Длина: 30 м, Материал: Медь, Прокладка: Открытая, cosφ: 0.85, Потери: 3%, Автомат: Тип D",
					resultSummary = "Ток: 9.8 А, Сечение: 2.5 мм² (медь), Автомат: 16 А (тип D), Потери: 2.1%"
				),
				UsageExample(
					id = "electrical_aluminum",
					title = "Алюминиевый кабель для дома",
					description = "Вводной кабель в частный дом, мощность 10 кВт",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 10 кВт, Напряжение: 220 В, Тип сети: Однофазная, Длина: 50 м, Материал: Алюминий, Прокладка: Открытая, cosφ: 0.9, Потери: 3%, Автомат: Тип C",
					resultSummary = "Ток: 50.5 А, Сечение: 16 мм² (алюминий), Автомат: 63 А (тип C), Потери: 2.8%"
				),
				UsageExample(
					id = "electrical_outdoor",
					title = "Освещение на улице",
					description = "Уличное освещение, мощность 1 кВт, длинная линия",
					inputSummary = "Тип: Оба расчёта, Ввод: По мощности, Мощность: 1 кВт, Напряжение: 220 В, Тип сети: Однофазная, Длина: 100 м, Материал: Медь, Прокладка: Открытая, cosφ: 0.95, Потери: 3%, Автомат: Тип C",
					resultSummary = "Ток: 4.8 А, Сечение: 4 мм² (медь), Автомат: 10 А (тип C), Потери: 2.9%"
				)
			)
		)
	}
}


