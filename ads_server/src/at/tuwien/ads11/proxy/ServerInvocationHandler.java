package at.tuwien.ads11.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.RMIServerInfo;
import at.tuwien.ads11.utils.ServerConstants;

public class ServerInvocationHandler implements InvocationHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(ServerInvocationHandler.class);

    Set<RMIServerInfo> servers;

    Set<RMIServerInfo> calls;

    public ServerInvocationHandler(Set<RMIServerInfo> servers) {
        this.servers = servers;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        LOG.info("Invoking method {}() ", m.getName());
        this.calls = servers;
        Iterator<RMIServerInfo> iter = this.calls.iterator();

        while (!this.calls.isEmpty()) {
            RMIServerInfo next = this.getNextServerNonDeterministically();
            try {
                LOG.debug("on replicated server {}:{}", next.getHost(), next.getPort());

                IServer server = (IServer) Naming.lookup("rmi://" + next.getHost() + ":" + next.getPort() + "/"
                        + ServerConstants.SERVER_OBJECT);

                return m.invoke(server, args);
            } catch (Exception e) {
                LOG.warn("Server not reachable. Cause: {}", e.getMessage());
                iter.remove();
            }
        }

        LOG.error("None of the replicated servers could be reached, throwing exception to client");
        throw new RemoteException("Could not reach the server. Please try again later ");

    }

    private RMIServerInfo getNextServerNonDeterministically() {
        int size = this.calls.size();
        Iterator<RMIServerInfo> iter = this.calls.iterator();
        if (size > 1) {
            int pos = new Random().nextInt(100) % size;
            System.out.println("SERVER POS: " + pos);
            while (iter.hasNext()) {
                if (pos == 0) {
                    return iter.next();
                }
                iter.next();
                pos--;
            }
        }
        return iter.next();
    }
}
