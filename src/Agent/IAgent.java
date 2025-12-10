package Agent;

import java.util.Hashtable;

import Server.Node;

public interface IAgent extends java.io.Serializable {

	public void init(String name, Node origin);

	public void setNameServer(Hashtable<String,Object> ns);

	public Hashtable<String,Object> getNameServer();

	public void move(Node target) throws MoveException;

	public void back() throws MoveException;

	public void main() throws MoveException;
}