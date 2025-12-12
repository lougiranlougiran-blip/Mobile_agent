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

    private static byte[] loadClassByteCode(String className) throws IOException {
        try (InputStream inputStream = JarFactory.class.getClassLoader().getResourceAsStream(className + ".class")) {
            byte[] buffer;
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            int nextValue;

            try {
                while ((nextValue = inputStream.read()) != -1) {
                    bs.write(nextValue);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            buffer = bs.toByteArray();

            return buffer;
        }
    }

    public static byte[] createJar(List<String> dependancies) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (JarOutputStream jos = new JarOutputStream(baos)) {

            for (String className : dependancies) {
                byte[] classBytes = loadClassByteCode(className);

                // @TODO former la bonne string
                String entryName = className + ".class";            // Creer une entree JAR
                JarEntry entry = new JarEntry(entryName);           // Ajout de l'entree
                entry.setSize(classBytes.length);
                jos.putNextEntry(entry);                            // Ecriture des metadonnees (nom, taille, ...)

                jos.write(classBytes);                              // Ecriture du binaire de la classe
                jos.closeEntry();                                   // Passage au prochain fichier (la classe)
            }

            return baos.toByteArray();
        }
    }

    public static Map<String, byte[]> readJar(int length, byte[] jar) throws IOException {
        Map<String, byte[]> classList = new HashMap<>();
        ByteArrayInputStream codeStream = new ByteArrayInputStream(jar, 0, length);

        try (JarInputStream jarStream = new JarInputStream(codeStream)) {
            JarEntry entry;

            while ((entry = jarStream.getNextJarEntry()) != null) {
                // @TODO former la bonne string
                String className = entry.getName();
                byte[] classBytes = jarStream.readAllBytes();       // Lit les bytes uniquement de l'entree courante

                classList.put(className, classBytes);
            }
        }
        return classList;
    }
}
