# Исправление проблемы с созданием Keystore

## Проблема
При создании keystore через Android Studio возникает ошибка "Отказано в доступе" (Access denied).

## Причина
В диалоге создания keystore указан неправильный путь:
- ❌ **Неправильно:** `E:\petProject\construction_android\keystore` (создает директорию)
- ✅ **Правильно:** `E:\petProject\construction_android\construction-release.keystore` (создает файл)

## Решение

### Шаг 1: Удалить неправильную директорию
Если была создана директория `keystore`, удалите её:
```powershell
Remove-Item "keystore" -Recurse -Force
```

### Шаг 2: Создать keystore с правильным путем

1. Откройте Android Studio
2. **Build → Generate Signed Bundle / APK**
3. Выберите **Android App Bundle** или **APK**
4. Нажмите **Create new...**
5. **ВАЖНО:** В поле **Key Store Path** укажите **ПОЛНЫЙ путь с расширением**:
   ```
   E:\petProject\construction_android\construction-release.keystore
   ```
   ⚠️ **НЕ** используйте просто `keystore` - это создаст директорию!

6. Заполните остальные поля:
   - **Password:** `D3!8MzxAwoUT`
   - **Confirm:** `D3!8MzxAwoUT`
   - **Key alias:** `construction`
   - **Key Password:** `D3!8MzxAwoUT`
   - **Confirm:** `D3!8MzxAwoUT`
   - **Validity (years):** `25` (или `10000` дней)
   - **Certificate:** Заполните информацию о разработчике

7. Нажмите **OK**

### Альтернативный способ: Создать в другой директории

Если проблемы с доступом продолжаются, создайте keystore в домашней директории:

1. В Android Studio укажите путь:
   ```
   %USERPROFILE%\construction-release.keystore
   ```
   Или:
   ```
   C:\Users\ВашеИмя\construction-release.keystore
   ```

2. После создания скопируйте файл в корень проекта:
   ```powershell
   Copy-Item "$env:USERPROFILE\construction-release.keystore" -Destination "E:\petProject\construction_android\construction-release.keystore"
   ```

3. Обновите `keystore.properties` если путь изменился

## Проверка

После создания проверьте:
```powershell
Test-Path "construction-release.keystore"
Get-Item "construction-release.keystore" | Select-Object Name, Length
```

Файл должен существовать и иметь размер больше 0 байт.

## Если проблема сохраняется

1. **Проверьте антивирус** - он может блокировать создание keystore файлов
2. **Создайте keystore в другой директории** (например, в `Documents`)
3. **Проверьте права доступа** к директории проекта
4. **Попробуйте запустить Android Studio от имени администратора** (хотя вы уже это сделали)



