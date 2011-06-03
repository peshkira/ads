package at.tuwien.ads11;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;

//TODO figure out how to forward calls to a failed rmi registry dynamically to another registry
public class ReplicatedServer implements IServer {

    private Registry registry;

    public ReplicatedServer() {
        try {
            IServer stub = (IServer) UnicastRemoteObject.exportObject(this, 0);
            this.registry = LocateRegistry.createRegistry(1099);
            this.registry.rebind(Constants.REMOTE_SERVER_OBJECT_NAME, stub);
            System.out.println("Server bound");

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) {
        try {
            ReplicatedServer server = new ReplicatedServer();
            Thread console = new Thread(new ServerConsole(server));
            console.start();
            console.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean register(String name, String pass) throws RemoteException {
        // TODO Auto-generated method stub
        System.out.println("REMOTE CALL: Registering client with: " + name + " " + pass);
        return true;
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
    
    protected void shutdown() {
      //TODO
        
    }

    // ========= private ===========

    private void init() {
        // connect to the spread deamon
        // check if this is the first server in the group
        // if yes create the proxy and bind it to a registry...
        // if no get the proxy, add this server to it and rebind it...
    }

    private void startRMIRegistry() {
        // if (System.getSecurityManager() == null) {
        // System.setSecurityManager(new SecurityManager());
        // }

        
    }
}
