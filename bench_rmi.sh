#!/bin/bash

# 0. Nettoyage initial des ports (RMI + Registry)
echo "Nettoyage des ports..."
fuser -k 2001/tcp 2002/tcp 2003/tcp 2004/tcp 2> /dev/null
sleep 2

# Chemins
BASE_DIR="$HOME/Documents/N7/intergiciel/Projet/test"
OUT_CSV="results_rmi.csv"
echo "size,time_ms" > "$OUT_CSV"

# 1. LANCEMENT UNIQUE DES SERVEURS RMI (En arrière-plan)
echo "Démarrage des serveurs RMI (s1, s2, s3)..."
# On suppose que chaque run.sh de serveur lance ServeurRMI <port> <type>
(cd "$BASE_DIR/s1/code" && ./run.sh 2002 t) & PID1=$!
(cd "$BASE_DIR/s2/code" && ./run.sh 2003 t) & PID2=$!
(cd "$BASE_DIR/s3/code" && ./run.sh 2004 t) & PID3=$!

sleep 5 # Temps pour LocateRegistry.createRegistry() et rebind()

# Tailles à tester
SIZES="100 500 1000 2500 5000 7500 10000"

# 2. BOUCLE DE TEST SUR LE CLIENT
for size in $SIZES
do
    echo "--- Test RMI : Taille $size ---"
    
    start=$(date +%s%3N)
    
    # Lancement du client avec les 3 ports et la taille
    # Remplace par ta commande exacte (ex: java -cp bin Client.Client ...)
    ./run.sh 2002 2003 2004 "$size"
    
    end=$(date +%s%3N)
    elapsed=$((end - start))
    
    echo "$size,$elapsed" >> "$OUT_CSV"
    echo "Temps RMI : $elapsed ms"
done

# 3. NETTOYAGE
echo "Arrêt des serveurs RMI..."
kill -INT $PID1 $PID2 $PID3