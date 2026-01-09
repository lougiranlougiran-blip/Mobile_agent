package Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.ClassLoader;

public class AgentObjectInputStream extends ObjectInputStream {

    private final ClassLoader agentClassLoader;

    public AgentObjectInputStream(ByteArrayInputStream in, ClassLoader loader) throws IOException {
        super(in);
        this.agentClassLoader = loader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return Class.forName(desc.getName(), false, agentClassLoader);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}
