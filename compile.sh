#!/bin/bash

# Répertoires de travail
AGENT_DIR="src/Agent"
SERVER_DIR="src/Server"
BIN_DIR="bin"
OUT_DIR="out"

# Créer les répertoires si non existants
mkdir -p "$BIN_DIR"
mkdir -p "$OUT_DIR"

# Compiler les fichiers Java
echo "Compilation des fichiers Java..."
javac -d "$BIN_DIR" "$AGENT_DIR"/*.java "$SERVER_DIR"/*.java

# Créer le fichier JAR
#echo "Création du fichier JAR..."
#cd "$BIN_DIR" || exit
#jar cfe "../$OUT_DIR/$JAR_NAME" Agent.App Agent/App.class Agent/HelloWorld.class

# Exécuter le JAR
#echo "Exécution du fichier JAR..."
#cd "../$OUT_DIR" || exit
#java -jar "$JAR_NAME"

# Optionnel : Nettoyer le répertoire bin
# rm -rf "$BIN_DIR"

