package Agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class JarFactory {
    /* 
     * Classe utilitaire qui permet de créer un JAR dynamiquement à partir d'une liste de classes.
     * En pratique, elle n'est pas obligatoire et le code pourrait être simplifié mais elle permet de raccourcir
     * le code de AgentImpl et du ClassLoader. De plus, je trouve l'utilisation de JarEntry intéressante.
     */

    /* Fonction qui utilise le vrai classLoader pour récupérer le bytecode d'une classe qu'on veut envoyer */
    private static byte[] loadClassByteCode(String className) throws IOException {
        try (InputStream inputStream = JarFactory.class.getClassLoader().getResourceAsStream(className)) {
            return inputStream.readAllBytes();
        }
    }

    /* Fonction qui crée un JAR à partir d'une liste de classes (les dépendances) */
    public static byte[] createJar(List<String> dependancies) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  // Flux pour écrire le JAR en mémoire

        // Création d'un JarOutpuStream pour structurer chaque classe dans le JAR. Il envoie les données dans la RAM.
        try (JarOutputStream jos = new JarOutputStream(baos)) {

            for (String className : dependancies) {
                // Conversion du nom de la classe en chemin (ex: Agent.AgentImpl -> Agent/AgentImpl.class)
                String classPath = className.replace(".", "/") + ".class";

                byte[] classBytes = loadClassByteCode(classPath);   // Récupération du bytecode de la classe

                JarEntry entry = new JarEntry(classPath);           // Ajout d'une entree dans le jos (un fichier .class)
                entry.setSize(classBytes.length);
                jos.putNextEntry(entry);                            // Ecriture des metadonnees (nom, taille, ...)

                jos.write(classBytes);                              // Ecriture du binaire de la classe
                jos.closeEntry();                                   // Passage au prochain fichier (la classe suivante)
            }

            return baos.toByteArray();                              // Retourne le JAR complet sous forme de tableau d'octets
        }
    }

    /* 
     * Fonction utilisée par le classLoader pour lire un JAR et en extraire une map contenant pour chaque entrée,
     * le nom de la classe et son bytecode.
     */
    public static Map<String, byte[]> readJar(int length, byte[] jar) throws IOException {
        Map<String, byte[]> classList = new HashMap<>();

        // On ouvre un JarInputStream pour lire les entrées du JAR
        try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jar, 0, length))) {
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {
                String classPath = entry.getName();
                // Conversion du chemin en nom de classe (ex: Agent/AgentImpl.class -> Agent.AgentImpl)
                String className = classPath.replace("/", ".").substring(0, classPath.length() - 6);

                // Sur le serveur, on connaît la taille du JAR mais pas de chaque entrée.
                // On a donc besoin d'un flux temporaire pour lire le contenu de chaque entrée.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // Lit tous les octes de l'entrée actuelle (la classe)
                jarStream.transferTo(out);

                // On ajoute la classe et son bytecode dans la map
                classList.put(className, out.toByteArray());
            }
        }
        return classList;
    }
}
