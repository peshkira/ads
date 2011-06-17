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

    }

    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        Movement m = new Movement(player, prisoner, rowOrCol, row, col);
        client.getLocalHistory().add(m);
        
        for (IClient c : client.getClientStubCache()) {
            try {
                c.doMove(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        

    }

}
