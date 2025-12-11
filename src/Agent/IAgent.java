package Agent;

import java.io.IOException;
import java.util.Hashtable;

import Server.Node;

public interface IAgent extends java.io.Serializable {

	public void init(String name, Node origin);

	public void setNameServer(Hashtable<String,Object> ns);

	public Hashtable<String,Object> getNameServer();

	public void move() throws IOException;

	public void back() throws IOException;

	public void main() throws IOException;
}