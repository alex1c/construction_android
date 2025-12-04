# Инструкция по сборке release версии для RuStore

## Подготовка к сборке

### 1. Проверка версии
Убедитесь, что версия указана правильно в `app/build.gradle`:
```gradle
versionCode 1
versionName "1.0"
```

### 2. Проверка Package Name
Package name: `com.construction`

### 3. Подпись приложения
Для публикации в RuStore необходимо подписать приложение.

#### Создание keystore (если ещё нет):
```bash
keytool -genkey -v -keystore construction-release.keystore -alias construction -keyalg RSA -keysize 2048 -validity 10000
```

#### Настройка signing config в `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file('construction-release.keystore')
            storePassword 'your-store-password'
            keyAlias 'construction'
            keyPassword 'your-key-password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**⚠️ ВАЖНО:** Не коммитьте keystore и пароли в Git! Добавьте в `.gitignore`:
```
*.keystore
keystore.properties
```

## Сборка APK

### Debug версия (для тестирования):
```bash
./gradlew assembleDebug
```
APK будет в: `app/build/outputs/apk/debug/app-debug.apk`

### Release версия:
```bash
./gradlew assembleRelease
```
APK будет в: `app/build/outputs/apk/release/app-release.apk`

## Сборка AAB (Android App Bundle)

RuStore рекомендует загружать AAB вместо APK:

```bash
./gradlew bundleRelease
```
AAB будет в: `app/build/outputs/bundle/release/app-release.aab`

## Проверка перед загрузкой

### 1. Проверка размера
- APK должен быть разумного размера (< 50 MB рекомендуется)
- AAB обычно меньше APK

### 2. Тестирование release версии
```bash
# Установка на устройство
adb install app/build/outputs/apk/release/app-release.apk

# Или через Android Studio: Build > Build Bundle(s) / APK(s) > Build APK(s)
```

### 3. Проверка функциональности
- [ ] Все калькуляторы работают
- [ ] Поиск работает
- [ ] История работает
- [ ] Шаринг работает
- [ ] Навигация работает
- [ ] Премиум функции отображаются (если включены)

## Загрузка в RuStore

1. Войдите в личный кабинет разработчика RuStore
2. Создайте новое приложение
3. Заполните всю информацию (см. RUSTORE_PUBLICATION.md)
4. Загрузите AAB файл
5. Добавьте скриншоты (минимум 2)
6. Добавьте иконку (512x512)
7. Отправьте на модерацию

## После публикации

1. Обновите URL в `strings.xml`:
   ```xml
   <string name="rustore_url">https://apps.rustore.ru/app/com.construction</string>
   ```
   Замените на реальный URL после публикации.

2. Для следующих версий увеличьте `versionCode` и обновите `versionName`.

## Troubleshooting

### Ошибка подписи
- Проверьте правильность паролей
- Убедитесь, что keystore файл существует
- Проверьте alias

### Ошибка сборки
- Очистите проект: `./gradlew clean`
- Пересоберите: `./gradlew build`

### Большой размер APK
- Включите minify: `minifyEnabled true`
- Проверьте зависимости
- Используйте AAB вместо APK

