package Agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarFactory {

    private final List<String> dependancies;

    public JarFactory(List<String> dependancies) {
        this.dependancies = dependancies;
    }

    private byte[] loadClassByteCode(String className) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(className + ".class")) {
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

    public byte[] createJar() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (JarOutputStream jos = new JarOutputStream(baos)) {

            for (String className : dependancies) {
                byte[] classBytes = loadClassByteCode(className);

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
}
