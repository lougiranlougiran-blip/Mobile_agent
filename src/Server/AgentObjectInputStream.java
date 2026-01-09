package Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.ClassLoader;

public class AgentObjectInputStream extends ObjectInputStream {
    /* Là où le classLoader permet de charger dynamiquement les classes d'un agent à partir d'un JAR,    
     * AgentObjectInputStream permet de désérialiser un objet en utilisant ce ClassLoader.
     * Si on créé un classLoader spécifique mais qu'on désérialise avec un ObjectInputStream classique,
     * les classes spécifiques à l'agent ne seront pas trouvées.
     */

    private final ClassLoader agentClassLoader;

    /* Permet de créer un ObjectInputStream qui utilise un ClassLoader spécifique */
    public AgentObjectInputStream(ByteArrayInputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.agentClassLoader = loader;
    }

    /* Méthode qui permet de désérialiser une classe. desc est un descripteur de sérialisation qui 
     * contient le nom de la classe et un numéro de version unique (SerialVersionUID) permettant de vérifier
     * la compatibilité entre la classe sérialisée et la classe chargée (via le classLoader).
     *  desc contient aussi la liste des champs de l'objet.
    */
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            // On demande au classeLoader de chercher la classe desc.getName() (par exemple Agent.AgentImpl) //
            return Class.forName(desc.getName(), false, agentClassLoader);
        } catch (ClassNotFoundException e) {
            /* Si la classe n'est pas trouvée dans le classLoader de l'agent, on utilise le ClassLoader par défaut 
            Si ce dernier ne la trouve pas quand même, on lève une erreur */
            return super.resolveClass(desc);
        }
    }
}
