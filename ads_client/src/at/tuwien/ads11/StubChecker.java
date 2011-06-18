package at.tuwien.ads11;

import java.util.Iterator;

public class StubChecker extends Thread {

    private AlcatrazClient client;

    private boolean run;

    public StubChecker(AlcatrazClient client) {
        this.client = client;
        setRun(true);
    }

    public void run() {
        while (isRun()) {

            Iterator<Integer> iter = this.client.getFailedPeers().iterator();
            while (iter.hasNext()) {
                int i = iter.next();
                try {
                    IClient stub = this.client.getStub(this.client.getClients().get(i));
                    if (stub != null) {
                        this.client.getCache().put(i, stub);
                        iter.remove();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public boolean isRun() {
        return run;
    }
}
