package at.tuwien.ads11.proxy;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import at.tuwien.ads11.remote.IServer;

public class ProxyFactory {

    public static IServer createServerProxy(IServer server) {
        List<IServer> servers = new ArrayList<IServer>();
        servers.add(server);
        
        IServer proxy = (IServer) Proxy.newProxyInstance(
                ServerInvocationHandler.class.getClassLoader(),
                new Class[] { IServer.class },
                new ServerInvocationHandler(servers));
        
        return proxy;
    }
}
