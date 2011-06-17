package at.tuwien.ads11;

import java.rmi.RemoteException;
import java.util.List;

import at.falb.games.alcatraz.api.Alcatraz;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.Movement;

public class AlcatrazClient implements IClient {
    
    private Alcatraz alcatraz;
    
    private String name;
    
    private String pass;

    public AlcatrazClient(String name, String pass) {
        this.alcatraz = new Alcatraz();
        this.name = name;
        this.pass = pass;
    }
    
    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Usage: java AlcatrazClient [name] [pass]");
            System.exit(1);
        }
        
        String name = args[0];
        String pass = args[1];
        AlcatrazClient client = new AlcatrazClient(name, pass);
        Thread console = new Thread(new ClientConsole(client));
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
        // TODO Auto-generated method stub
        
    }
    
    
}
