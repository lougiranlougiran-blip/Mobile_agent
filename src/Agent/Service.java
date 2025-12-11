package Agent;

import java.io.IOException;

import Server.Node;

public class Service extends AgentImpl {

    boolean start = true;

    public Service() {
        super();
        init("Agent", new Node("localhost", 2000));
    }

    @Override
    public void main() throws IOException {
        if (start) {
            start = false;
            System.out.println("Before moving to server " + getTarget());
            move();
        } else if (canMove()) {
            System.out.println("Before moving to server " + getTarget());
            move();
        } else {
            back();
            System.out.println("Back to the origin");
            start = true;
        }
    }
}
