package at.tuwien.ads11.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import at.tuwien.ads11.remote.IServer;

public class ServerInvocationHandler implements InvocationHandler {
    
    Set<IServer> servers;
    
    Set<IServer> calls;
    
    public ServerInvocationHandler(Set<IServer> servers) {
        this.servers = servers;
    }

    // THIS DOES NOT WORK PROPERLY BECAUSE THE FRIGGIN SPREAD CONNECTION IS NOT SERIALIZED.
    // THE ONLY IDEA I HAVE IS TO START AN RMI REGISTRY FOR ALL SERVERS (after all)
    // AND PUT EACH SERVER IN THE REGISTRY...
    // THEN THE PROXY WILL HAVE TO INVOKE THE MEHTOD REMOTELY TOO..
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        System.out.println("invoking: " + m.getName());
        this.calls = servers;
        Iterator<IServer> iter = this.calls.iterator();
        
        while (iter.hasNext()) {
            try {
                IServer next = iter.next();
                return m.invoke(next, args);
            } catch (Exception e) {
                e.printStackTrace(); //for debug
                iter.remove();
            }
        }
        
        throw new Exception("Could not reach the server. Please try again later ");
        
    }
}
