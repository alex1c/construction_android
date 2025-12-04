# Настройка подписи приложения (Keystore)

## Обзор

Для публикации приложения в RuStore необходимо подписать APK/AAB файл цифровой подписью. Это делается с помощью keystore файла.

## Создание Keystore

> **Примечание:** Если `keytool` не найден, используйте **Android Studio** (см. `CREATE_KEYSTORE_MANUAL.md`) или установите JDK.

### Вариант 1: Через Android Studio (Рекомендуется)

См. подробную инструкцию в `CREATE_KEYSTORE_MANUAL.md`

### Вариант 2: Использование скрипта (Windows)

1. Запустите `create_keystore.bat`:
   ```cmd
   create_keystore.bat
   ```

2. Скрипт создаст keystore с параметрами по умолчанию:
   - Имя файла: `construction-release.keystore`
   - Alias: `construction`
   - Пароли: `construction123` (⚠️ ОБЯЗАТЕЛЬНО ИЗМЕНИТЕ!)

### Вариант 2: Использование скрипта (Linux/Mac)

1. Сделайте скрипт исполняемым:
   ```bash
   chmod +x create_keystore.sh
   ```

2. Запустите скрипт:
   ```bash
   ./create_keystore.sh
   ```

### Вариант 3: Ручное создание

Выполните команду в терминале:

```bash
keytool -genkey -v -keystore construction-release.keystore -alias construction -keyalg RSA -keysize 2048 -validity 10000
```

Вам будет предложено ввести:
- Пароль для keystore
- Пароль для ключа
- Информацию о разработчике (имя, организация, город, страна)

## Настройка keystore.properties

1. Скопируйте пример файла:
   ```bash
   cp keystore.properties.example keystore.properties
   ```

2. Откройте `keystore.properties` и укажите реальные значения:
   ```properties
   storeFile=construction-release.keystore
   storePassword=ваш-пароль-keystore
   keyAlias=construction
   keyPassword=ваш-пароль-ключа
   ```

3. ⚠️ **ВАЖНО:** Файл `keystore.properties` уже добавлен в `.gitignore` и не будет закоммичен в репозиторий.

## Безопасность

### ⚠️ КРИТИЧЕСКИ ВАЖНО:

1. **НЕ коммитьте keystore файлы в Git!**
   - `construction-release.keystore` уже в `.gitignore`
   - `keystore.properties` уже в `.gitignore`

2. **Храните keystore безопасно:**
   - Сделайте резервную копию keystore файла
   - Сохраните пароли в безопасном месте
   - Без keystore вы НЕ сможете обновлять приложение в RuStore!

3. **Используйте сильные пароли:**
   - Минимум 12 символов
   - Комбинация букв, цифр и символов
   - Не используйте пароли по умолчанию из скрипта!

## Проверка подписи

После создания keystore, проверьте его:

```bash
keytool -list -v -keystore construction-release.keystore
```

## Использование в сборке

После настройки `keystore.properties`, release сборка будет автоматически подписана:

```bash
# Сборка подписанного APK
./gradlew assembleRelease

# Сборка подписанного AAB (рекомендуется для RuStore)
./gradlew bundleRelease
```

## Восстановление keystore

Если вы потеряли keystore:
- ❌ Невозможно восстановить
- ❌ Невозможно обновить существующее приложение в RuStore
- ✅ Можно создать новое приложение с новым package name

**Поэтому ОБЯЗАТЕЛЬНО сделайте резервную копию keystore!**

## Troubleshooting

### Ошибка: "Keystore file does not exist"
- Убедитесь, что `construction-release.keystore` находится в корне проекта
- Проверьте путь в `keystore.properties`

### Ошибка: "Password was incorrect"
- Проверьте пароли в `keystore.properties`
- Убедитесь, что пароли совпадают с теми, что использовались при создании keystore

### Ошибка: "Alias does not exist"
- Проверьте `keyAlias` в `keystore.properties`
- Должно быть: `keyAlias=construction`

## Следующие шаги

1. ✅ Создать keystore (используя скрипт или вручную)
2. ✅ Настроить `keystore.properties`
3. ✅ Проверить, что keystore работает
4. ✅ Собрать release версию
5. ✅ Протестировать подписанный APK/AAB

