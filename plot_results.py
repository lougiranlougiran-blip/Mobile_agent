import pandas as pd
import matplotlib.pyplot as plt

# Configuration visuelle
plt.style.use('seaborn-v0_8-muted')
params = {
    'axes.labelsize': 11,
    'axes.titlesize': 13,
    'legend.fontsize': 10,
    'figure.figsize': [10, 6],
    'grid.alpha': 0.3
}
plt.rcParams.update(params)

def load_csv_robust(filename, default_columns):
    """Charge un CSV en gérant l'absence de headers."""
    try:
        # On tente de lire normalement
        df = pd.read_csv(filename)
        # Si la première valeur de la première colonne est un nombre, 
        # c'est probablement qu'il manque le header
        if not isinstance(df.columns[0], str) or df.columns[0].isdigit():
             df = pd.read_csv(filename, header=None, names=default_columns)
        # On s'assure que les colonnes sont bien nommées comme attendu
        df.columns = default_columns
        return df
    except Exception as e:
        print(f"Erreur sur {filename}: {e}")
        return None

def plot_comparison(rmi_file, agent_file, x_col, title, xlabel, filename, is_nb=False):
    cols = ['nb', 'time_ms'] if is_nb else ['size', 'time_ms']
    
    df_rmi = load_csv_robust(rmi_file, cols)
    df_agent = load_csv_robust(agent_file, cols)
    
    if df_rmi is None or df_agent is None: return

    plt.figure()
    plt.plot(df_rmi[cols[0]], df_rmi[cols[1]], 'o-', label='RMI', color='#d62728', lw=2)
    plt.plot(df_agent[cols[0]], df_agent[cols[1]], 's-', label='Agent Mobile', color='#1f77b4', lw=2)
    
    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel("Temps total (ms)")
    
    # Résolution du bug d'ordonnée : on force l'échelle de 0 à max + 15%
    y_max = max(df_rmi[cols[1]].max(), df_agent[cols[1]].max())
    plt.ylim(0, y_max * 1.15)
    
    if is_nb:
        plt.xticks(sorted(df_rmi[cols[0]].unique()))
    
    plt.grid(True, linestyle='--')
    plt.legend()
    plt.tight_layout()
    plt.savefig(filename, dpi=300)
    plt.close()
    print(f"Graphique généré : {filename}")

# --- Exécution des 4 comparaisons clés ---

# 1. Volume de données (Normal)
plot_comparison("results_rmi_3servers_k_data.csv", "results_agent_3servers_k_data.csv", 
                'size', "Variation des données (Réseau Local Rapide)\n3 serveurs", 
                "Nombre d'images", "1_data_normal.png")

# 2. Volume de données (Sabotage réseau) - LE PLUS IMPORTANT
plot_comparison("results_rmi_100ms_1mb_3servers_k_data.csv", "results_agent_100ms_1mb_3servers_k_data.csv", 
                'size', "Variation des données (100ms, 1Mbit/s)\n3 serveurs", 
                "Nombre d'images", "2_data_hard.png")

# 3. Nombre de serveurs (Normal)
plot_comparison("results_rmi_10k_data_.csv", "results_agent_10k_data.csv", 
                'nb', "Impact de la distribution (Réseau Local Rapide)\nDataset fixe (3000 images)", 
                "Nombre de serveurs cibles", "3_servers_normal.png", is_nb=True)

# 4. Nombre de serveurs (Sabotage réseau)
plot_comparison("results_rmi_10k_data_100ms_1mb.csv", "results_agent_10k_data_100ms_1mb.csv", 
                'nb', "Impact de la distribution (100ms, 1Mbit/s)\nDataset fixe (3000 images)", 
                "Nombre de serveurs cibles", "4_servers_hard.png", is_nb=True)