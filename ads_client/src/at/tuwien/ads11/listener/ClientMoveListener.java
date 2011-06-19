package at.tuwien.ads11.listener;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import at.tuwien.ads11.AlcatrazClient;
import at.tuwien.ads11.IClient;
import at.tuwien.ads11.remote.Movement;

public class ClientMoveListener implements MoveListener {

	private static final Logger LOG = LoggerFactory.getLogger(ClientMoveListener.class);
	
    private AlcatrazClient client;
    
    private ExecutorService threadPool;

    public ClientMoveListener(AlcatrazClient client) {
        this.client = client;
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void gameWon(Player player) {
        System.out.println("We have a winner: " + player.getId());
        this.client.stopGame();
    }

    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        Movement m = new Movement(player, prisoner, rowOrCol, row, col);
        client.getLocalHistory().add(m);

        for (Integer idx : client.getCache().keySet()) {
            IClient stub = client.getCache().get(idx);
            if (stub == null)
                continue;
            //MovePropagator propagator = new MovePropagator(stub, m, idx);
            //Thread t = new Thread(propagator);
            //t.start();
            threadPool.execute(new MovePropagator(stub, m, idx));
        }
    }

//    private void refreshStub(int idx) {
//        try {
//            IClient stub = this.client.getStub(this.client.getClients().get(idx));
//            if (stub != null) {
//                this.client.getCache().put(idx, stub);
//            }
//        } catch (Exception e) {
//            System.out.println("refresh was unsuccessful!");
//        }
//    }

    public void shutdown() {
    	threadPool.shutdownNow();
    }

	private class MovePropagator implements Runnable {

        private IClient stub;

        private Movement m;

        private int idx;

        private MovePropagator(IClient stub, Movement m, int idx) {
            this.stub = stub;
            this.m = m;
            this.idx = idx;
        }

        @Override
        public void run() {
            try {
                this.stub.doMove(m);
            } catch (RemoteException e) {
                System.out.println("DO MOVE FAILED ON: " + idx);
                ClientMoveListener.this.client.getFailedPeers().add(idx);
            }
        }

    }

}
