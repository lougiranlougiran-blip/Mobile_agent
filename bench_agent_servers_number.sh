#!/bin/bash

# Vérification de l'argument
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <nombre_de_serveurs_cibles>"
    exit 1
fi

NB_SERVEURS=$1  # Nombre de serveurs cibles (ex: 3)
SIZE=3000       # Taille fixe du dataset pour tester l'impact du nombre de sauts
BASE_DIR="$HOME/Documents/N7/intergiciel/Projet/test"
OUT_CSV="results_agent_10k_data_5s.csv"

# 0. Nettoyage initial des ports (Origine 2001 + Cibles 2002...N)
echo "Nettoyage des ports 2001 à $((2001 + NB_SERVEURS))..."
fuser -k 2001/tcp 2> /dev/null
for ((i=1; i<=NB_SERVEURS; i++)); do
    PORT=$((2001 + i))
    fuser -k $PORT/tcp 2> /dev/null
done
sleep 2

# 1. LANCEMENT DYNAMIQUE DES SERVEURS CIBLES (s1, s2, ...)
echo "Démarrage de $NB_SERVEURS serveur(s) cible(s) Agent..."
PIDS=()
PORTS_LIST=""

for ((i=1; i<=NB_SERVEURS; i++)); do
    PORT=$((2001 + i))
    DIR_INDEX=$i 
    
    # Lancement du serveur passif (-t) dans son dossier respectif
    # On lui donne son port et les types par défaut
    (cd "$BASE_DIR/s${DIR_INDEX}/code" && ./run.sh -t 127.0.0.1 $PORT t NN) &
    PIDS+=($!)
    PORTS_LIST="$PORTS_LIST $PORT"
done

# Attente pour que les serveurs cibles soient prêts à recevoir l'agent
sleep 5 

# 2. EXÉCUTION DE L'ORIGINE (Port 2001)
echo "--- Test Agent : $SIZE images réparties sur $NB_SERVEURS cible(s) ---"
echo "Itinéraire de l'agent (ports cibles) :$PORTS_LIST"

start=$(date +%s%3N)

# Lancement de l'origine (-o)
# L'origine crée l'agent, lui donne la liste des ports cibles et la taille
# Le programme s'arrête de lui-même quand l'agent revient à l'origine
./run.sh -o 127.0.0.1 2001 t NN "$SIZE" $PORTS_LIST

end=$(date +%s%3N)
elapsed=$((end - start))

# Sauvegarde du résultat (Nombre de serveurs cibles, Temps total en ms)
echo "$NB_SERVEURS,$elapsed" >> "$OUT_CSV"
echo "------------------------------------------"
echo "Temps Agent Total : $elapsed ms"

# 3. NETTOYAGE
echo "Arrêt des serveurs cibles..."
for pid in "${PIDS[@]}"; do
    kill -INT "$pid" 2> /dev/null
done

echo "Terminé. Résultat ajouté dans $OUT_CSV"