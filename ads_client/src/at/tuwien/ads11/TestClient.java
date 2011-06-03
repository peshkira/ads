package at.tuwien.ads11;

import java.rmi.Naming;

import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.remote.IServer;

public class TestClient {

    public static void main(String[] args) {
        // if (System.getSecurityManager() == null) {
        // System.setSecurityManager(new SecurityManager());
        // }

        if (args.length != 2) {
            System.exit(1);
        }

        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);

            IServer server = (IServer) Naming.lookup("rmi://" + host + ":" + port + "/"
                    + Constants.REMOTE_SERVER_OBJECT_NAME);
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
