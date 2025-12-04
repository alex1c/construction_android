@echo off
REM Script to create keystore for signing the app
REM This will create a keystore file for release signing

echo Creating keystore for Construction Calculators app...
echo.

REM Try to find keytool in common locations
set KEYTOOL_PATH=
if exist "%JAVA_HOME%\bin\keytool.exe" (
    set KEYTOOL_PATH=%JAVA_HOME%\bin\keytool.exe
) else if exist "%ANDROID_HOME%\jbr\bin\keytool.exe" (
    set KEYTOOL_PATH=%ANDROID_HOME%\jbr\bin\keytool.exe
) else if exist "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" (
    set KEYTOOL_PATH=C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe
) else (
    REM Try to use keytool from PATH
    where keytool >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set KEYTOOL_PATH=keytool
    )
)

if "%KEYTOOL_PATH%"=="" (
    echo ERROR: keytool not found!
    echo.
    echo Please ensure Java JDK is installed and JAVA_HOME is set.
    echo Or run the command manually:
    echo   keytool -genkey -v -keystore construction-release.keystore -alias construction -keyalg RSA -keysize 2048 -validity 10000
    echo.
    pause
    exit /b 1
)

echo Using keytool: %KEYTOOL_PATH%
echo.

REM Set keystore parameters
set KEYSTORE_NAME=construction-release.keystore
set KEY_ALIAS=construction
set KEYSTORE_PASSWORD=construction123
set KEY_PASSWORD=construction123
set VALIDITY=10000

echo Keystore name: %KEYSTORE_NAME%
echo Key alias: %KEY_ALIAS%
echo Validity: %VALIDITY% days
echo.
echo WARNING: Using default passwords. Please change them in keystore.properties after creation!
echo.

REM Create keystore
"%KEYTOOL_PATH%" -genkey -v -keystore %KEYSTORE_NAME% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity %VALIDITY% -storepass %KEYSTORE_PASSWORD% -keypass %KEY_PASSWORD% -dname "CN=Calc1, OU=Development, O=Calc1, L=Moscow, ST=Moscow, C=RU"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Keystore created successfully!
    echo.
    echo IMPORTANT: Change the default passwords in keystore.properties file!
    echo Store the keystore file and passwords securely!
) else (
    echo.
    echo Error creating keystore!
    pause
    exit /b 1
)

pause

