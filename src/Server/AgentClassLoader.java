package Server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AgentClassLoader extends ClassLoader  {

    /*
    @TODO

    À REFAIRE

    Le ClassLoader doit lire depuis un byte[] pas un fichier.
    Regarder comment est fait JarFactory
     */

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = loadClassFromFile(name);
        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadClassFromFile(String fileName)  {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                fileName.replace('.', File.separatorChar) + ".class");
        byte[] buffer;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = 0;

        try {
            while ((nextValue = inputStream.read()) != -1 ) {
                byteStream.write(nextValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buffer = byteStream.toByteArray();
        return buffer;
    }

}
