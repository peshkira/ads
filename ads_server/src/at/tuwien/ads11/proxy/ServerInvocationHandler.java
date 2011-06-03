package at.tuwien.ads11.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import at.tuwien.ads11.remote.IServer;

public class ServerInvocationHandler implements InvocationHandler {
    
    List<IServer> servers;
    
    public ServerInvocationHandler(List<IServer> servers) {
        this.servers = servers;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        // TODO Auto-generated method stub
        return null;
    }

}
