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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

//TODO figure out how to forward calls to a failed rmi registry dynamically to another registry
public class ReplicatedServer implements IServer{

    private static final long serialVersionUID = -8917839808656077153L;

    private Registry registry;

    private List<Game> games;
    
    private List<Game> playing;

    private Set<ClientMock> clients;

    private transient IServer proxy;
    
    private int rmiPort;
    private int daemonPort;
    private String daemonIP;
    private String serverId;
    
    private transient SpreadConnection spreadCon;
    private transient SpreadGroup serverGroup;
    
    public ReplicatedServer(Properties props) {
    	this.serverId = props.getProperty("serverId");
    	this.rmiPort = Integer.parseInt(props.getProperty("rmiPort"));
    	this.daemonPort = Integer.parseInt(props.getProperty("daemonPort"));
    	this.daemonIP = props.getProperty("daemonIP");
    	this.games = new ArrayList<Game>();
    	this.clients = new HashSet<ClientMock>();
    	this.playing = new ArrayList<Game>();
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
			System.out.println("Config file: " + args[0] + " not found.");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Error in processing the config file.");
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
        boolean add = this.clients.add(client);
        return add;
    }

    /**
     * Works the same way as the register. If the Client is not registered, than the method
     * will just return false.
     */
    @Override
    public boolean unregister(String name, String pass) throws RemoteException {
        ClientMock mock = new ClientMock(name, pass);
        return this.clients.remove(mock);
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
        return this.games.add(g);
    }

    @Override
    public boolean cancelGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);
        return this.games.remove(g);
    }

    @Override
    public Game startGame(String game, String name, String pass) throws RemoteException {
        Game g = new Game(game, name, pass);
        
        for (Game tmp : this.games) {
            if (tmp.equals(g)) {
                g = tmp;
                break;
            }
        }
        
        this.games.remove(g);
        this.playing.add(g);
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

    protected void shutdown() {
        try {
            
            serverGroup.leave();
        	UnicastRemoteObject.unexportObject(this.proxy, true);
            UnicastRemoteObject.unexportObject(this.registry, true);

        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        } catch (SpreadException e) {
        	System.err.println("Error while leaving the server group.");
			e.printStackTrace();
		}

    }

    // ========= private ===========

    private void start() {
        // connect to the spread deamon
        // check if this is the first server in the group
        // if yes create the proxy and bind it to a registry...
        // if no get the proxy, add this server to it and rebind it...
    	connectToSpread();
    	startRMIRegistry(rmiPort);
    }
    
    private void connectToSpread() {
    	spreadCon = new SpreadConnection();
    	spreadCon.add(new MembershipMessageListener(this));
    	serverGroup = new SpreadGroup();
    	
    	
    	try {
			spreadCon.connect(InetAddress.getByName(daemonIP), daemonPort, getServerId(), false, true);
			serverGroup.join(spreadCon, Constants.SPREAD_SERVER_GROUP);
			//this.sendProxyReference(serverGroup);
		} catch (UnknownHostException e) {
			System.err.println("Can not find daemon: " + daemonIP);
			e.printStackTrace();
			System.exit(1);
		} catch (SpreadException e) {
			System.err.println("Error while connecting to Spread.");
			e.printStackTrace();
			System.exit(1);
		}
    }

    private void startRMIRegistry(int port) {
        // if (System.getSecurityManager() == null) {
        // System.setSecurityManager(new SecurityManager());
        // }

        try {
            this.registry = LocateRegistry.createRegistry(port);
            ProxyFactory.getInstance().addServer(this);
            this.rebindProxy();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    
    public void rebindProxy() throws RemoteException {
        this.proxy = ProxyFactory.getInstance().createServerProxy();
        IServer stub = (IServer) UnicastRemoteObject.exportObject(this.proxy, 0);
        this.registry.rebind(Constants.REMOTE_SERVER_OBJECT_NAME, stub);
    }
    
    // the others have add this to the proxy
    // this has to be called everytime a 
    // server joins in order to synchronize
    // the servers...
    public void sendProxyReference(SpreadGroup group) {
       try {
           SpreadMessage message = new SpreadMessage();
           message.setObject((IServer) this);
           message.setSelfDiscard(true);
           message.setReliable();
           message.addGroup(group);
           spreadCon.multicast(message);
    } catch (SpreadException e) {
        e.printStackTrace();
    }
    }
    
    private List<Game> anonymizeGames() {
        List<Game> anonymize = new ArrayList<Game>();
        
        for (Game g : this.games) {
            Game tmp = new Game(g.getName(), g.getHost(), "");
            tmp.setPlayers(g.getPlayers());
        }
        
        return anonymize;
    }


    public String getServerId() {
        return serverId;
    }

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
