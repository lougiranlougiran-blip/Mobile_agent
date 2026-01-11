#!/bin/bash

# Vérification de l'argument
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <nombre_de_serveurs>"
    exit 1
fi

NB_SERVEURS=$1
SIZE=10000
BASE_DIR="$HOME/Documents/N7/intergiciel/Projet/test"
OUT_CSV="results_rmi_10k_data_5s.csv"

# 0. Nettoyage initial des ports
echo "Nettoyage des ports 2002 à $((2001 + NB_SERVEURS))..."
for ((i=1; i<=NB_SERVEURS; i++)); do
    PORT=$((2001 + i))
    fuser -k $PORT/tcp 2> /dev/null
done
sleep 2

# 1. LANCEMENT DYNAMIQUE DES SERVEURS RMI
echo "Démarrage de $NB_SERVEURS serveur(s) RMI..."
PIDS=()
PORTS_LIST=""

for ((i=1; i<=NB_SERVEURS; i++)); do
    PORT=$((2001 + i))
    DIR_INDEX=$i # On suppose que tes dossiers sont s1, s2, s3...
    
    # Lancement du serveur dans son dossier respectif
    (cd "$BASE_DIR/s${DIR_INDEX}/code" && ./run.sh $PORT t) &
    PIDS+=($!)
    PORTS_LIST="$PORTS_LIST $PORT"
done

# Attente pour l'enregistrement (bind) dans le rmiregistry
sleep 5 

# 2. EXÉCUTION DU CLIENT
echo "--- Test RMI : $SIZE images sur $NB_SERVEURS serveur(s) ---"
echo "Ports utilisés :$PORTS_LIST"

start=$(date +%s%3N)

# Appel du client avec la liste des ports et la taille fixe
# Le client traite les paramètres reçus pour contacter chaque stub [cite: 15, 18]
./run.sh $PORTS_LIST "$SIZE"

end=$(date +%s%3N)
elapsed=$((end - start))

# Sauvegarde du résultat (Nb de serveurs, Temps en ms)
echo "$NB_SERVEURS,$elapsed" >> "$OUT_CSV"
echo "------------------------------------------"
echo "Temps RMI Total : $elapsed ms"

# 3. NETTOYAGE
echo "Arrêt des serveurs RMI..."
for pid in "${PIDS[@]}"; do
    kill -INT "$pid" 2> /dev/null
done

echo "Terminé. Résultat ajouté dans $OUT_CSV"