package Agent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import Server.Node;

public class Agent extends AgentImpl {

    private boolean start = true;
    private String name = "Agent";

    private Node origin;
    private Node previous;
    private Node next;
    private int index;


    private final List<Node> nodes = Arrays.asList(
            new Node("localhost", 2001),
            new Node("localhost", 2002),
            new Node("localhost", 2003)
    );

    public Agent() {
        super();
        init(this);
        origin = new Node("localhost", 2000);
        index = 0;
        next = nodes.get(index);
        previous = null;
    }

    @Override
    public void main() throws IOException {
        if (start) {
            start = false;
            System.out.println("Before moving to server " + getTarget());
            move();
            System.out.println("Hello World" + serverServices.get("name"));
        } else if (canMove()) {
            System.out.println("Before moving to server " + getTarget());
            move();
        } else {
            back();
            System.out.println("Back to the origin");
            start = true;
        }
    }

    public String getName() {
        return name;
    }

    public Node getOrigin() {
        return origin;
    }

    public String nextGetIP() {
        return next.getIP();
    }

    public int nextGetPort() {
        return next.getPort();
    }

    public void goNext() {
        previous = next;
        next = nodes.get(index++);
    }

    public void goBack() {
        previous = next;
        next = null;
        index = 0;
    }

    public boolean canMove() {
        return index < nodes.size();
    }

    public Node getTarget() {
        return next;
    }
}
