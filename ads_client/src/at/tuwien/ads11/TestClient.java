package at.tuwien.ads11;

import java.rmi.Naming;
import java.util.List;

import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.remote.Game;
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
            
            boolean created = server.createGame("MyGame", "test", "pass");
            System.out.println("Game created: " + created);
            
            
            List<Game> games = server.fetchGames();
            System.out.println("Games: " + games.size());
            
            if (games.size() > 0) {
                System.out.println(games.get(0).getName());
                System.out.println(games.get(0).getHost());
            }
            
            boolean cancelGame = server.cancelGame("MyGame", "test", "pass");
            System.out.println("Game cancelled: " + cancelGame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
