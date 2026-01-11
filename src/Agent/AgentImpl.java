package Agent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import Server.Node;
import Server.Server;

public abstract class AgentImpl implements IAgent {
    /* AgentImpl contient toute la logique de migration vers les différents serveurs,
     * ainsi que la communication avec l'envoi du jar contenant les classes, l'agent sérialisé etc...
     * En effet, seule la logique métier, c'est-à-dire les adresses IP ainsi et le traitement,
     * est présente sur l'agent.
     */

    /* 
     * Le mot-clé 'transient' indique q'un élément ne doit pas être sérialisé.
     * C'est le cas des sockets et des output streams.
     */
    private transient Socket clientSocket;
    private transient OutputStream socketOS;
    private transient ByteArrayOutputStream objectBAOS;
    private transient ObjectOutputStream objectOS;
    private transient DataOutputStream dataOS;

    // attribut qui permet de conserver le jar en mémoire
    protected byte[] ownCode;
    // permet de savoir si l'agent est encore sur le serveur d'origine (true) ou pas (false)
    protected boolean start = true;

    protected String name;              // Nom de l'agent
    protected Node origin;              // Serveur d'origine        
    protected int index;                // Index qui permet de suivre sur quel serveur se trouve l'agent

    // Table des services récupérés sur un serveur, et donnés par le serveur
    protected transient Hashtable<String, Object> serverServices;

    // Liste des serveurs
    protected List<Node> nodes;

    @Override
    public void init(List<Node> nodes, Node origin, String name) {
        this.nodes = nodes;
        this.origin = origin;
        this.name = name;
        this.index = -1; // Au départ, l'agent est sur le serveur d'origine
    }

    /* Fonction principale de l'agent. Cette fonction pourrait aussi être surchargée DANS l'agent,
     * mais son implantation ici est correcte car elle reste générique et permet d'éviter de réécrire
     * la même logique de déplacement à chaque fois.    
     */
    @Override
    public void main() throws IOException {

        if (!start && index >= nodes.size()) {                      // On est revenu à l'origine
            onComeBack();                                           // On effectue le traitement lorsque l'agent revient
            Server.stopServer(origin.getHost(), origin.getPort());  // On envoie une notification au serveur pour qu'il s'arrete
        } else {
            execute();                                              // Sinon, on est au départ ou sur un serveur cible
        }

        /* Un verrou est partagé entre le serveur et l'agent via les services du serveur.
         * L'agent notifie le serveur une fois son traitement terminé pour que le serveur puisse libérer les ressources
         * et continuer son exécution. */
        Object o = getServerServices().get(this.getClass().getName()+"_lock");
		synchronized(o) {o.notify();}
    }

    /* Fonction qui gère la logique de déplacement de l'agent. A chaque appel, l'agent
     * effectue son traitement local via la fonction process(), puis se déplace vers le
     * noeud suivant ou revient à l'origine si tous les noeuds ont été visités.
    */
    public void execute() throws IOException {
        if (start) {
            start = false;
            index = 0;
            System.out.println("Agent " + name + " has successfully started.");
            move(); 
        } else {
            System.out.println("Agent arrived on node: " + nodes.get(index));
            process();
            
            if (index < nodes.size() - 1) {
                index++;
                move();
            } else {
                index++;
                System.out.println("All targets visited, returning to origin: " + origin);
                back();
            }
        }
    }

    /* Fonction permettant de déplacer l'agent vers le noeud suivant 
     * On recupère d'abord le noeud suivant, puis on ouvre la connexion,
     * on envoie le message, et finalement on ferme la connexion.
     */
    @Override
	public void move() throws IOException {
        Node next = nodes.get(index);
        System.out.println("Moving to next node: " + next);
        startConnection(next.getHost(), next.getPort());
        sendMessage();
        stopConnection();
    }

    /* Fonction permettant de déplacer l'agent vers le noeud d'origine
     * On ouvre la connexion, on envoie le message (contenant les résultats)
     * et finalement on ferme la connexion.
     */
    @Override
	public void back() throws IOException {
        startConnection(origin.getHost(), origin.getPort());
        sendMessage();
        stopConnection();
    }

    /* Permet d'ouvrir un socket et de communiquer avec le serveur
       identifié par une adresse ip (host) et un numéro de port */
    public void startConnection(String host, int port) {
        try {
            clientSocket = new Socket(host, port);            // Ouverture du socket
            socketOS = clientSocket.getOutputStream();        // On récupère le flux du socket pour envoyer des octets
            objectBAOS = new ByteArrayOutputStream();         // Flux pour sérialiser l'agent dans un tableau d'octets (taille inconnue)
            objectOS = new ObjectOutputStream(objectBAOS);    // Flux pour sérialiser l'agent
            dataOS = new DataOutputStream(socketOS);          // Flux pour envoyer les longueurs des donnés

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* Sérialise un objet en tableau d'octets */
    public byte[] serializeObject(Object obj) {
        try {
            objectOS.writeObject(obj);               // On écrit l'objet dans le flux
            objectOS.flush();                        // On vide pour envoyer immédiatement
            return objectBAOS.toByteArray();         // On récupère le tableau d'octets
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Envoie un message contenant le code (jar) et l'objet sérialisé de l'agent.
    C'est cette fonction qui envoie réellement l'agent au serveur.*/
    public void sendMessage() throws IOException {
        byte[] codeToSend = (ownCode != null) ? ownCode : JarFactory.createJar(getRequiredClasses());
        byte[] objectBytes = serializeObject(this);          // Sérialisation de l'agent  
        dataOS.writeInt(codeToSend.length);                  // Taille du JAR
        dataOS.write(codeToSend);                            // Contenu du JAR
        dataOS.writeInt(objectBytes.length);                 // Taille de l'agent
        dataOS.write(objectBytes);                           // Contenu de l'agent
        dataOS.flush();                                      // On force l'envoie
    }

    /* Ferme les flux et le socket (donc la connexion) */
    public void stopConnection() throws IOException {
        dataOS.close();
        objectBAOS.close();
        objectOS.close();
        socketOS.close();
        clientSocket.close();
    }

    /* Retourne la liste des classes nécessaires pour exécuter l'agent.
       Cette liste peut être surchargée dans la classe Agent si besoin.
       Elle permet au code de fonctionner si la logique métier tient dans l'agent seul.
     */
    public List<String> getRequiredClasses() {
        return Arrays.asList("Agent.AgentImpl", "Agent.IAgent", "Agent.JarFactory", "Server.Service", "Server.Node", this.getClass().getName());
    }

    /* Fonction appelée par le serveur pour transmettre à l'agent les services qu'il propose */
    @Override
	public void setServerServices(Hashtable<String,Object> ns) {
        this.serverServices = ns;
    }

    /* Fonction utilisée par l'agent pour récupérer les services proposés par le serveur */
    @Override
	public Hashtable<String,Object> getServerServices() {
        return serverServices;
    }

    /* Fonction appelée par le serveur pour transmettre sucessivement le même code de l'agent */
    @Override
    public void setOwnCode(byte[] code) {
        this.ownCode = code;
    }

    /* Fonction abastraite implémentée dans l'agent qui contient un traitement spécifique (ex: additionner des nombres) */
    public abstract void process();

    /* Fonction abstraite implémentée dans l'agent qui contient le traitement à effectuer
     * lorsque l'agent revient à son serveur d'origine (ex: calculer une moyenne sur les résultats) */
    public abstract void onComeBack();
}
