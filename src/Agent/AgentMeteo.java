package Agent;

import Server.Node;
import Server.ServiceMeteo;
import java.util.Arrays;
import java.util.List;

public class AgentMeteo extends AgentImpl {
    /* Agent contenant uniquement la logique métier (l'application).
     * Dans ce cas, l'agent cherhce des données sur les different serveur et ramène une liste de moyenne des données.
     */

    private String name = "Agent";
    private Node origin;
    private int count = 10;

    private String[][] meteoResult;

    public AgentMeteo(String host, int port) {
        super();
        
        // Liste des noeuds (serveurs) avec les adresses IP et les ports
        List<Node> nodes = Arrays.asList(
            new Node("127.0.0.1", 2001),
            new Node("127.0.0.1", 2002),
            new Node("127.0.0.1", 2003),
            new Node("127.0.0.1", 2004)
        );
        
        // L'origine est le serveur avec l'IP est le Port passés en paramètre à l'exécution
        origin = new Node(host, port);

        // Initialise la logique de migration sur AgentImpl
        init(nodes, origin, name);
        // Initialisation de la liste des resultats : type donnée * moyenne
        meteoResult = new String[nodes.size()][2];

    }

    /* 
     * Traitement effectuée par l'agent, implementation de la méthode abstraite de AgentImpl.
     * Pour cet exemple, chaque serveur possède un type de donnée, l'agent trouve ce type de donnée, 
     * puis fait la moyenne des donnée que le serveur a. Il conserve cette moyenne. 
     */
    @Override
    public void process() {
        // On recupère le service Meteo du server
        ServiceMeteo s = (ServiceMeteo) serverServices.get("meteo");
        // si on n'est pas sur le serveur de départ on recupere les donnée du serveur
        if (index != 0) {
            System.out.println("Using service: " + s.getName());

            double[][] tmp;
            switch (s.getName()) {
                case "Temperature" :
                    tmp = s.getTemperatureData(count);
                    break;
                case "Humidite" :
                    tmp = s.getHumiditeData(count);
                    break;
                case "Pression" :
                    tmp = s.getPressionData(count);
                    break;
                default:
                    // normalement impossible
                    throw new AssertionError();
            }
            // on traite et enregistre les données du serveur.
            meteoResult[index - 1] = new String[] {s.getName(), String.valueOf(average(tmp))};
        }

    }

    

    @Override
    public void onComeBack() {
        /** fin de la recuperation des données */
        System.out.print("\n\n");

        System.out.println("Agent de retour");
        // itéré sur le tableau, on restitu les moyenne trouvées, et leur type
        for (String [] val : meteoResult) {
            System.out.println(val[0] + " : " + val[1]);
        }
        

        System.out.print("\n\n");


    }


    /* Liste des classes à envoyer. Pour cet exmeple, nous avons inclut 
     * notre librairie personnalisée de réseau de neurones.
     */
    @Override 
    public List<String> getRequiredClasses() {
        return Arrays.asList(
            "Agent.AgentImpl",
            "Agent.IAgent",
            "Agent.JarFactory",
            "Server.ServiceMeteo",
            "Server.Node",
            this.getClass().getName()
        );
    }

    /** fonction privé permettant de calculer la moyenne des données renvoyé par le serveur */
    private static double average(double[][] data) {
        // si la data est vide, on evite les erreurs 
        if (data == null || data.length == 0) {
            return 0.0;
        }
        // calcule de la moyenne
        double sum = 0.0;
        for (double[] row : data) {
            sum += row[0];
        }
        return sum / data.length;
    }
}

