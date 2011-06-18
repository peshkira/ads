package at.tuwien.ads11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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

    private List<ClientMock> clients;
    private List<IClient> clientStubCache;
    private List<Movement> history;

    private Registry registry;

    private IClient stub;

    private boolean registered;
    private boolean runningGame;
    private String hostingGame;
    private String joinedGame;

    private ClientSynchronizer synchronizer;

    private int numPlayers;

    public AlcatrazClient(Properties props) {
        this.alcatraz = new Alcatraz();
        this.username = props.getProperty("client.username");
        this.password = props.getProperty("client.password");
        this.ip = props.getProperty("client.ip");
        this.port = Integer.parseInt(props.getProperty("client.port"));
        this.proxyIp = props.getProperty("proxy.ip");
        this.proxyPort = Integer.parseInt(props.getProperty("proxy.port"));
        this.setClientStubCache(new LinkedList<IClient>());
        this.history = new ArrayList<Movement>();
        this.registered = false;
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
        numPlayers = game.getPlayers().size();
        int numId = -1;

        this.setClients(game.getPlayers());
        ClientMock tmp = new ClientMock(this.username, this.password);

        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (tmp.getName().equals(game.getPlayers().get(i).getName())) {
                numId = i;
                clients.remove(i);
            }
        }

        
        if (numId < 0 || numId > 3) {
            // TODO error handling
            LOG.error("numId is out of Range {}", numId);
        }

        LOG.debug("My NumId is {}", numId);
        if (this.getClientStubCache().size() == 0) {
            this.initRemoteStubs(game.getPlayers());
        }

        this.alcatraz.init(numPlayers, numId);
        this.alcatraz.addMoveListener(new ClientMoveListener(this));
        this.alcatraz.start();

        this.synchronizer = new ClientSynchronizer(this, 5000, numId);
        Thread sync = new Thread(this.synchronizer);
        sync.start();

        this.alcatraz.showWindow();
    }

    @Override
    public void doMove(Movement m) throws RemoteException {
        if (this.getLocalHistory().size() != 0) {
            Movement lastKnown = this.getLocalHistory().get(this.getLocalHistory().size() - 1);
            if (m.getPlayer().getId() != this.getExpectedTurnId(lastKnown.getPlayer().getId())) {
                LOG.warn("Something must have gone wrong. Expected player id is {}, moving player id was {}",
                        this.getExpectedTurnId(lastKnown.getPlayer().getId()), m.getPlayer().getId());
                this.synchronizer.synchronize();
            } else {
                this.applyMove(m);
            }
        } else {
            this.applyMove(m);
        }

    }

    private int getExpectedTurnId(int lastKnown) {
        int result = -1;
        switch (lastKnown) {
        case 0:
            result = 1;
            break;
        case 1:
            result = (numPlayers == 2) ? 0 : 2;
            break;
        case 2:
            result = (numPlayers == 3) ? 0 : 3;
            break;
        case 3:
            result = 0;
            break;
        }

        return result;
    }

    @Override
    public List<Movement> getHistory() throws RemoteException {
        return this.history;
    }

    public List<Movement> getLocalHistory() {
        return this.history;
    }

    public void shutdown() {
        this.synchronizer.setRun(false);
        this.alcatraz.disposeWindow();
        try {
            UnicastRemoteObject.unexportObject(this, true);
            UnicastRemoteObject.unexportObject(this.registry, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }

    public void register() throws RemoteException {
        if (registered) {
            System.out.println("You are already registered.");
            return;
        }

        ClientMock mock = new ClientMock(this.username, this.password);
        mock.setHost(this.ip);
        mock.setPort(this.port);
        registered = this.server.register(mock);

        if (registered)
            System.out.println("You are successfully registered");
        else
            System.out.println("You were not registered. It seems you are already registered");
    }

    public void unregister() throws RemoteException {
        if (!registered) {
            System.out.println("You have to register first.");
            return;
        }

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
        // TODO: fix it on server side, set hostingGame via exception
        if (hostingGame != null) {
            System.out.println("You are already hosting game: " + hostingGame);
            return;
        }

        boolean created = this.server.createGame(name, this.username, this.password);
        if (created) {
            hostingGame = name;
            System.out
                    .println("You created a game successfully. Check out the games in order to see if somebody joined");
        } else
            System.out.println("The Game could not be created");
    }

    public void cancelGame(String name) throws RemoteException {
        // TODO: nonexistent game exception, return hosted game if any
        boolean cancelled = this.server.cancelGame(name, this.username, this.password);
        if (cancelled)
            System.out.println("Game " + name + " was successfully cancelled.");
        else
            System.out.println("Game " + name + " could not be cancelled.");
    }

    public void joinGame(String name) throws RemoteException {
        if (hostingGame != null) {
            System.out.println("You can not join other games while you are hosting another(" + hostingGame + ")");
            return;
        }

        if (joinedGame != null) {
            System.out.println("You have to leave game " + joinedGame + " first.");
            return;
        }

        boolean joined = this.server.joinGame(name, this.username, this.password);
        if (joined) {
            joinedGame = name;
            System.out.println("You have successfully joined the game: " + name);
        } else
            System.out.println("Game " + name + " could not be joined.");
    }

    public void leaveGame(String name) throws RemoteException {
        // TODO: nonexistent game exception, return joined game if any
        boolean left = this.server.leaveGame(name, this.username, this.password);
        if (left)
            System.out.println("You have left the game: " + name);
        else
            System.out.println("Game " + name + " could not be left.");
    }

    // Server
    public void startGame(String name) throws RemoteException {
        if (runningGame) {
            System.out.println("You are already running game: " + hostingGame != null ? hostingGame : joinedGame);
            return;
        }
        Game game = this.server.startGame(name, this.username, this.password);

        // IF rejoining running game...
        if (name.length() < 1) {
            this.startGame(game);
            System.out.println("Game: " + game.getName() + " rejoined.");
            runningGame = true;
            return;

        }

        if (game != null) {
            this.initRemoteStubs(game.getPlayers());

            for (IClient c : this.getClientStubCache()) {
                callStartGameOnClient(c, game);
            }
            runningGame = true;
            this.startGame(game);
            System.out.println("Game " + name + " has been successfully started.");
        } else
            System.out.println("Game " + name + " could not be started.");
    }

    private void initRemoteStubs(List<ClientMock> clients) {
        List<ClientMock> unreachableClients = new ArrayList<ClientMock>();
        for (ClientMock client : clients) {
            if (client.getName().equals(this.username))
                continue;
            else {
                try {
                    IClient clientStub = this.getStub(client);
                    getClientStubCache().add(clientStub);
                } catch (Exception e) {
                    unreachableClients.add(client);
                }

            }
        }

        while (!unreachableClients.isEmpty()) {
            Iterator<ClientMock> it = unreachableClients.iterator();
            while (it.hasNext()) {
                try {
                    IClient stub = this.getStub(it.next());
                    if (stub != null) {
                        it.remove();
                        getClientStubCache().add(stub);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public IClient getStub(ClientMock client) throws Exception {
        LOG.debug("Getting Stub of {}:{}", client.getHost(), client.getPort());
        IClient clientStub = (IClient) Naming.lookup("rmi://" + client.getHost() + ":" + client.getPort() + "/"
                + Constants.REMOTE_CLIENT_OBJECT_NAME);

        return clientStub;
    }
    
    public boolean refreshCache() {
        List<IClient> cache = new ArrayList<IClient>();
        for (ClientMock client : clients) {
            if (client.getName().equals(this.username))
                continue;
            else {
                try {
                    IClient clientStub = this.getStub(client);
                    cache.add(clientStub);
                } catch (Exception e) {
                }
            }
        }
        
        if (cache.size() == clientStubCache.size()) {
            System.out.println("refresh cache");
            this.setClientStubCache(cache);
            return true;
        }
        
        return false;
    }

    private void callStartGameOnClient(IClient clientStub, Game game) {
        try {
            clientStub.startGame(game);
        } catch (Exception e) {
            LOG.error("ERROR {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void start() {
        this.getServerProxy();
        this.startRMIRegistry();
    }

    private void startRMIRegistry() {
        try {
            this.registry = LocateRegistry.createRegistry(this.port);
            this.stub = (IClient) UnicastRemoteObject.exportObject(this, 0);
            this.registry.rebind(Constants.REMOTE_CLIENT_OBJECT_NAME, this.stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void getServerProxy() {
        try {
            this.server = (IServer) Naming.lookup("rmi://" + this.proxyIp + ":" + this.proxyPort + "/"
                    + Constants.REMOTE_SERVER_OBJECT_NAME);
        } catch (MalformedURLException e) {
            LOG.error("An error occurred, the server uri seem to be malformed. Cause: {}", e.getMessage());
        } catch (RemoteException e) {
            LOG.error("An error occurred, the server seems to be unreachable. Cause: {}", e.getMessage());
        } catch (NotBoundException e) {
            LOG.error("An error occurred, the server seems to be unreachable. Cause: {}", e.getMessage());
        }
    }

    public void setClientStubCache(List<IClient> clientStubCache) {
        this.clientStubCache = clientStubCache;
    }

    public List<IClient> getClientStubCache() {
        return clientStubCache;
    }

    public void applyMove(Movement m) {
        LOG.info("applying movement: {}", m);
        this.alcatraz.doMove(m.getPlayer(), m.getPrisoner(), m.getRowOrCol(), m.getRow(), m.getCol());
        this.history.add(m);

    }

    public void setClients(List<ClientMock> clients) {
        this.clients = clients;
    }

    public List<ClientMock> getClients() {
        return clients;
    }
    
    public String toString() {
        return this.username;
    }
}
