# Создание Keystore вручную

## Способ 1: Через Android Studio (Рекомендуется)

1. Откройте проект в Android Studio
2. Перейдите в меню: **Build → Generate Signed Bundle / APK**
3. Выберите **Android App Bundle** (рекомендуется для RuStore) или **APK**
4. Нажмите **Create new...** для создания нового keystore
5. Заполните форму:
   - **Key store path**: Выберите место для сохранения (например, `construction-release.keystore`)
   - **Password**: Введите пароль для keystore (запомните его!)
   - **Key alias**: `construction`
   - **Key password**: Введите пароль для ключа (может совпадать с паролем keystore)
   - **Validity**: `10000` (дней)
   - **Certificate**: Заполните информацию о разработчике
6. Нажмите **OK**
7. Скопируйте созданный keystore в корень проекта
8. Создайте файл `keystore.properties` на основе `keystore.properties.example`

## Способ 2: Через командную строку (если установлен JDK)

1. Убедитесь, что JDK установлен и `keytool` доступен:
   ```cmd
   keytool -version
   ```

2. Если `keytool` не найден, найдите его в:
   - `%JAVA_HOME%\bin\keytool.exe`
   - `%ANDROID_HOME%\jbr\bin\keytool.exe`
   - `C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`

3. Запустите команду (замените пути и пароли на свои):
   ```cmd
   keytool -genkey -v -keystore construction-release.keystore -alias construction -keyalg RSA -keysize 2048 -validity 10000
   ```

4. Введите пароли и информацию о разработчике при запросе

## Способ 3: Использование скрипта (если keytool доступен)

1. Убедитесь, что `keytool` доступен в PATH или установите JAVA_HOME/ANDROID_HOME
2. Запустите скрипт:
   ```cmd
   create_keystore.bat
   ```
3. ⚠️ **ВАЖНО**: Измените пароли по умолчанию в `keystore.properties`!

## После создания keystore

1. Скопируйте `keystore.properties.example` в `keystore.properties`
2. Откройте `keystore.properties` и укажите:
   ```properties
   storeFile=construction-release.keystore
   storePassword=ваш-реальный-пароль
   keyAlias=construction
   keyPassword=ваш-реальный-пароль
   ```
3. Убедитесь, что `construction-release.keystore` находится в корне проекта
4. Проверьте, что файлы добавлены в `.gitignore` (не коммитятся в Git)

## Проверка keystore

После создания проверьте keystore:
```cmd
keytool -list -v -keystore construction-release.keystore
```

Введите пароль keystore при запросе.

## Безопасность

⚠️ **КРИТИЧЕСКИ ВАЖНО:**
- НЕ коммитьте `construction-release.keystore` в Git
- НЕ коммитьте `keystore.properties` в Git
- Сделайте резервную копию keystore в безопасном месте
- Используйте сильные пароли (минимум 12 символов)
- Без keystore вы НЕ сможете обновлять приложение в RuStore!

