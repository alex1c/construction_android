package com.construction.domain

import com.construction.domain.repository.CalculatorRepository

/**
 * Complete documentation of all calculators in the construction app.
 * This serves as a single source of truth for calculator specifications,
 * validation rules, and expected behavior.
 *
 * All calculators are defined in [CalculatorRepository] and calculated by [com.construction.domain.engine.CalculatorEngine].
 *
 * ## Validation Rules (Common)
 *
 * - **Length/Area/Volume**: Must be > 0, max reasonable limits:
 *   - Room dimensions: 0.1 m to 100 m
 *   - Areas: 0.01 m² to 10000 m²
 *   - Volumes: 0.001 m³ to 10000 m³
 *
 * - **Percentages**: 0% to 100%
 *
 * - **Counts/Integers**: Must be > 0, max 1000000
 *
 * - **Material consumption**: Must be > 0
 *
 * - **Rounding**: Material quantities (rolls, packs, tiles) are rounded UP to avoid shortage
 *
 * ## Calculator List
 *
 * ### Category: Отделка и интерьер (Finishing & Interior)
 *
 * 1. **wallpaper** - Калькулятор количества обоев
 *    - Inputs: room_length (м), room_width (м), room_height (м), roll_width (м, default 0.53), roll_length (м, default 10.0)
 *    - Outputs: rolls_count (шт, rounded UP)
 *    - Formula: wall_area = 2 * (length + width) * height; rolls = (wall_area / roll_area) * 1.1
 *
 * 2. **paint** - Калькулятор краски
 *    - Inputs: wall_area (м²), paint_consumption (кг/м², default 0.15), coats_count (default 2)
 *    - Outputs: paint_mass (кг)
 *    - Formula: mass = area * consumption * coats
 *
 * 3. **tile_adhesive** - Калькулятор плиточного клея
 *    - Inputs: tile_area (м²), adhesive_consumption (кг/м², default 4.0), layer_thickness (мм, default 3.0)
 *    - Outputs: adhesive_mass (кг)
 *    - Formula: adjusted_consumption = base_consumption * (thickness / 3.0); mass = area * adjusted_consumption
 *
 * 4. **putty** - Калькулятор шпатлёвки
 *    - Inputs: wall_area (м²), putty_consumption (кг/м², default 1.2), layer_thickness (мм, default 2.0)
 *    - Outputs: putty_mass (кг)
 *    - Formula: adjusted_consumption = base_consumption * (thickness / 2.0); mass = area * adjusted_consumption
 *
 * 5. **primer** - Калькулятор грунтовки
 *    - Inputs: surface_area (м²), primer_consumption (л/м², default 0.2), coats_count (default 1)
 *    - Outputs: primer_volume (л)
 *    - Formula: volume = area * consumption * coats
 *
 * 6. **plaster** - Калькулятор штукатурки
 *    - Inputs: wall_area (м²), plaster_consumption (кг/м², default 8.5), layer_thickness (мм, default 10.0)
 *    - Outputs: plaster_mass (кг)
 *    - Formula: adjusted_consumption = base_consumption * (thickness / 10.0); mass = area * adjusted_consumption
 *
 * 7. **wall_area** - Калькулятор площади стен
 *    - Inputs: room_length (м), room_width (м), room_height (м), openings_area (м², default 0.0), waste_percent (%, default 10.0)
 *    - Outputs: total_area (м²), area_with_waste (м²)
 *    - Formula: perimeter = 2 * (length + width); total = perimeter * height - openings; with_waste = total * (1 + waste/100)
 *    - **Consistency**: This formula must match internal area calculations in wallpaper, paint, plaster, putty calculators
 *
 * 8. **tile** - Калькулятор плитки
 *    - Inputs: surface_area (м²), tile_length (см), tile_width (см), waste_percent (%, default 10.0)
 *    - Outputs: tile_count (шт, rounded UP)
 *    - Formula: tile_area = (length/100) * (width/100); count = (surface_area / tile_area) * (1 + waste/100)
 *
 * 9. **laminate** - Калькулятор ламината
 *    - Inputs: room_length (м), room_width (м), laminate_length (м, default 1.3), laminate_width (м, default 0.2), pack_count (шт, default 8), waste_percent (%, default 5.0)
 *    - Outputs: pack_count (шт, rounded UP), total_area (м²)
 *    - Formula: room_area = length * width; board_area = laminate_length * laminate_width; boards = (room_area / board_area) * (1 + waste/100); packs = ceil(boards / pack_count)
 *
 * ### Category: Конструкции и бетон (Structures & Concrete)
 *
 * 10. **foundation** - Калькулятор фундамента
 *     - Inputs: foundation_length (м), foundation_width (м), foundation_height (м), rebar_diameter (мм, default 12.0)
 *     - Outputs: concrete_volume (м³), rebar_mass (кг), formwork_area (м²)
 *     - Formula: volume = length * width * height; rebar ≈ 100 kg/m³; formwork = 2 * (length + width) * height
 *     - **Consistency**: concrete_volume should match concrete calculator input
 *
 * 11. **concrete** - Калькулятор бетона
 *     - Inputs: concrete_volume (м³), cement_grade (default 400), concrete_grade (default 200)
 *     - Outputs: cement_mass (кг), sand_mass (кг), gravel_mass (кг), water_volume (л)
 *     - Formula: Based on M200 mix (1:2:4:0.5), density ≈ 2400 kg/m³
 *     - **Consistency**: Should match foundation calculator concrete volume
 *
 * 12. **roof** - Калькулятор кровли
 *     - Inputs: roof_length (м), roof_width (м), roof_angle (°, default 30.0), material_weight (кг/м², default 5.0), waste_percent (%, default 10.0)
 *     - Outputs: roof_area (м²), material_area (м²), total_weight (кг)
 *     - Formula: area = length * width / cos(angle_rad); material = area * (1 + waste/100); weight = area * material_weight
 *
 * 13. **brick_blocks** - Калькулятор кирпича и блоков
 *     - Inputs: wall_length (м), wall_height (м), wall_thickness (м, default 0.4), material_type (1-3), block_length (см, default 20.0), block_width (см, default 30.0), block_height (см, default 20.0), waste_percent (%, default 5.0)
 *     - Outputs: block_count (шт, rounded UP), wall_volume (м³)
 *     - Formula: wall_volume = length * height * thickness; block_volume = (length/100) * (width/100) * (height/100); count = (wall_volume / block_volume) * (1 + waste/100)
 *
 * 14. **stairs** - Калькулятор лестницы
 *     - Inputs: floor_height (м), step_height (см, default 17.0), step_width (см, default 30.0)
 *     - Outputs: steps_count (шт), flight_length (м), angle (°)
 *     - Formula: steps = floor_height * 100 / step_height; length = steps * step_width / 100; angle = atan(step_height / step_width) * 180/π
 *
 * 15. **gravel** - Калькулятор щебня
 *     - Inputs: area (м²), layer_thickness (см, default 20.0), gravel_density (кг/м³, default 1500.0), compaction_coefficient (default 1.3)
 *     - Outputs: gravel_volume (м³), gravel_mass (кг)
 *     - Formula: volume = area * (thickness/100) * compaction; mass = volume * density
 *
 * ### Category: Инженерные системы (Engineering Systems)
 *
 * 16. **ventilation** - Калькулятор вентиляции
 *     - Inputs: room_length (м), room_width (м), room_height (м), air_exchange_rate (раз/ч, default 1.0)
 *     - Outputs: airflow_rate (м³/ч), room_volume (м³)
 *     - Formula: volume = length * width * height; airflow = volume * exchange_rate
 *
 * 17. **heated_floor** - Калькулятор тёплого пола
 *     - Inputs: floor_area (м²), power_per_sqm (Вт/м², default 150.0), usage_hours (ч, default 8.0), electricity_price (руб/кВт·ч, default 5.0)
 *     - Outputs: total_power (кВт), daily_consumption (кВт·ч), monthly_cost (руб)
 *     - Formula: power = (area * power_per_sqm) / 1000; consumption = power * hours; cost = consumption * 30 * price
 *
 * 18. **water_pipes** - Калькулятор водопроводных труб
 *     - Inputs: pipe_diameter (мм), pipe_length (м), flow_velocity (м/с, default 1.5), roughness (мм, default 0.1)
 *     - Outputs: flow_rate (л/мин), pressure_loss (Па), pipe_area (м²)
 *     - Formula: area = π * (diameter/2)²; flow = area * velocity * 1000 * 60; pressure_loss ≈ (0.02 * length * velocity²) / (2 * diameter) * 1000
 *
 * ### Category: Металл и электрика (Metal & Electricity)
 *
 * 19. **rebar** - Калькулятор арматуры
 *     - Inputs: rebar_diameter (мм), rebar_length (м), rebar_count (шт), steel_density (кг/м³, default 7850.0)
 *     - Outputs: total_length (м), total_mass (кг), mass_per_meter (кг/м)
 *     - Formula: total_length = length * count; area = π * (diameter/2)²; mass_per_m = area * density; total_mass = total_length * mass_per_m
 *
 * 20. **cable_section** - Калькулятор сечения кабеля
 *     - Inputs: power (кВт), voltage (В, default 220.0), cable_length (м), power_factor (default 0.9), voltage_drop_percent (%, default 5.0)
 *     - Outputs: current (А), cable_section (мм²), voltage_drop (В)
 *     - Formula: current = (power * 1000) / (voltage * power_factor); section ≈ (current / 10) * 1.5; voltage_drop = current * resistance
 *     - **Consistency**: Must be compatible with electrical calculator for same inputs
 *
 * 21. **electrical** - Калькулятор электрики
 *     - Inputs: total_power (кВт), voltage (В, default 220.0), phase_count (1 or 3, default 1), cable_length (м), safety_factor (default 1.25)
 *     - Outputs: current (А), breaker_current (А), cable_section (мм²)
 *     - Formula: current = (power * 1000) / (voltage * √3 for 3-phase else voltage); breaker = current * safety_factor; section ≈ (current / 10) * 1.5
 *     - **Consistency**: Must be compatible with cable_section calculator for same inputs
 *
 * ## Cross-Calculator Consistency Requirements
 *
 * 1. **Wall Area Consistency**: wall_area calculator must produce the same area as used internally by:
 *    - wallpaper: 2 * (length + width) * height
 *    - paint, plaster, putty: uses wall_area directly
 *
 * 2. **Concrete Consistency**: foundation.concrete_volume should match concrete.calculator input
 *
 * 3. **Electrical Consistency**: For same power, voltage, length:
 *    - cable_section.current ≈ electrical.current (for 1-phase)
 *    - cable_section.cable_section ≈ electrical.cable_section
 *
 * ## Testing Requirements
 *
 * Each calculator should have tests covering:
 * - Normal cases with realistic values
 * - Boundary cases (min/max valid values)
 * - Invalid inputs (negative, zero where not allowed, extreme values)
 * - Rounding behavior (material counts rounded UP)
 * - Cross-calculator consistency
 */
object CalculatorDocumentation {
	
	/**
	 * Returns all calculator IDs in the system.
	 */
	fun getAllCalculatorIds(): List<String> {
		return CalculatorRepository.getCalculators().map { it.id }
	}
	
	/**
	 * Returns the total number of calculators.
	 */
	fun getCalculatorCount(): Int {
		return CalculatorRepository.getCalculators().size
	}
}

