@echo off
REM ============================================================
REM  build.bat — Compila y ejecuta el Sistema de Acuario Marino
REM  Requiere: Java JDK 17+
REM ============================================================

SET SRC_DIR=src
SET OUT_DIR=out
SET JAR_NAME=AquariumSystem.jar
SET MAIN_CLASS=com.aquarium.ui.AquariumUI

echo 🌊 Compilando Sistema de Acuario Marino...
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM Recopilar todos los .java
for /r %SRC_DIR% %%f in (*.java) do (
    echo %%f >> sources.txt
)

javac -d %OUT_DIR% @sources.txt
del sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo ❌ Error de compilación.
    exit /b 1
)

echo 📦 Empaquetando JAR...
echo Main-Class: %MAIN_CLASS% > manifest.txt
jar cfm %JAR_NAME% manifest.txt -C %OUT_DIR% .
del manifest.txt

echo ✅ Compilación exitosa. Ejecutando...
java -jar %JAR_NAME%
