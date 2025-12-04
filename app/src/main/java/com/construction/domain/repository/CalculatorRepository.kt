package com.construction.domain.repository

import com.construction.domain.model.CalculatorCategory
import com.construction.domain.model.CalculatorDefinition
import com.construction.domain.model.InputFieldDefinition
import com.construction.domain.model.InputFieldType
import com.construction.domain.model.ResultFieldDefinition
import com.construction.domain.model.UsageExample

/**
 * Static in-memory repository for construction calculators.
 * Provides access to calculator categories and definitions.
 */
object CalculatorRepository {
	
	// Category IDs
	private const val CATEGORY_FINISHING = "finishing_interior"
	private const val CATEGORY_STRUCTURES = "structures_concrete"
	private const val CATEGORY_ENGINEERING = "engineering_systems"
	private const val CATEGORY_METAL_ELECTRICITY = "metal_electricity"
	
	/**
	 * Returns all available calculator categories.
	 */
	fun getCategories(): List<CalculatorCategory> {
		return listOf(
			CalculatorCategory(
				id = CATEGORY_FINISHING,
				name = "Отделка и интерьер",
				description = "Калькуляторы для расчёта материалов для внутренней отделки помещений"
			),
			CalculatorCategory(
				id = CATEGORY_STRUCTURES,
				name = "Конструкции и бетон",
				description = "Калькуляторы для расчёта материалов для строительных конструкций и бетонных работ"
			),
			CalculatorCategory(
				id = CATEGORY_ENGINEERING,
				name = "Инженерные системы",
				description = "Калькуляторы для расчёта параметров инженерных систем"
			),
			CalculatorCategory(
				id = CATEGORY_METAL_ELECTRICITY,
				name = "Металл и электрика",
				description = "Калькуляторы для расчёта металлических конструкций и электрических систем"
			)
		)
	}
	
	/**
	 * Returns all available calculators.
	 */
	fun getCalculators(): List<CalculatorDefinition> {
		return listOf(
			// Finishing & Interior (9 calculators)
			createWallpaperCalculator(),
			createPaintCalculator(),
			createTileAdhesiveCalculator(),
			createPuttyCalculator(),
			createPrimerCalculator(),
			createPlasterCalculator(),
			createWallAreaCalculator(),
			createTileCalculator(),
			createLaminateCalculator(),
			
			// Structures & Concrete (6 calculators)
			createFoundationCalculator(),
			createConcreteCalculator(),
			createRoofCalculator(),
			createBrickBlocksCalculator(),
			createStairsCalculator(),
			createGravelCalculator(),
			
			// Engineering Systems (3 calculators)
			createVentilationCalculator(),
			createHeatedFloorCalculator(),
			createWaterPipesCalculator(),
			
			// Metal & Electricity (3 calculators)
			createRebarCalculator(),
			createCableSectionCalculator(),
			createElectricalCalculator()
		)
	}
	
	/**
	 * Returns all calculators belonging to the specified category.
	 */
	fun getCalculatorsByCategory(categoryId: String): List<CalculatorDefinition> {
		return getCalculators().filter { it.categoryId == categoryId }
	}
	
	/**
	 * Returns a calculator by its unique ID, or null if not found.
	 */
	fun getCalculatorById(id: String): CalculatorDefinition? {
		return getCalculators().firstOrNull { it.id == id }
	}
	
	// Finishing & Interior Calculators
	
	private fun createWallpaperCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "wallpaper",
			categoryId = CATEGORY_FINISHING,
			name = "Калькулятор количества обоев",
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
					id = "foundation_length",
					label = "Длина фундамента",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Общая длина фундамента"
				),
				InputFieldDefinition(
					id = "foundation_width",
					label = "Ширина фундамента",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина фундамента"
				),
				InputFieldDefinition(
					id = "foundation_height",
					label = "Высота фундамента",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота фундамента"
				),
				InputFieldDefinition(
					id = "rebar_diameter",
					label = "Диаметр арматуры",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 12.0,
					hint = "Диаметр арматурных стержней"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "concrete_volume",
					label = "Объём бетона",
					unit = "м³"
				),
				ResultFieldDefinition(
					id = "rebar_mass",
					label = "Масса арматуры",
					unit = "кг"
				),
				ResultFieldDefinition(
					id = "formwork_area",
					label = "Площадь опалубки",
					unit = "м²"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "foundation_garage",
					title = "Ленточный фундамент под гараж",
					description = "Расчёт материалов для ленточного фундамента под гараж 6×4 м.",
					inputSummary = "Длина 20 м, ширина 0,4 м, высота 0,5 м, арматура 12 мм",
					resultSummary = "Бетон 4 м³, арматура 400 кг, опалубка 20 м²"
				),
				UsageExample(
					id = "foundation_house",
					title = "Фундамент под частный дом",
					description = "Расчёт материалов для ленточного фундамента под дом 10×8 м.",
					inputSummary = "Длина 36 м, ширина 0,5 м, высота 0,6 м, арматура 12 мм",
					resultSummary = "Бетон 10,8 м³, арматура 1080 кг, опалубка 43,2 м²"
				),
				UsageExample(
					id = "foundation_slab",
					title = "Плитный фундамент",
					description = "Расчёт материалов для плитного фундамента под дом.",
					inputSummary = "Длина 10 м, ширина 8 м, высота 0,3 м, арматура 14 мм",
					resultSummary = "Бетон 24 м³, арматура 2400 кг, опалубка 52 м²"
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
					type = InputFieldType.INTEGER,
					defaultValue = 200.0,
					hint = "Марка бетона (М100, М150, М200, М250, М300, М400)"
				),
				InputFieldDefinition(
					id = "cement_ratio",
					label = "Пропорция цемента",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 1.0,
					hint = "Пропорция цемента (обычно 1)"
				),
				InputFieldDefinition(
					id = "sand_ratio",
					label = "Пропорция песка",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 2.5,
					hint = "Пропорция песка (для М200 обычно 2.5)"
				),
				InputFieldDefinition(
					id = "gravel_ratio",
					label = "Пропорция щебня",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 4.5,
					hint = "Пропорция щебня (для М200 обычно 4.5)"
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
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "concrete_foundation",
					title = "Фундамент 10 м³ М200",
					description = "Расчёт компонентов бетонной смеси для фундамента дома.",
					inputSummary = "Объём 10 м³, марка М200, пропорции 1:2.5:4.5, В/Ц 0.5",
					resultSummary = "Цемент 1750 кг, песок 5000 кг, щебень 7875 кг, вода 875 л"
				),
				UsageExample(
					id = "concrete_floor",
					title = "Стяжка пола 5 м³ М150",
					description = "Расчёт компонентов бетона для стяжки пола.",
					inputSummary = "Объём 5 м³, марка М150, пропорции 1:3:5, В/Ц 0.6",
					resultSummary = "Цемент 778 кг, песок 2667 кг, щебень 3889 кг, вода 467 л"
				),
				UsageExample(
					id = "concrete_slab",
					title = "Перекрытие 15 м³ М300",
					description = "Расчёт компонентов бетона для перекрытия между этажами.",
					inputSummary = "Объём 15 м³, марка М300, пропорции 1:1.5:3, В/Ц 0.45",
					resultSummary = "Цемент 3818 кг, песок 6545 кг, щебень 11455 кг, вода 1718 л"
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
					id = "roof_length",
					label = "Длина крыши",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина крыши"
				),
				InputFieldDefinition(
					id = "roof_width",
					label = "Ширина крыши",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Ширина крыши"
				),
				InputFieldDefinition(
					id = "roof_angle",
					label = "Угол наклона",
					unit = "°",
					type = InputFieldType.NUMBER,
					defaultValue = 30.0,
					hint = "Угол наклона крыши в градусах"
				),
				InputFieldDefinition(
					id = "material_weight",
					label = "Вес материала",
					unit = "кг/м²",
					type = InputFieldType.NUMBER,
					defaultValue = 5.0,
					hint = "Вес кровельного материала на квадратный метр"
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
					id = "total_weight",
					label = "Общий вес",
					unit = "кг"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "roof_garage",
					title = "Крыша гаража",
					description = "Расчёт кровельных материалов для односкатной крыши гаража.",
					inputSummary = "Длина 6 м, ширина 4 м, угол 15°, вес материала 5 кг/м², запас 10%",
					resultSummary = "Площадь 24,8 м², материала 27,3 м², вес 124 кг"
				),
				UsageExample(
					id = "roof_house",
					title = "Двускатная крыша дома",
					description = "Расчёт кровельных материалов для двускатной крыши частного дома.",
					inputSummary = "Длина 10 м, ширина 8 м, угол 30°, вес материала 6 кг/м², запас 10%",
					resultSummary = "Площадь 92,4 м², материала 101,6 м², вес 554 кг"
				),
				UsageExample(
					id = "roof_steep",
					title = "Крутая крыша",
					description = "Расчёт материалов для крутой крыши с большим углом наклона.",
					inputSummary = "Длина 12 м, ширина 6 м, угол 45°, вес материала 7 кг/м², запас 10%",
					resultSummary = "Площадь 101,8 м², материала 112 м², вес 713 кг"
				)
			)
		)
	}
	
	private fun createBrickBlocksCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "brick_blocks",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор кирпича и блоков",
			shortDescription = "Количество кирпичей, газоблоков и пеноблоков",
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
					id = "wall_thickness",
					label = "Толщина стены",
					unit = "м",
					type = InputFieldType.LENGTH,
					defaultValue = 0.4,
					hint = "Толщина стены"
				),
				InputFieldDefinition(
					id = "material_type",
					label = "Тип материала",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "1 - Кирпич, 2 - Газоблок, 3 - Пеноблок"
				),
				InputFieldDefinition(
					id = "block_length",
					label = "Длина блока",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 20.0,
					hint = "Длина одного блока"
				),
				InputFieldDefinition(
					id = "block_width",
					label = "Ширина блока",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 30.0,
					hint = "Ширина одного блока"
				),
				InputFieldDefinition(
					id = "block_height",
					label = "Высота блока",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 20.0,
					hint = "Высота одного блока"
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
					id = "block_count",
					label = "Количество блоков",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "wall_volume",
					label = "Объём стены",
					unit = "м³"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "brick_wall",
					title = "Стена из кирпича",
					description = "Расчёт количества кирпича для возведения стены.",
					inputSummary = "Длина 10 м, высота 2,5 м, толщина 0,4 м, кирпич 20×30×20 см, запас 5%",
					resultSummary = "Потребуется 1667 кирпичей, объём стены 10 м³"
				),
				UsageExample(
					id = "gas_block_house",
					title = "Дом из газоблоков",
					description = "Расчёт газоблоков для строительства наружных стен дома.",
					inputSummary = "Длина 40 м, высота 3 м, толщина 0,4 м, блок 20×30×20 см, запас 5%",
					resultSummary = "Потребуется 4200 блоков, объём стены 48 м³"
				),
				UsageExample(
					id = "foam_block_garage",
					title = "Гараж из пеноблоков",
					description = "Расчёт пеноблоков для строительства гаража.",
					inputSummary = "Длина 20 м, высота 2,5 м, толщина 0,3 м, блок 20×30×20 см, запас 5%",
					resultSummary = "Потребуется 1313 блоков, объём стены 15 м³"
				)
			)
		)
	}
	
	private fun createStairsCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "stairs",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор лестницы",
			shortDescription = "Угол наклона, количество ступеней, высота и длина пролёта",
			inputFields = listOf(
				InputFieldDefinition(
					id = "floor_height",
					label = "Высота этажа",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Высота от пола до пола следующего этажа"
				),
				InputFieldDefinition(
					id = "step_height",
					label = "Высота ступени",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 17.0,
					hint = "Высота одной ступени"
				),
				InputFieldDefinition(
					id = "step_width",
					label = "Ширина ступени",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 30.0,
					hint = "Ширина проступи"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "steps_count",
					label = "Количество ступеней",
					unit = "шт"
				),
				ResultFieldDefinition(
					id = "flight_length",
					label = "Длина пролёта",
					unit = "м"
				),
				ResultFieldDefinition(
					id = "angle",
					label = "Угол наклона",
					unit = "°"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "stairs_standard",
					title = "Стандартная лестница на второй этаж",
					description = "Расчёт параметров лестницы для частного дома с высотой этажа 3 м.",
					inputSummary = "Высота этажа 3 м, высота ступени 17 см, ширина ступени 30 см",
					resultSummary = "18 ступеней, длина пролёта 5,4 м, угол наклона 29,5°"
				),
				UsageExample(
					id = "stairs_compact",
					title = "Компактная лестница",
					description = "Расчёт лестницы для помещения с ограниченным пространством.",
					inputSummary = "Высота этажа 2,7 м, высота ступени 18 см, ширина ступени 25 см",
					resultSummary = "15 ступеней, длина пролёта 3,75 м, угол наклона 35,8°"
				),
				UsageExample(
					id = "stairs_comfortable",
					title = "Комфортная лестница",
					description = "Расчёт лестницы с оптимальными параметрами для комфортного подъёма.",
					inputSummary = "Высота этажа 3,2 м, высота ступени 16 см, ширина ступени 32 см",
					resultSummary = "20 ступеней, длина пролёта 6,4 м, угол наклона 26,6°"
				)
			)
		)
	}
	
	private fun createGravelCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "gravel",
			categoryId = CATEGORY_STRUCTURES,
			name = "Калькулятор щебня",
			shortDescription = "Количество щебня для фундамента, дорожек, отмостки и подсыпки",
			inputFields = listOf(
				InputFieldDefinition(
					id = "area",
					label = "Площадь",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь для засыпки щебнем"
				),
				InputFieldDefinition(
					id = "layer_thickness",
					label = "Толщина слоя",
					unit = "см",
					type = InputFieldType.LENGTH,
					defaultValue = 20.0,
					hint = "Толщина слоя щебня"
				),
				InputFieldDefinition(
					id = "gravel_density",
					label = "Плотность щебня",
					unit = "кг/м³",
					type = InputFieldType.NUMBER,
					defaultValue = 1500.0,
					hint = "Плотность щебня"
				),
				InputFieldDefinition(
					id = "compaction_coefficient",
					label = "Коэффициент уплотнения",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 1.3,
					hint = "Коэффициент уплотнения при трамбовке"
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
					label = "Масса щебня",
					unit = "кг"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "gravel_foundation",
					title = "Подсыпка под фундамент",
					description = "Расчёт щебня для подсыпки под ленточный фундамент.",
					inputSummary = "Площадь 20 м², толщина слоя 20 см, плотность 1500 кг/м³, коэффициент 1,3",
					resultSummary = "Объём 5,2 м³, масса 7800 кг"
				),
				UsageExample(
					id = "gravel_path",
					title = "Дорожка из щебня",
					description = "Расчёт щебня для устройства садовой дорожки.",
					inputSummary = "Площадь 15 м², толщина слоя 15 см, плотность 1500 кг/м³, коэффициент 1,3",
					resultSummary = "Объём 2,9 м³, масса 4350 кг"
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
					hint = "Высота помещения"
				),
				InputFieldDefinition(
					id = "air_exchange_rate",
					label = "Кратность воздухообмена",
					unit = "раз/ч",
					type = InputFieldType.NUMBER,
					defaultValue = 1.0,
					hint = "Количество полных замен воздуха в час"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "airflow_rate",
					label = "Производительность",
					unit = "м³/ч"
				),
				ResultFieldDefinition(
					id = "room_volume",
					label = "Объём помещения",
					unit = "м³"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "ventilation_living_room",
					title = "Вентиляция гостиной",
					description = "Расчёт производительности вентиляции для жилой комнаты.",
					inputSummary = "Длина 5 м, ширина 4 м, высота 2,7 м, кратность 1 раз/ч",
					resultSummary = "Производительность 54 м³/ч, объём помещения 54 м³"
				),
				UsageExample(
					id = "ventilation_kitchen",
					title = "Вентиляция кухни",
					description = "Расчёт вентиляции для кухни с повышенной кратностью воздухообмена.",
					inputSummary = "Длина 4 м, ширина 3 м, высота 2,7 м, кратность 3 раз/ч",
					resultSummary = "Производительность 97,2 м³/ч, объём помещения 32,4 м³"
				),
				UsageExample(
					id = "ventilation_bathroom",
					title = "Вентиляция ванной",
					description = "Расчёт вентиляции для ванной комнаты с высокой влажностью.",
					inputSummary = "Длина 2,5 м, ширина 2 м, высота 2,5 м, кратность 5 раз/ч",
					resultSummary = "Производительность 62,5 м³/ч, объём помещения 12,5 м³"
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
					label = "Площадь пола",
					unit = "м²",
					type = InputFieldType.AREA,
					hint = "Площадь обогреваемого пола"
				),
				InputFieldDefinition(
					id = "power_per_sqm",
					label = "Мощность на м²",
					unit = "Вт/м²",
					type = InputFieldType.POWER,
					defaultValue = 150.0,
					hint = "Мощность нагревательного элемента на квадратный метр"
				),
				InputFieldDefinition(
					id = "usage_hours",
					label = "Часов работы в день",
					unit = "ч",
					type = InputFieldType.NUMBER,
					defaultValue = 8.0,
					hint = "Количество часов работы в сутки"
				),
				InputFieldDefinition(
					id = "electricity_price",
					label = "Цена электроэнергии",
					unit = "руб/кВт·ч",
					type = InputFieldType.NUMBER,
					defaultValue = 5.0,
					hint = "Стоимость одного киловатт-часа"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "total_power",
					label = "Общая мощность",
					unit = "кВт"
				),
				ResultFieldDefinition(
					id = "daily_consumption",
					label = "Суточное потребление",
					unit = "кВт·ч"
				),
				ResultFieldDefinition(
					id = "monthly_cost",
					label = "Месячные затраты",
					unit = "руб"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "heated_floor_bathroom",
					title = "Тёплый пол в ванной",
					description = "Расчёт мощности и затрат на электрический тёплый пол в ванной комнате.",
					inputSummary = "Площадь 6 м², мощность 150 Вт/м², работа 8 ч/день, цена 5 руб/кВт·ч",
					resultSummary = "Мощность 0,9 кВт, потребление 7,2 кВт·ч/день, затраты 1080 руб/мес"
				),
				UsageExample(
					id = "heated_floor_kitchen",
					title = "Тёплый пол на кухне",
					description = "Расчёт тёплого пола для кухни среднего размера.",
					inputSummary = "Площадь 12 м², мощность 150 Вт/м², работа 6 ч/день, цена 5 руб/кВт·ч",
					resultSummary = "Мощность 1,8 кВт, потребление 10,8 кВт·ч/день, затраты 1620 руб/мес"
				),
				UsageExample(
					id = "heated_floor_living_room",
					title = "Тёплый пол в гостиной",
					description = "Расчёт тёплого пола для большой гостиной с постоянным обогревом.",
					inputSummary = "Площадь 20 м², мощность 150 Вт/м², работа 12 ч/день, цена 5 руб/кВт·ч",
					resultSummary = "Мощность 3 кВт, потребление 36 кВт·ч/день, затраты 5400 руб/мес"
				)
			)
		)
	}
	
	private fun createWaterPipesCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "water_pipes",
			categoryId = CATEGORY_ENGINEERING,
			name = "Калькулятор водопроводных труб",
			shortDescription = "Диаметр, пропускная способность и гидравлические параметры",
			inputFields = listOf(
				InputFieldDefinition(
					id = "pipe_diameter",
					label = "Диаметр трубы",
					unit = "мм",
					type = InputFieldType.LENGTH,
					hint = "Внутренний диаметр трубы"
				),
				InputFieldDefinition(
					id = "pipe_length",
					label = "Длина трубы",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина трубопровода"
				),
				InputFieldDefinition(
					id = "flow_velocity",
					label = "Скорость потока",
					unit = "м/с",
					type = InputFieldType.NUMBER,
					defaultValue = 1.5,
					hint = "Скорость движения воды в трубе"
				),
				InputFieldDefinition(
					id = "roughness",
					label = "Шероховатость",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 0.1,
					hint = "Шероховатость внутренней поверхности трубы"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "flow_rate",
					label = "Пропускная способность",
					unit = "л/мин"
				),
				ResultFieldDefinition(
					id = "pressure_loss",
					label = "Потери давления",
					unit = "Па"
				),
				ResultFieldDefinition(
					id = "pipe_area",
					label = "Площадь сечения",
					unit = "м²"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "water_pipes_apartment",
					title = "Водопровод в квартире",
					description = "Расчёт параметров водопроводной трубы для квартиры.",
					inputSummary = "Диаметр 20 мм, длина 15 м, скорость потока 1,5 м/с, шероховатость 0,1 мм",
					resultSummary = "Пропускная способность 28,3 л/мин, потери давления 1688 Па"
				),
				UsageExample(
					id = "water_pipes_house",
					title = "Водопровод в частном доме",
					description = "Расчёт водопровода для частного дома с большей длиной.",
					inputSummary = "Диаметр 25 мм, длина 30 м, скорость потока 1,5 м/с, шероховатость 0,1 мм",
					resultSummary = "Пропускная способность 44,2 л/мин, потери давления 1350 Па"
				),
				UsageExample(
					id = "water_pipes_high_flow",
					title = "Труба с высокой пропускной способностью",
					description = "Расчёт трубы для системы с высоким расходом воды.",
					inputSummary = "Диаметр 32 мм, длина 20 м, скорость потока 2 м/с, шероховатость 0,1 мм",
					resultSummary = "Пропускная способность 96,5 л/мин, потери давления 2500 Па"
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
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "1 - Фундамент, 2 - Плита, 3 - Стена, 4 - Колонна"
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
					type = InputFieldType.LENGTH,
					defaultValue = 20.0,
					hint = "Расстояние между арматурными стержнями (обычно 15-25 см)"
				),
				InputFieldDefinition(
					id = "rebar_diameter",
					label = "Диаметр арматуры",
					unit = "мм",
					type = InputFieldType.LENGTH,
					defaultValue = 12.0,
					hint = "Диаметр арматурного стержня (обычно 8-16 мм)"
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
			shortDescription = "Расчёт оптимального сечения по мощности, длине линии и току",
			inputFields = listOf(
				InputFieldDefinition(
					id = "power",
					label = "Мощность",
					unit = "кВт",
					type = InputFieldType.POWER,
					hint = "Мощность нагрузки"
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
					id = "power_factor",
					label = "Коэффициент мощности",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 0.9,
					hint = "Коэффициент мощности нагрузки"
				),
				InputFieldDefinition(
					id = "voltage_drop_percent",
					label = "Допустимое падение напряжения",
					unit = "%",
					type = InputFieldType.PERCENT,
					defaultValue = 5.0,
					hint = "Максимальное допустимое падение напряжения"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "current",
					label = "Ток",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "cable_section",
					label = "Сечение кабеля",
					unit = "мм²"
				),
				ResultFieldDefinition(
					id = "voltage_drop",
					label = "Падение напряжения",
					unit = "В"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "cable_section_room",
					title = "Розеточная линия на комнату",
					description = "Расчёт сечения кабеля для розеточной группы в комнате.",
					inputSummary = "Мощность 3 кВт, напряжение 220 В, длина 15 м, коэффициент мощности 0,9, падение 5%",
					resultSummary = "Ток 15,2 А, сечение кабеля 2,3 мм², падение напряжения 11 В"
				),
				UsageExample(
					id = "cable_section_kitchen",
					title = "Линия для кухонной техники",
					description = "Расчёт кабеля для подключения мощной кухонной техники.",
					inputSummary = "Мощность 5 кВт, напряжение 220 В, длина 10 м, коэффициент мощности 0,9, падение 5%",
					resultSummary = "Ток 25,3 А, сечение кабеля 3,8 мм², падение напряжения 11 В"
				),
				UsageExample(
					id = "cable_section_heater",
					title = "Кабель для электрического котла",
					description = "Расчёт кабеля для подключения электрического отопительного котла.",
					inputSummary = "Мощность 9 кВт, напряжение 220 В, длина 20 м, коэффициент мощности 1,0, падение 5%",
					resultSummary = "Ток 40,9 А, сечение кабеля 6,1 мм², падение напряжения 11 В"
				)
			)
		)
	}
	
	private fun createElectricalCalculator(): CalculatorDefinition {
		return CalculatorDefinition(
			id = "electrical",
			categoryId = CATEGORY_METAL_ELECTRICITY,
			name = "Калькулятор электрики",
			shortDescription = "Расчёт сечения кабеля и мощности автоматов",
			inputFields = listOf(
				InputFieldDefinition(
					id = "total_power",
					label = "Общая мощность",
					unit = "кВт",
					type = InputFieldType.POWER,
					hint = "Суммарная мощность всех потребителей"
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
					id = "phase_count",
					label = "Количество фаз",
					unit = null,
					type = InputFieldType.INTEGER,
					defaultValue = 1.0,
					hint = "Количество фаз (1 или 3)"
				),
				InputFieldDefinition(
					id = "cable_length",
					label = "Длина кабеля",
					unit = "м",
					type = InputFieldType.LENGTH,
					hint = "Длина кабеля от щита до нагрузки"
				),
				InputFieldDefinition(
					id = "safety_factor",
					label = "Коэффициент запаса",
					unit = null,
					type = InputFieldType.NUMBER,
					defaultValue = 1.25,
					hint = "Коэффициент запаса для автомата"
				)
			),
			resultFields = listOf(
				ResultFieldDefinition(
					id = "current",
					label = "Расчётный ток",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "breaker_current",
					label = "Номинальный ток автомата",
					unit = "А"
				),
				ResultFieldDefinition(
					id = "cable_section",
					label = "Сечение кабеля",
					unit = "мм²"
				)
			),
			usageExamples = listOf(
				UsageExample(
					id = "electrical_apartment",
					title = "Вводной кабель на квартиру",
					description = "Расчёт вводного кабеля и автомата для квартиры.",
					inputSummary = "Мощность 10 кВт, напряжение 220 В, 1 фаза, длина 25 м, коэффициент запаса 1,25",
					resultSummary = "Ток 45,5 А, автомат 57 А, сечение кабеля 6,8 мм²"
				),
				UsageExample(
					id = "electrical_house",
					title = "Вводной кабель на частный дом",
					description = "Расчёт вводного кабеля для частного дома с трёхфазным подключением.",
					inputSummary = "Мощность 15 кВт, напряжение 380 В, 3 фазы, длина 30 м, коэффициент запаса 1,25",
					resultSummary = "Ток 22,8 А, автомат 29 А, сечение кабеля 3,4 мм²"
				),
				UsageExample(
					id = "electrical_stove",
					title = "Варочная панель",
					description = "Расчёт кабеля и автомата для подключения электрической варочной панели.",
					inputSummary = "Мощность 7 кВт, напряжение 220 В, 1 фаза, длина 8 м, коэффициент запаса 1,25",
					resultSummary = "Ток 31,8 А, автомат 40 А, сечение кабеля 4,8 мм²"
				)
			)
		)
	}
}


