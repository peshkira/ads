package at.tuwien.ads11.listener;

import java.rmi.RemoteException;

import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import at.tuwien.ads11.AlcatrazClient;
import at.tuwien.ads11.IClient;
import at.tuwien.ads11.remote.Movement;

public class ClientMoveListener implements MoveListener {

    private AlcatrazClient client;

    public ClientMoveListener(AlcatrazClient client) {
        this.client = client;
    }

    @Override
    public void gameWon(Player player) {
        // TODO set all cleint flags to default
        System.out.println("We have a winner: " + player.getId());

    }

    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        Movement m = new Movement(player, prisoner, rowOrCol, row, col);
        client.getLocalHistory().add(m);

        for (Integer idx : client.getCache().keySet()) {
            IClient stub = client.getCache().get(idx);
            MovePropagator propagator = new MovePropagator(stub, m, idx);
            Thread t = new Thread(propagator);
            t.start();
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
                ClientMoveListener.this.client.getFailedPeers().add(idx);
            }
        }

    }

}
