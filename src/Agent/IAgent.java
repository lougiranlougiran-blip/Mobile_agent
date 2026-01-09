package Agent;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

import Server.Node;

public interface IAgent extends Serializable {

	public void init(List<Node> nodes, Node origin, String name);

	public void setServerServices(Hashtable<String,Object> ns);

	public Hashtable<String,Object> getServerServices();

	public void move() throws IOException;

	public void back() throws IOException;

	public void main() throws IOException;

	public void setOwnCode(byte[] code);
}