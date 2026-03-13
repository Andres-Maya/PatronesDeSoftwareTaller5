#!/bin/bash
# ============================================================
#  build.sh — Compila y ejecuta el Sistema de Acuario Marino
#  Requiere: Java JDK 17+
# ============================================================

SRC_DIR="src"
OUT_DIR="out"
JAR_NAME="AquariumSystem.jar"
MAIN_CLASS="com.aquarium.ui.AquariumUI"

echo "🌊 Compilando Sistema de Acuario Marino..."
mkdir -p "$OUT_DIR"

# Recopilar todos los .java
SOURCES=$(find "$SRC_DIR" -name "*.java")

javac -d "$OUT_DIR" $SOURCES
if [ $? -ne 0 ]; then
  echo "❌ Error de compilación."
  exit 1
fi

echo "📦 Empaquetando JAR..."
echo "Main-Class: $MAIN_CLASS" > manifest.txt
jar cfm "$JAR_NAME" manifest.txt -C "$OUT_DIR" .
rm manifest.txt

echo "✅ Compilación exitosa. Ejecutando..."
java -jar "$JAR_NAME"
