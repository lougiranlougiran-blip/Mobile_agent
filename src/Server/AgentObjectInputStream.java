package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.lang.ClassLoader;

public class AgentObjectInputStream extends ObjectInputStream {

    private final ClassLoader agentClassLoader;

    public AgentObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.agentClassLoader = loader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return agentClassLoader.loadClass(desc.getName());
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}