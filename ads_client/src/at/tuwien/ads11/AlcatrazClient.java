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
import at.tuwien.ads11.listener.ClientMoveListener;
import at.tuwien.ads11.remote.ClientMock;
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
        int numPlayers = game.getPlayers().size();
        int numId = -1;
        ClientMock tmp = new ClientMock(this.username, this.password);
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (tmp.equals(game.getPlayers().get(i))) {
                numId = i;
            }
        }

        if (numId < 0 || numId > 3) {
            // TODO error handling
        }

        this.alcatraz.init(numPlayers, numId);
        this.alcatraz.addMoveListener(new ClientMoveListener());

        // is there something else to do here?
        // check if you are the first one and move....?
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

    public void register() throws RemoteException {
        ClientMock mock = new ClientMock(this.username, this.password);
        mock.setHost(this.ip);
        mock.setPort(this.port);
        boolean register = this.server.register(mock);

        if (register)
            System.out.println("You are successfully registered");
        else
            System.out.println("You were not registered. It seems you are already registered");
    }

    public void unregister() throws RemoteException {
        boolean unregister = this.server.unregister(this.username, this.password);

        if (unregister)
            System.out.println("You signed out successfully");
        else
            System.out.println("You did not sign out. It seems you are trying to sign out without registering first");

    }

    public void fetchGames() throws RemoteException {
        List<Game> games = this.server.fetchGames();
        System.out.println("Name \t Nr. Players");
        if (games.isEmpty())
            System.out.println("No games available at the moment.");
        for (Game g : games) {
            System.out.println(g.getName() + "\t" + g.getPlayers().size());
        }

    }

    public void createGame(String name) throws RemoteException {
        boolean created = this.server.createGame(name, this.username, this.password);
        if (created)
            System.out
                    .println("You created a game successfully. Check out the games in order to see if somebody joined");
        else
            System.out.println("The Game could not be created");
    }

    public void cancelGame(String name) throws RemoteException {
        boolean cancelled = this.server.cancelGame(name, this.username, this.password);
        if (cancelled)
            System.out.println("Game " + name + " was successfully cancelled.");
        else
            System.out.println("Game " + name + "could not be cancelled.");
    }

    public void joinGame(String name) throws RemoteException {
        boolean joined = this.server.joinGame(name, this.username, this.password);
        if (joined)
            System.out.println("You have successfully joined the game: " + name);
        else
            System.out.println("Game " + name + " could not be joined.");
    }

    public void leaveGame(String name) throws RemoteException {
        boolean left = this.server.leaveGame(name, this.username, this.password);
        if (left)
            System.out.println("You have left the game: " + name);
        else
            System.out.println("Game " + name + " could not be left.");
    }

    // Server
    public void startGame(String name) throws RemoteException {
        Game game = this.server.startGame(name, this.username, this.password);
        if (game != null) {
            System.out.println("Game " + name + " has been successfully started.");
            for (ClientMock client : game.getPlayers())
                ;// Call startgame on each client
        } else
            System.out.println("Game " + name + " could not be started.");
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
