package at.tuwien.ads11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerConsole implements Runnable {

    private BufferedReader reader;
    
    private ReplicatedServer server;
    
    public ServerConsole(ReplicatedServer server) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.server = new ReplicatedServer();
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Please type in your command");
            String line = reader.readLine();
            
            while (!line.equals("exit")) {
                line = reader.readLine();
                this.executeCommand(line);
            }
            
            this.server.shutdown();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private void executeCommand(String line) {
        System.out.println("Unknown command: " + line);
        
    }

    
}
