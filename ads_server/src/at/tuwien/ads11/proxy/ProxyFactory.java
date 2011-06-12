package at.tuwien.ads11.proxy;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.RMIServerInfo;

public final class ProxyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyFactory.class);
    
    private static ProxyFactory uniqueInstance;
    
    private Set<RMIServerInfo> servers;
    
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
    
    public boolean addServer(RMIServerInfo server) {
        boolean added = servers.add(server);
        if (added) {
            LOG.info("Server {} is now known the to proxy", server.toString());
        }
        
        return added;
    }
    
    private ProxyFactory() {
        this.servers = new HashSet<RMIServerInfo>();
    }
}
