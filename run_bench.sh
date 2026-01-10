#!/bin/bash

fuser -k 2001/tcp 2002/tcp 2003/tcp 2004/tcp 2> /dev/null

# Chemins des serveurs
BASE_DIR="$HOME/Documents/N7/intergiciel/Projet/test"
S1_DIR="$BASE_DIR/s1/code"
S2_DIR="$BASE_DIR/s2/code"
S3_DIR="$BASE_DIR/s3/code"

# Chemin vers ton code Client/Origine (ajuste si nécessaire)
ORIGIN_DIR="$(pwd)" 

OUT_CSV="results_agent.csv"
echo "size,time_ms" > "$OUT_CSV"

# 1. LANCEMENT UNIQUE DES SERVEURS CIBLES
echo "Démarrage des serveurs cibles..."
# On utilise (cd ...) & pour ne pas changer le dossier du script principal
(cd "$S1_DIR" && ./run.sh -t 127.0.0.1 2002 t NN) & 
PID1=$!
(cd "$S2_DIR" && ./run.sh -t 127.0.0.1 2003 t NN) &
PID2=$!
(cd "$S3_DIR" && ./run.sh -t 127.0.0.1 2004 t NN) &
PID3=$!

# Attendre que les serveurs soient prêts
sleep 3 

# Tailles à tester
SIZES="100 500 1000 2500 5000 7500 10000"

# 2. BOUCLE DE TEST
for size in $SIZES
do
    echo "--- Test pour Taille = $size ---"
    
    start=$(date +%s%3N)
    
    # LANCEMENT DE L'ORIGINE
    # On s'assure d'exécuter le run.sh qui est dans le dossier courant
    "$ORIGIN_DIR/run.sh" -o 127.0.0.1 2001 t NN "$size"
    
    end=$(date +%s%3N)
    elapsed=$((end - start))
    
    echo "$size,$elapsed" >> "$OUT_CSV"
    echo "Résultat pour $size : $elapsed ms"
    echo "------------------------------------------"
done

# 3. NETTOYAGE FINAL
echo "Fin des tests. Arrêt des serveurs..."
kill -INT $PID1 $PID2 $PID3

echo "Terminé. Résultats dans $OUT_CSV"