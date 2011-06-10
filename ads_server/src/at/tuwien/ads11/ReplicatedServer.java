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
import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.proxy.ProxyFactory;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;

//TODO figure out how to forward calls to a failed rmi registry dynamically to another registry
public class ReplicatedServer implements IServer {

    private Registry registry;
    
    private List<Game> games;
    
    private Set<ClientMock> clients;

    private IServer proxy;
    
    private int rmiPort;
    private int daemonPort;
    private String daemonIP;
    private String serverId;
    
    private SpreadConnection spreadCon;
    private SpreadGroup serverGroup;
    
    //TEST MSGS
    recThread rt;

    public ReplicatedServer(Properties props) {
    	this.serverId = props.getProperty("serverId");
    	this.rmiPort = Integer.parseInt(props.getProperty("rmiPort"));
    	this.daemonPort = Integer.parseInt(props.getProperty("daemonPort"));
    	this.daemonIP = props.getProperty("daemonIP");
    	this.games = new ArrayList<Game>();
    	this.clients = new HashSet<ClientMock>();
       
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
        server.start();
        Thread console = new Thread(new ServerConsole(server));
        console.start();

    }

    @Override
    public boolean register(String name, String pass) throws RemoteException {
        ClientMock client = new ClientMock(name, pass);
        boolean add = this.clients.add(client);
        return add;
    }

    @Override
    public boolean unregister(String name, String pass) throws RemoteException {
        ClientMock mock = new ClientMock(name, pass);
        return this.clients.remove(mock);
    }

    @Override
    public List<Game> fetchGames() throws RemoteException {
        return this.games;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean joinGame(String game, String name, String pass) throws RemoteException {
        
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
            rt.run = false;
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
    	serverGroup = new SpreadGroup();
    	
    	try {
			spreadCon.connect(InetAddress.getByName(daemonIP), daemonPort, serverId, false, true);
			// TEST MSGS
			rt = new recThread(spreadCon);
			rt.start();
			serverGroup.join(spreadCon, Constants.SPREAD_SERVER_GROUP);
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

            this.proxy = ProxyFactory.createServerProxy(this);
            IServer stub = (IServer) UnicastRemoteObject.exportObject(proxy, 0);
            this.registry.rebind(Constants.REMOTE_SERVER_OBJECT_NAME, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    
}
