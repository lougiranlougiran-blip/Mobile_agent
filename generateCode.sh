#!/bin/bash

# Nom de l'archive de sortie
ZIP_NAME="code.zip"

# Dossier temporaire pour préparer la structure
TEMP_DIR="export_temp"

# Nettoyage si le dossier existe déjà
rm -rf "$TEMP_DIR"
rm -f "$ZIP_NAME"

echo "--- Préparation de l'archive ---"

# 1. Création de la structure des dossiers
mkdir -p "$TEMP_DIR/src/Server"
mkdir -p "$TEMP_DIR/src/Loader"
mkdir -p "$TEMP_DIR/src/resources/MNIST"
mkdir -p "$TEMP_DIR/src/resources/Meteo"
mkdir -p "$TEMP_DIR/src/Agent/"

# 2. Copie des fichiers en respectant les exclusions
# Copie src/Agent mais exclut Agent.java
cp -r src/Agent/* "$TEMP_DIR/src/Agent/"
rm -f "$TEMP_DIR/src/Agent/Agent.java"
rm -f "$TEMP_DIR/src/Agent/AgentImpl.java"
rm -f "$TEMP_DIR/src/Agent/AgentMeteo.java"

# Copie intégrale des autres dossiers
cp -r src/Server/* "$TEMP_DIR/src/Server/"
cp -r src/Loader/* "$TEMP_DIR/src/Loader/"

# 3. Copie du script run.sh à la racine
if [ -f "run.sh" ]; then
    cp "run.sh" "$TEMP_DIR/"
    chmod +x "$TEMP_DIR/run.sh"
else
    echo "Attention: run.sh introuvable !"
fi

# 4. Copie du dosser MNIST et du dossier meteo
if [ -d "src/resources/MNIST" ]; then
    cp -r src/resources/MNIST/* "$TEMP_DIR/src/resources/MNIST/"
fi
cp -r src/resources/Meteo/* "$TEMP_DIR/src/resources/Meteo/"

# 5. Création du ZIP
cd "$TEMP_DIR"
zip -r "../$ZIP_NAME" .
cd ..

# 6. Nettoyage final
rm -rf "$TEMP_DIR"

echo "--------------------------------"
echo "Succès ! L'archive $ZIP_NAME a été créée."
