package at.tuwien.ads11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.listener.MembershipMessageListener;
import at.tuwien.ads11.proxy.ProxyFactory;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.RequestUUID;
import at.tuwien.ads11.utils.ResultPoller;
import at.tuwien.ads11.utils.ServerConstants;
import at.tuwien.ads11.utils.ServerMessageFactory;

//TODO figure out how to forward calls to a failed rmi registry dynamically to another registry
public class ReplicatedServer implements IServer {

    private static final long serialVersionUID = -8917839808656077153L;

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedServer.class);

    private ServerState state;

    private int rmiPort;
    private int daemonPort;
    private String daemonIP;
    private String serverId;
    private boolean adminsProxy;

    private transient Registry registry;
    private transient SpreadConnection spreadCon;
    private SpreadGroup serverGroup;
    private SpreadGroup ownGroup;
    private transient IServer proxy;
    private transient Map<RequestUUID, Object> requests;

    public ReplicatedServer(Properties props) {
        this.serverId = props.getProperty("server.id");

        this.adminsProxy = Boolean.parseBoolean(props.getProperty("server.proxy.administer"));
        this.rmiPort = Integer.parseInt(props.getProperty("server.rmi.port"));

        this.daemonPort = Integer.parseInt(props.getProperty("spread.daemon.port"));
        this.daemonIP = props.getProperty("spraed.daemon.ip");

        this.state = new ServerState();
        this.requests = new HashMap<RequestUUID, Object>();
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

        ReplicatedServer server = new ReplicatedServer(props);
        Thread console = new Thread(new ServerConsole(server));

        server.start();
        console.start();

    }

    /**
     * Wraps the info into a client mock object and adds it to the set. If the
     * client is already register it doesn't matter as it won't be added again
     * the the set. ClientMock equals will return true...
     * 
     * If however the pass is different, than the client will be considered as a
     * new one.
     */
    @Override
    public boolean register(String name, String pass) throws RemoteException {
        ClientMock client = new ClientMock(name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());
        try {

            SpreadMessage message = new ServerMessageFactory().getDefaultMessage();
            message.setType(ServerConstants.MSG_PLAYER_REGISTER);
            message.digest(client);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }
        
        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        
        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }
    
    public boolean register(ClientMock mock) {
        return this.state.addClient(mock);
    }

    /**
     * Works the same way as the register. If the Client is not registered, than
     * the method will just return false.
     */
    @Override
    public boolean unregister(String name, String pass) throws RemoteException {
        ClientMock mock = new ClientMock(name, pass);
        return this.state.removeClient(mock);
    }

    /**
     * Returns the current list of games that are not started yet.
     */
    @Override
    public List<Game> fetchGames() throws RemoteException {
        return this.anonymizeGames();
    }

    @Override
    public boolean createGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);
        return this.state.addGame(g);
    }

    @Override
    public boolean cancelGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);
        return this.state.removeGame(g);
    }

    @Override
    public Game startGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);

        for (Game tmp : this.state.getGames()) {
            if (tmp.equals(g)) {
                g = tmp;
                break;
            }
        }

        this.state.removeGame(g);
        this.state.addRunningGame(g);
        return g;
    }

    @Override
    public boolean joinGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);

        return false;
    }

    @Override
    public boolean leaveGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }
    
    public Map<RequestUUID, Object> getRequests() {
        return this.requests;
    }

    protected void shutdown() {
        try {

            serverGroup.leave();

            if (adminsProxy) {
                UnicastRemoteObject.unexportObject(this.proxy, true);
                UnicastRemoteObject.unexportObject(this.registry, true);
            }

        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        } catch (SpreadException e) {
            LOG.error("Error while leaving the server group.");
            e.printStackTrace();
        }

        // consider to kill the process here.
    }

    public void sendProxyReference(SpreadGroup group) {
        try {
            SpreadMessage message = new ServerMessageFactory().getDefaultMessage();
            message.addGroup(group);
            message.setType(ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE);
            message.setObject(this);
            spreadCon.multicast(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    public void receiveServerReference(IServer server) {
        this.rebindProxy(new IServer[] { server });
    }

    public void askForServerReference() {
        if (this.adminsProxy) {
            LOG.info("Asking for Server References to refresh proxy");

            try {
                SpreadMessage message = new ServerMessageFactory().getDefaultMessage();
                message.addGroup(serverGroup);
                message.setType(ServerConstants.MSG_GET_SERVER_REFERENCE);
                spreadCon.multicast(message);
            } catch (SpreadException e) {
                e.printStackTrace();
                // what to do here...
            }
        }
    }

    // ========= private ===========

    private void start() {
        connectToSpread();
        if (adminsProxy) {
            getRMIRegistry();
        }
    }

    private void connectToSpread() {
        spreadCon = new SpreadConnection();
        spreadCon.add(new MembershipMessageListener(this));
        serverGroup = new SpreadGroup();
        ownGroup = new SpreadGroup();

        try {
            spreadCon.connect(InetAddress.getByName(daemonIP), daemonPort, getServerId(), false, true);
            serverGroup.join(spreadCon, ServerConstants.SPREAD_SERVER_GROUP);
            ownGroup.join(spreadCon, serverId);	
        } catch (UnknownHostException e) {
            LOG.error("Can not find daemon: {}", daemonIP);
            System.err.println();
            e.printStackTrace();
            System.exit(1);
        } catch (SpreadException e) {
            LOG.error("Error while connecting to Spread.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void getRMIRegistry() {
        try {
            this.registry = LocateRegistry.createRegistry(this.rmiPort);

        } catch (RemoteException e) {
            e.printStackTrace();
            LOG.error("System will exit now");
            System.exit(1);
        }
    }

    private void rebindProxy(IServer... servers) {
        boolean rebind = false;
        for (IServer server : servers) {
            if (ProxyFactory.getInstance().addServer(server)) {
                rebind = true;
            }
        }

        try {
            if (rebind) {
                LOG.info("Rebinding proxy...");

                if (this.proxy != null)
                    UnicastRemoteObject.unexportObject(this.proxy, true);

                this.proxy = ProxyFactory.getInstance().createServerProxy();
                IServer stub = (IServer) UnicastRemoteObject.exportObject(proxy, 0);
                this.registry.rebind(Constants.REMOTE_SERVER_OBJECT_NAME, stub);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private List<Game> anonymizeGames() {
        List<Game> anonymize = new ArrayList<Game>();

        for (Game g : this.state.getGames()) {
            Game tmp = new Game(g.getName(), g.getHost(), "");
            tmp.setPlayers(g.getPlayers());
        }

        return anonymize;
    }

    public String getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
    	return this.serverId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((daemonIP == null) ? 0 : daemonIP.hashCode());
        result = prime * result + daemonPort;
        result = prime * result + rmiPort;
        result = prime * result + ((serverId == null) ? 0 : serverId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReplicatedServer other = (ReplicatedServer) obj;
        if (daemonIP == null) {
            if (other.daemonIP != null)
                return false;
        } else if (!daemonIP.equals(other.daemonIP))
            return false;
        if (daemonPort != other.daemonPort)
            return false;
        if (rmiPort != other.rmiPort)
            return false;
        if (serverId == null) {
            if (other.serverId != null)
                return false;
        } else if (!serverId.equals(other.serverId))
            return false;
        return true;
    }

}
