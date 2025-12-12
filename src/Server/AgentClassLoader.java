package Server;

import Agent.JarFactory;

import java.io.IOException;

import java.util.Map;

public class AgentClassLoader extends ClassLoader  {

    private final Map<String, byte[]> classCache;

    public AgentClassLoader(byte[] jarData, int length) throws IOException {
        this.classCache = JarFactory.readJar(length, jarData);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classBytes = classCache.get(name);

        if (classBytes == null) throw new ClassNotFoundException(name);

        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
