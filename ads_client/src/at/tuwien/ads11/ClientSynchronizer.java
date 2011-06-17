package at.tuwien.ads11;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.remote.Movement;

public class ClientSynchronizer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSynchronizer.class);

    private boolean run;

    private int wait;

    private int prevIndex;

    private AlcatrazClient client;

    public ClientSynchronizer(AlcatrazClient client, int time, int prevIndex) {
        this.setWait(time);
        this.setRun(true);
        this.client = client;
        this.prevIndex = prevIndex;
    }

    @Override
    public void run() {
        while (isRun()) {
            try {

                List<Movement> remote = client.getClientStubCache().get(this.prevIndex).getHistory();
                List<Movement> local = client.getLocalHistory();

                List<Movement> delta = this.getDelta(local, remote);
                this.applyDelta(delta);

                Thread.sleep(this.wait);

            } catch (RemoteException e) {
                LOG.warn("An error occurred while trying to synchronize: {}", e.getMessage());

            } catch (InterruptedException e) {
            }
        }
    }

    private void applyDelta(List<Movement> delta) {
        for (Movement m : delta) {
            try {
                this.client.doMove(m); //local call
                LOG.info("applying movement: {}", m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private List<Movement> getDelta(List<Movement> local, List<Movement> remote) {
        List<Movement> delta = new ArrayList<Movement>();

        if (local.size() < remote.size()) {
            LOG.info("client is not up to date, synchronizing...");
            int diff = remote.size() - local.size();
            diff--; // fix index
            while (diff >= 0) {
                Movement m = remote.get(diff);
                delta.add(0, m);
                diff--;
            }
        }

        return delta;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public boolean isRun() {
        return run;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getWait() {
        return wait;
    }
}
