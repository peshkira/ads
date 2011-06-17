package at.tuwien.ads11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.falb.games.alcatraz.api.Alcatraz;
import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.remote.Movement;



public class AlcatrazClient implements IClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(AlcatrazClient.class);

	private Alcatraz alcatraz;
	
	private IServer server;
    
    private String username;
    private String password;
    private String ip;
    private String proxyIp;
    
    private int port;
    private int proxyPort;
    
    public AlcatrazClient(Properties props) {
        this.alcatraz = new Alcatraz();
        this.username = props.getProperty("client.username");
        this.password = props.getProperty("client.password");
        this.ip = props.getProperty("client.ip");
        this.port = Integer.parseInt(props.getProperty("client.port"));
        this.proxyIp = props.getProperty("proxy.ip");
        this.proxyPort = Integer.parseInt(props.getProperty("proxy.port"));
    }

    public static void main(String args[]) {
        if (args == null || args.length != 1) {
            System.out.println("Invalid argument count - provide name of the config file.");
            System.exit(1);
        }

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(args[0]));
        } catch (FileNotFoundException e) {
            LOG.error("Config file: {} not found.", args[0]);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            LOG.error("Error in processing the config file.");
            System.exit(1);
        }

        AlcatrazClient client = new AlcatrazClient(props);
        Thread console = new Thread(new ClientConsole(client));
        client.start();
        console.start();

    }

    @Override
    public void startGame(Game game) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public void doMove(Movement m) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Movement> getHistory() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    public void shutdown() {
        this.alcatraz.disposeWindow();

    }

    public void register() {
        try {
            boolean register = this.server.register(this.username, this.password);

            if (register) {
                System.out.println("You are successfully registered");
            } else {
                System.out.println("You were not registered. It seems you are already registered");
            }

        } catch (RemoteException e) {
            LOG.error("An error occurred. No Repsonse from server. Cause:{}", e.getMessage());
            System.out.println("An error occurred. The server was unreachable. Please try again later.");
        }
    }

    public void unregister() {
        try {
            boolean unregister = this.server.unregister(this.username, this.password);

            if (unregister) {
                System.out.println("You signed out successfully");
            } else {
                System.out
                        .println("You did not sign out. It seems you are trying to sign out without registering first");
            }

        } catch (RemoteException e) {
            LOG.error("An error occurred. No Repsonse from server. Cause:{}", e.getMessage());
            System.out.println("An error occurred. The server was unreachable. Please try again later.");
        }
    }

    public void fetchGames() {
        try {
            List<Game> games = this.server.fetchGames();
            System.out.println("Name \t Nr. Players");
            for (Game g : games) {
                System.out.println(g.getName() + "\t" + g.getPlayers().size());
            }
            
        } catch (RemoteException e) {
            LOG.error("An error occurred. The server was unreachable. Cause:{}", e.getMessage());
            System.out.println("An error occurred. The server was unreachable. Please try again later.");
        }
    }

    public void createGame(String name) {

    }

    public void cancelGame(String name) {

    }

    public void joinGame(String name) {

    }

    public void leaveGame(String name) {

    }

    // Server
    public void startGame(String name) {

    }

    private void start() {
        this.getServerProxy();
        // something else?
    }

    private void getServerProxy() {
        try {
            this.server = (IServer) Naming.lookup("rmi://" + "localhost" + ":" + 1234 + "/"
                    + Constants.REMOTE_SERVER_OBJECT_NAME);
        } catch (MalformedURLException e) {
            LOG.error("An error occurred, the server uri seem to be malformed. Cause: {}", e.getMessage());
        } catch (RemoteException e) {
            LOG.error("An error occurred, the server seems to be unreachable. Cause: {}", e.getMessage());
        } catch (NotBoundException e) {
            LOG.error("An error occurred, the server seems to be unreachable. Cause: {}", e.getMessage());
        }
    }

}
