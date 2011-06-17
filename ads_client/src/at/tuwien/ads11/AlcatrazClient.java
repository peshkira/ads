package at.tuwien.ads11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.falb.games.alcatraz.api.Alcatraz;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.Movement;

public class AlcatrazClient implements IClient {
    
    private static final Logger LOG = LoggerFactory.getLogger(AlcatrazClient.class);

	private Alcatraz alcatraz;
    
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
