package at.tuwien.ads11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientConsole implements Runnable {
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

    private void executeCommand(String line) {

        if (line.equals("exit")) {
            this.run = false;
        } else if (line.equals("help")) {
            System.out.println("Use one of the following:");
            System.out.println("help \t\t- print this message");
            System.out.println("register \t\t- register with the server");
            System.out.println("unregister \t\t- unregister with the server");
            System.out.println("games \t\t- fetch the open games");
            System.out.println("create [game name] \t\t- create a new game");
            System.out.println("cancel [game name] \t\t- cancel the game");
            System.out.println("join [game name] \t\t- join a new game");
            System.out.println("leave [game name] \t\t- leave the game");
            System.out.println("start [game name] \t\t- start a game that you host");
            System.out.println("exit \t\t- exit the application");
        } else if (line.equals("register")) {
            //TODO register
            
        } else if (line.equals("unregister")) {
            //TODO unregister
            
        } else if (line.equals("games")) {
            //TODO fetch games from server
            
        } else if (line.startsWith("create")) {
            //TODO parse rest of line and create game;
            
        } else if (line.startsWith("cancel")) {
            //TODO cancel game
            
        } else if (line.startsWith("join")) {
            //TODO join game
            
        } else if (line.startsWith("leave")) {
            //TODO leave game
            
        } else if (line.startsWith("start")) {
            // TODO start game
            
        } else if (line.equals("")) {
            //do nothing...
        } else {
            System.out.println("Unknown command: " + line);
        }
        

    }

    private void prompt(String msg) {
        System.out.print("client>" + msg);
    }
}
