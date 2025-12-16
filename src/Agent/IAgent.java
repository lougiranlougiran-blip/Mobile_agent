package Agent;

import java.io.IOException;
import java.util.Hashtable;

public interface IAgent extends java.io.Serializable {

	public void init(Agent service);

	public void setServerServices(Hashtable<String,Object> ns);

	public Hashtable<String,Object> getServerServices();

	public void move() throws IOException;

	public void back() throws IOException;

	public void main() throws IOException;
}