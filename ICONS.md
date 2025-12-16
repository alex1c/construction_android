# Иконки калькуляторов

## Обзор

В приложении добавлена система инфографики с иконками для всех калькуляторов и категорий. Иконки помогают пользователям быстро идентифицировать нужный калькулятор и улучшают визуальное восприятие интерфейса.

## Структура

### CalculatorIcons.kt

Утилитный объект, предоставляющий иконки для:
- **Калькуляторов** - уникальная иконка для каждого из 21 калькулятора
- **Категорий** - иконка для каждой из 4 категорий
- **Цветов категорий** - цветовая схема для визуального разделения

## Иконки калькуляторов

### Отделка и интерьер
- **Обои** (`wallpaper`) - `Icons.Default.Image`
- **Краска** (`paint`) - `Icons.Default.FormatPaint`
- **Плиточный клей** (`tile_adhesive`) - `Icons.Default.Construction`
- **Шпатлёвка** (`putty`) - `Icons.Default.Build`
- **Грунтовка** (`primer`) - `Icons.Default.Layers`
- **Штукатурка** (`plaster`) - `Icons.Default.Home`
- **Площадь стен** (`wall_area`) - `Icons.Default.SquareFoot`
- **Плитка** (`tile`) - `Icons.Default.GridOn`
- **Ламинат** (`laminate`) - `Icons.Default.ViewQuilt`

### Конструкции и бетон
- **Фундамент** (`foundation`) - `Icons.Default.Home`
- **Бетон** (`concrete`) - `Icons.Default.Business`
- **Кровля** (`roof`) - `Icons.Default.Home`
- **Кирпич и блоки** (`brick_blocks`) - `Icons.Default.Home`
- **Лестницы** (`stairs`) - `Icons.Default.TrendingUp`
- **Щебень** (`gravel`) - `Icons.Default.Circle`

### Инженерные системы
- **Вентиляция** (`ventilation`) - `Icons.Default.AcUnit`
- **Тёплый пол** (`heated_floor`) - `Icons.Default.WaterDrop`
- **Водопроводные трубы** (`water_pipes`) - `Icons.Default.Build`

### Металл и электрика
- **Арматура** (`rebar`) - `Icons.Default.Build`
- **Сечение кабеля** (`cable_section`) - `Icons.Default.Power`
- **Электрика** (`electrical`) - `Icons.Default.Power`

## Иконки категорий

- **Отделка и интерьер** - `Icons.Default.Home` (Зелёный: #4CAF50)
- **Конструкции и бетон** - `Icons.Default.Business` (Синий: #2196F3)
- **Инженерные системы** - `Icons.Default.Settings` (Оранжевый: #FF9800)
- **Металл и электрика** - `Icons.Default.Power` (Фиолетовый: #9C27B0)

## Цветовая схема

Каждая категория имеет свой цвет для визуального разделения:

| Категория | Цвет | HEX |
|-----------|------|-----|
| Отделка и интерьер | Зелёный | #4CAF50 |
| Конструкции и бетон | Синий | #2196F3 |
| Инженерные системы | Оранжевый | #FF9800 |
| Металл и электрика | Фиолетовый | #9C27B0 |

## Использование

### В UI компонентах

```kotlin
// Получить иконку калькулятора
val icon = CalculatorIcons.getIcon("wallpaper")

// Получить иконку категории
val categoryIcon = CalculatorIcons.getCategoryIcon("finishing_interior")

// Получить цвет категории
val color = Color(CalculatorIcons.getCategoryColor("finishing_interior"))
```

### Пример отображения

```kotlin
Icon(
    imageVector = CalculatorIcons.getIcon(calculator.id),
    contentDescription = calculator.name,
    modifier = Modifier.size(48.dp),
    tint = Color(CalculatorIcons.getCategoryColor(calculator.categoryId))
)
```

## Где используются иконки

1. **HomeScreen** - Популярные калькуляторы (карточки с иконками)
2. **CategorySection** - Категории с иконками
3. **CalculatorList** - Список калькуляторов с иконками
4. **CalculatorScreen** - Заголовок калькулятора с иконкой

## Технические детали

- Все иконки используют Material Design Icons
- Иконки загружаются из библиотеки `androidx.compose.material:material-icons-extended`
- Размер иконок: 48dp для калькуляторов, 32dp для категорий
- Цвета применяются через `tint` параметр Icon

## Будущие улучшения

- [ ] Добавить кастомные векторные иконки для более точного отображения
- [ ] Добавить анимации при наведении/нажатии
- [ ] Поддержка тёмной темы с адаптивными цветами
- [ ] Иконки для результатов расчётов



