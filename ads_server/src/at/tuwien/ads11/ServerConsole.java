package at.tuwien.ads11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerConsole implements Runnable {

    private BufferedReader reader;

    private ReplicatedServer server;

    private boolean run;

    public ServerConsole(ReplicatedServer server) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.server = server;
        this.run = true;
    }

    @Override
    public void run() {
        try {
            System.out.println("Welcome to the ALCATRAZ Replicated Server");

            String line = "";

            while (this.run) {
                this.prompt("");
                line = reader.readLine();
                this.executeCommand(line);
            }

            this.server.shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void executeCommand(String line) {

        if (line.equals("exit")) {
            this.run = false;
        } else if (line.equals("help")) {
            System.out.println("you are on your man.");
        } else {
            this.prompt("Unknown command: " + line+"\n");
        }
        

    }

    private void prompt(String msg) {
        System.out.print("server>" + msg);
    }

}
