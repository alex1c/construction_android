# Исправление проблемы с алиасом keystore

## Проблема
```
Failed to read key construction from store: No key with alias 'construction' found in keystore
```

Keystore файл существует, но не содержит ключ с алиасом `construction`.

## Решение: Пересоздать keystore через Android Studio

### Шаг 1: Удалить старый keystore (опционально, для чистоты)

```powershell
# Создать резервную копию на всякий случай
Copy-Item construction-release.keystore construction-release.keystore.backup

# Удалить старый keystore
Remove-Item construction-release.keystore
```

### Шаг 2: Создать новый keystore через Android Studio

1. Откройте проект в **Android Studio**

2. Перейдите в меню: **Build → Generate Signed Bundle / APK**

3. Выберите **Android App Bundle** (рекомендуется для RuStore) или **APK**

4. Нажмите **Create new...** для создания нового keystore

5. **ВАЖНО:** Заполните форму с ТОЧНЫМИ значениями:

   - **Key store path**: 
     ```
     E:\petProject\construction_android\construction-release.keystore
     ```
     ⚠️ Укажите **ПОЛНЫЙ путь с расширением .keystore** (не просто директорию!)

   - **Password**: `Agrotorg87`
   - **Confirm**: `Agrotorg87`
   
   - **Key alias**: `construction` ⚠️ **ОБЯЗАТЕЛЬНО именно "construction"!**
   - **Key password**: `Agrotorg87`
   - **Confirm**: `Agrotorg87`
   
   - **Validity (years)**: `25` (или `10000` дней)
   
   - **Certificate**:
     - First and Last Name: `Calc1`
     - Organizational Unit: `Development`
     - Organization: `Calc1`
     - City or Locality: `Moscow`
     - State or Province: `Moscow`
     - Country Code: `RU`

6. Нажмите **OK**

7. Keystore будет создан в корне проекта: `construction-release.keystore`

### Шаг 3: Проверить keystore.properties

Убедитесь, что файл `keystore.properties` содержит правильные значения:

```properties
storeFile=construction-release.keystore
storePassword=Agrotorg87
keyAlias=construction
keyPassword=Agrotorg87
```

### Шаг 4: Проверить сборку

После создания keystore попробуйте собрать release:

```bash
./gradlew assembleRelease
```

Или через Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

## Альтернативный способ: Через командную строку (если keytool доступен)

Если у вас установлен JDK и keytool доступен:

```bash
# Удалить старый keystore
del construction-release.keystore

# Создать новый keystore
keytool -genkey -v -keystore construction-release.keystore -alias construction -keyalg RSA -keysize 2048 -validity 10000 -storepass Agrotorg87 -keypass Agrotorg87 -dname "CN=Calc1, OU=Development, O=Calc1, L=Moscow, ST=Moscow, C=RU"
```

## Проверка keystore

После создания, проверьте содержимое keystore:

```bash
keytool -list -v -keystore construction-release.keystore -storepass Agrotorg87
```

Вы должны увидеть:
```
Alias name: construction
...
```

## Важно

- ⚠️ **Алиас ДОЛЖЕН быть точно `construction`** (как указано в keystore.properties)
- ⚠️ **Пароли ДОЛЖНЫ совпадать** с keystore.properties (`Agrotorg87`)
- ⚠️ **Сохраните keystore и пароли безопасно** - без них нельзя обновлять приложение в RuStore!

