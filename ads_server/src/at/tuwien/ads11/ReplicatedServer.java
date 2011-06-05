package at.tuwien.ads11;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public ReplicatedServer(int port) {
       this.startRMIRegistry(port);
       this.games = new ArrayList<Game>();
       this.clients = new HashSet<ClientMock>();
       
    }

    public static void main(String args[]) {

        if (args == null || args.length != 1) {
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ReplicatedServer server = new ReplicatedServer(port);
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
            UnicastRemoteObject.unexportObject(this.proxy, true);
            UnicastRemoteObject.unexportObject(this.registry, true);
            
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }

    }

    // ========= private ===========

    private void init() {
        // connect to the spread deamon
        // check if this is the first server in the group
        // if yes create the proxy and bind it to a registry...
        // if no get the proxy, add this server to it and rebind it...
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
