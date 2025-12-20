package Agent;

import java.util.Arrays;
import java.util.List;

import Server.Node;
import Server.Service;

public class Agent extends AgentImpl {

    private String name = "Agent";
    private Node origin;
    private int result;

    public Agent(String host, int port) {
        super();

        List<Node> nodes = Arrays.asList(
            new Node("192.168.0.43", 2001),
            new Node("192.168.0.43", 2002),
            new Node("192.168.0.28", 2003),
            new Node("192.168.0.28", 2004)
        );

        origin = new Node(host, port);
        init(nodes, origin, name);

        result = 0;
    }

    @Override
    public void process() {
        Service s = (Service) serverServices.get("service");
        System.out.println("Processing with: " + s.getName());
        result += s.getRessource();
    }

    @Override
    public void onComeBack() {
        System.out.println("Agent is back with total result: " + result);
    }
}
