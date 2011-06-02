package at.tuwien.ads11;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ReplicatedServer implements IServer {

    public ReplicatedServer() {
        
    }
    
    public static void main(String args[]) {
        
    }
    
    @Override
    public boolean register(String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
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
    
    private void startRMIRegistry() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
