package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ServiceMeteo {
    /* Implémente un service proposé par un serveur.
     * Ici une base de donnée qui contient soit des données de Température, de Pression ou d'humidité.
     */
    private String name;
    private String path = "src/resources/Meteo/";


    public ServiceMeteo(String typeDonne) {
        // definition du type de donnée possédé lors de l'initialisation
        this.name = typeDonne;
    }

    public String getName() {
        return name;
    }

    public double[][] getTemperatureData(int count) {
        if (name.equals("Temperature")) {
            // renvoyer les "cout" lignes du fichier si il a les données de temperature
            System.out.println("revoie de la " + name);
            return loadCSV(path + "Temperature.csv", count);
        } else {
            // renvoie rien sinon 
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    // renvoyer les "cout" lignes du fichier si il a les données d'humidité et rien sinon (liste vide)
    public double[][] getHumiditeData(int count) {
        if (name.equals("Humidite")) {
            System.out.println("revoie de l'" + name);
            return loadCSV(path + "Humidite.csv", count); 
        } else {
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    // renvoyer les "cout" lignes du fichier si il a les données de preession et rien sinon (liste vide)
    public double[][] getPressionData(int count) {
        if (name.equals("Pression")) {
            System.out.println("revoie de la " + name);
            return loadCSV(path + "Pression.csv", count); 
        } else {
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    // permet de charger les csv qui contiennent les données météo.
// Charge au plus N valeurs depuis un fichier CSV
private static double[][] loadCSV(String path, int N) {

    // Liste dynamique pour stocker les lignes lues (chaque ligne = un tableau de double)
    List<double[]> rows = new ArrayList<>();

    // Ouverture du fichier en lecture, avec fermeture automatique à la fin (try-with-resources)
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {

        String line;              // Contient chaque ligne lue
        boolean firstLine = true; // Sert à ignorer la première ligne (en-tête)
        Integer numberOfLine = 0; // Compteur de lignes lues (hors en-tête)


        //  boucle et s'arrête lorsque nous avons lu asser de ligne ou plus de ligne a lire
        while ((line = br.readLine()) != null && numberOfLine < N) {

            // Ignore la première ligne car nos CSV on une en tete
            if (firstLine) {
                firstLine = false;
                continue;
            }

            // Découpe la ligne par virgule
            String[] parts = line.split(",");

            // Si la ligne n'a pas au moins 2 colonnes, on l'ignore
            if (parts.length < 2) continue;

            // Convertit la deuxième colonne en double
            double value = Double.parseDouble(parts[1].trim());

            // Ajoute la valeur dans la liste sous forme de tableau
            rows.add(new double[] { value });

            // Incrémente le nombre de lignes valides lues
            numberOfLine++;
        }

    } catch (IOException | NumberFormatException e) {
        // Gestion des erreurs de lecture ou de conversion
        System.err.println("Erreur chargement CSV : " + path);
        e.printStackTrace();
    }

    // Transforme la liste dynamique en tableau 2D fixe
    return rows.toArray(new double[0][]);
}

