package at.tuwien.ads11;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.tuwien.ads11.remote.IServer;

public class TestClient {

    public static void main(String[] args) {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        try {
            String name = "Server";
            Registry registry = LocateRegistry.getRegistry();
            IServer server = (IServer) registry.lookup(name);
            boolean registered = server.register("test", "pass");
            if (registered) {
                System.out.println("I am registered");
            } else {
                System.out.println("I am not registered");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
