package at.tuwien.ads11;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;

//TODO figure out how to forward calls to a failed rmi registry dynamically to another registry
public class ReplicatedServer implements IServer {

    public ReplicatedServer() {
        
    }
    
    public static void main(String args[]) {
        startRMIRegistry();
    }
    
    @Override
    public boolean register(String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        System.out.println("You are registered");
        return false;
    }

    @Override
    public boolean unregister(String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Game> fetchGames() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean createGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean cancelGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Game startGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean joinGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean leaveGame(String game, String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }
    
    //========= private ===========
    
    private void init() {
        // connect to the spread deamon
        // check if this is the first server in the group
        // if yes create the proxy and bind it to a registry...
        // if no get the proxy, add this server to it and rebind it...
    }
    
    private static void startRMIRegistry() {
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
        
        try {
            String name = "Server";
            IServer server = new ReplicatedServer();
            IServer stub =
                (IServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind(name, stub);
            System.out.println("Server bound");    
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
