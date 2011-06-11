package at.tuwien.ads11.utils;

import at.tuwien.ads11.ReplicatedServer;

public class ResultPoller {

    private ReplicatedServer server;

    private int iterations;

    public ResultPoller(ReplicatedServer server) {
        this.server = server;
        this.iterations = -1;
    }

    public ResultPoller(ReplicatedServer server, int iterations) {
        this.server = server;
        this.iterations = iterations;
    }

    public Object poll(RequestUUID uuid) {
        if (this.iterations == -1) {
            while (true) {
                Object result = this.get(uuid);

                if (result != null) {
                    return result;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) { }
                }
            }
        } else {
            int i = this.iterations;
            while (i > 0) {
                Object result = this.get(uuid);
                
                if (result != null) {
                    return result;
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) { }
                }
            }
        }
        return null;
    }

    private Object get(RequestUUID uuid) {
        return server.getRequests().get(uuid);
    }
}
