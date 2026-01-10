import pandas as pd
import matplotlib.pyplot as plt

# sudo tc qdisc add dev lo root netem delay 100ms rate 1mbit
# sudo tc qdisc del dev lo root

# Lecture des résultats
df_agent = pd.read_csv("results_agent.csv")
# df_rmi = pd.read_csv("results_rmi.csv") # Si tu as fait les tests RMI

plt.figure(figsize=(10, 6))

# Tracé de la courbe Agent
plt.plot(df_agent["size"], df_agent["time_ms"], lw=2, marker='s', color='blue', label="Agent Mobile")

# Si tu as RMI, décommente :
# plt.plot(df_rmi["size"], df_rmi["time_ms"], lw=2, marker='o', color='red', label="RMI")

plt.grid(True, linestyle='--', alpha=0.7)
plt.title("Temps d'exécution en fonction de la taille du dataset")
plt.xlabel("Nombre d'images traitées")
plt.ylabel("Temps total (ms)")
plt.legend()

# Sauvegarde de l'image
plt.savefig("performance_comparison.png")
print("Graphique généré : performance_comparison.png")
