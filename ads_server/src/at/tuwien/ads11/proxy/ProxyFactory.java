package at.tuwien.ads11.proxy;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import at.tuwien.ads11.remote.IServer;

public final class ProxyFactory {

    private static ProxyFactory uniqueInstance;
    
    private Set<IServer> servers;
    
    public static synchronized ProxyFactory getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new ProxyFactory();
        }
        
        return uniqueInstance;
    }
    
    public IServer createServerProxy() {
        IServer proxy = (IServer) Proxy.newProxyInstance(
                ServerInvocationHandler.class.getClassLoader(),
                new Class[] { IServer.class },
                new ServerInvocationHandler(this.servers));
        
        return proxy;
    }
    
    public boolean addServer(IServer server) {
        boolean added = servers.add(server);
        if (added) {
            System.out.println("New Server is known to the proxy: " + server.toString());
        }
        
        return added;
    }
    
    private ProxyFactory() {
        this.servers = new HashSet<IServer>();
    }
}
