package Agent;

import java.util.Arrays;
import java.util.List;


import Server.Node;
import Server.ServiceMeteo;

public class AgentMeteo extends AgentImpl {


    private String name = "Agent";
    private Node origin;
    private int count = 10;

    private String[][] meteoResult;

    public AgentMeteo(String host, int port) {
        super();

        List<Node> nodes = Arrays.asList(
            new Node("127.0.0.1", 2001),
            new Node("127.0.0.1", 2002),
            new Node("127.0.0.1", 2003),
            new Node("127.0.0.1", 2004)
        );

        origin = new Node(host, port);

        init(nodes, origin, name);
        meteoResult = new String[nodes.size()][2];

    }

    @Override
    public void process() {
        ServiceMeteo s = (ServiceMeteo) serverServices.get("meteo");
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
            meteoResult[index - 1] = new String[] {s.getName(), String.valueOf(average(tmp))};
        }

    }

    

    @Override
    public void onComeBack() {
        /** fin de la recuperation des données */
        System.out.print("\n\n");

        System.out.println("Agent de retour");
        for (String [] val : meteoResult) {
            System.out.println(val[0] + " : " + val[1] + "\n");
        }
        

        System.out.print("\n\n");


    }

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

    private static double average(double[][] data) {
        if (data == null || data.length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double[] row : data) {
            sum += row[0];
        }
        return sum / data.length;
    }
}

