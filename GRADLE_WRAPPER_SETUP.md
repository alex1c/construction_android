# Настройка Gradle Wrapper

## Проблема
Файлы Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) отсутствуют в проекте.

## Решение

### Вариант 1: Через Android Studio (Рекомендуется)

1. Откройте проект в Android Studio
2. Android Studio автоматически создаст Gradle Wrapper при первом открытии проекта
3. Или выполните: **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
4. Выберите **Gradle wrapper** и нажмите **Apply**

### Вариант 2: Через командную строку (если установлен Gradle)

Если у вас установлен Gradle глобально:

```bash
gradle wrapper --gradle-version 8.7
```

Это создаст файлы:
- `gradlew` (Linux/Mac)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`

### Вариант 3: Скачать вручную

1. Скачайте `gradle-wrapper.jar` с официального сайта Gradle
2. Поместите его в `gradle/wrapper/gradle-wrapper.jar`
3. Создайте скрипты `gradlew` и `gradlew.bat` (см. примеры ниже)

### Вариант 4: Использовать Android Studio для сборки

Если wrapper недоступен, используйте Android Studio:
- **Build → Make Project** - для компиляции
- **Build → Build Bundle(s) / APK(s)** - для сборки APK/AAB

## После создания wrapper

Проверьте, что файлы созданы:
```bash
# Windows
dir gradlew*

# Linux/Mac
ls -la gradlew*
```

Должны быть файлы:
- `gradlew` (Unix скрипт)
- `gradlew.bat` (Windows скрипт)
- `gradle/wrapper/gradle-wrapper.jar`

## Использование

После создания wrapper, используйте:

```bash
# Windows
.\gradlew.bat :app:compileDebugUnitTestKotlin

# Linux/Mac
./gradlew :app:compileDebugUnitTestKotlin
```

## Текущая конфигурация

Версия Gradle: **8.7** (указана в `gradle/wrapper/gradle-wrapper.properties`)



