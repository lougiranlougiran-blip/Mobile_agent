package Agent;

import java.util.ArrayList;
import java.util.List;

import Server.Node;

public class Service extends AgentImpl {

    boolean start = true;

    // private HashMap<String, Integer> nodes = new HashMap<>(); 
    private List<Node> nodes = new ArrayList<>();

    Node previous = null;
    Node next = null;
    int index = -1;

    public Service() {
        super();
        addNodes(2001, 2002);
        next = nodes.get(index++);
        
    }

    public void addNodes(int... ports) {
        for (int port : ports) {
            nodes.add(new Node("localhost", port));
        }
    }

    @Override
    public void main() throws MoveException {
        if (start) {
            start = false;
            System.out.println("Before moving to server " + index);
            previous = next;
            move(next);
        } else if (index < nodes.size()) {
            next = nodes.get(index++);
            previous = next;
            System.out.println("Before moving to server " + index);
            move(next);
        } else {
            index = -1;
            next = null;
            back();
            System.out.println("Back to the origin");
            start = true;
        }

        Object o = getNameServer().get(this.getClass().getClass() + "_lock");
        synchronized(o) {o.notify();}
    }
}

