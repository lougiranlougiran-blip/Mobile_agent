package Server;

import Agent.JarFactory;

import java.io.IOException;

import java.util.Map;

public class AgentClassLoader extends ClassLoader  {
    /* Permet de charger dynamiquement les classes d'un agent à partir d'un JAR */

    // Map des classes contenant le nom de la classe et les bytes correspondants
    private final Map<String, byte[]> classes;

    // A la création, on lit directement le JAR pour remplir la map
    public AgentClassLoader(byte[] jarData, int length) throws IOException {
        this.classes = JarFactory.readJar(length, jarData);
    }

    /* Quand le server rencontre une classe inconnue, de nom 'name',
       il regarde dans sa map puis convertit les bytes en un objet Class */
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classes.get(name);  // Récupère le code associé à la classe

        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length); // Si trouvé, le convertit en Class
        }

        // Sinon on cherche dans le ClassLoader local, potentiellement en levant un erreur
        return super.findClass(name);
    }
}
