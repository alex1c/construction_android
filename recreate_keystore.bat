@echo off
REM Script to recreate keystore with correct alias and passwords from keystore.properties
REM This will delete the old keystore and create a new one with alias 'construction'

echo ========================================
echo Recreating keystore for Construction App
echo ========================================
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
    echo Please use Android Studio to recreate the keystore:
    echo   1. Build -^> Generate Signed Bundle / APK
    echo   2. Create new...
    echo   3. Use these settings:
    echo      - Key store path: construction-release.keystore
    echo      - Password: Agrotorg87
    echo      - Key alias: construction
    echo      - Key password: Agrotorg87
    echo      - Validity: 10000 days
    echo.
    pause
    exit /b 1
)

echo Using keytool: %KEYTOOL_PATH%
echo.

REM Backup old keystore if it exists
if exist "construction-release.keystore" (
    echo Backing up old keystore...
    copy "construction-release.keystore" "construction-release.keystore.backup" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo Old keystore backed up to: construction-release.keystore.backup
    )
    echo.
)

REM Set keystore parameters matching keystore.properties
set KEYSTORE_NAME=construction-release.keystore
set KEY_ALIAS=construction
set KEYSTORE_PASSWORD=Agrotorg87
set KEY_PASSWORD=Agrotorg87
set VALIDITY=10000

echo Keystore parameters:
echo   File: %KEYSTORE_NAME%
echo   Alias: %KEY_ALIAS%
echo   Validity: %VALIDITY% days
echo   Passwords: (from keystore.properties)
echo.

REM Delete old keystore if it exists
if exist "%KEYSTORE_NAME%" (
    echo Deleting old keystore...
    del "%KEYSTORE_NAME%" >nul 2>&1
)

echo Creating new keystore...
echo.

REM Create keystore with correct parameters
"%KEYTOOL_PATH%" -genkey -v -keystore %KEYSTORE_NAME% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity %VALIDITY% -storepass %KEYSTORE_PASSWORD% -keypass %KEY_PASSWORD% -dname "CN=Calc1, OU=Development, O=Calc1, L=Moscow, ST=Moscow, C=RU"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Keystore created successfully!
    echo ========================================
    echo.
    echo Keystore file: %KEYSTORE_NAME%
    echo Key alias: %KEY_ALIAS%
    echo.
    echo IMPORTANT: 
    echo   - Keystore passwords match keystore.properties
    echo   - Store the keystore file and passwords securely!
    echo   - Without this keystore, you cannot update the app in RuStore!
    echo.
    
    REM Verify the keystore was created correctly
    echo Verifying keystore...
    "%KEYTOOL_PATH%" -list -v -keystore %KEYSTORE_NAME% -storepass %KEYSTORE_PASSWORD% | findstr /C:"Alias name:" /C:"construction"
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo Verification: Keystore contains alias 'construction' - OK!
    ) else (
        echo.
        echo WARNING: Could not verify keystore contents!
    )
) else (
    echo.
    echo ========================================
    echo ERROR: Failed to create keystore!
    echo ========================================
    echo.
    echo Please try creating it manually through Android Studio:
    echo   Build -^> Generate Signed Bundle / APK -^> Create new...
    echo.
    pause
    exit /b 1
)

echo.
pause

