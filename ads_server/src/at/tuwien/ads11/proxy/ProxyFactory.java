package at.tuwien.ads11.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
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
    
    public void addServer(IServer server) {
        servers.add(server);
    }
    
    private ProxyFactory() {
        this.servers = new HashSet<IServer>();
    }
}
