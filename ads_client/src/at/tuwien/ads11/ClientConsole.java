package at.tuwien.ads11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConsole implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClientConsole.class);
	
	private BufferedReader reader;

    private AlcatrazClient client;
    
    private boolean run;

    public ClientConsole(AlcatrazClient client) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
        this.run = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("Welcome to ALCATRAZ... There is no escape");

            String line = "";

            while (this.run) {
                this.prompt("");
                line = reader.readLine();
                this.executeCommand(line);
            }

            this.client.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // TODO: better command execution
    private void executeCommand(String line) {
    	try {
	        if (line.equals("exit")) {
	            this.run = false;
	        } else if (line.equals("help")) {
	            System.out.println("Use one of the following:");
	            System.out.println("help \t\t\t- print this message");
	            System.out.println("register \t\t- register with the server");
	            System.out.println("unregister \t\t- unregister with the server");
	            System.out.println("games \t\t\t- fetch the open games");
	            System.out.println("create [game name] \t- create a new game");
	            //System.out.println("cancel [game name] \t- cancel the game");
	            System.out.println("join [game name] \t- join a new game");
	            System.out.println("leave [game name] \t- leave the game");
	            System.out.println("start \t\t\t- rejoin running game");
	            System.out.println("start [game name] \t- start a game that you host");
	            System.out.println("exit \t\t\t- exit the application");
	        } else if (line.equals("register")) {
	            client.register();
	        } else if (line.equals("unregister")) {
	            client.unregister();
	            
	        } else if (line.equals("games")) {
	            client.fetchGames();
	            
	        } else if (line.startsWith("create ")) {
	        	client.createGame(line.replaceFirst("create ", ""));
	        //} else if (line.startsWith("cancel ")) {
	        //    client.startGame(line.replaceFirst("cancel ", ""));
	            
	        } else if (line.startsWith("join ")) {
	            client.joinGame(line.replaceFirst("join ", ""));
	            
	        } else if (line.startsWith("leave ")) {
	            client.leaveGame(line.replaceFirst("leave ", ""));
	            
	        } else if (line.startsWith("start")) {
	            client.startGame(line.replaceFirst("start", "").trim());
	            
	        } else if (line.equals("")) {
	            //do nothing...
	        } else {
	            System.out.println("Unknown command: " + line);
	        }
    	} catch (RemoteException e) {
    		LOG.error("An error occurred. No Repsonse from server. Cause:{}", e.getMessage());
            System.out.println("An error occurred. The server was unreachable. Please try again later.");
    	}
    }

    private void prompt(String msg) {
        System.out.print("client>" + msg);
    }
}
