import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IClient extends Remote {

    /**
     * Called when the game is started. The Game object will contain infos about
     * the other players, how to contact them and the order of the players.
     * 
     * @param game
     *            the current game
     * @throws RemoteException
     */
    void startGame(Game game) throws RemoteException;

    /**
     * The method will be called when the player on turn moves. In this way the
     * state is propagated via the clients. The Movement object will contain a
     * player id, a prisoner id, integers for row and columns and possibly some
     * further info depending on the interfaces defined in the Alcatraz API.
     * 
     * @param m
     *            the movement object.
     * @throws RemoteException
     */
    void doMove(Movement m) throws RemoteException;

    /**
     * Each client will keep a copy of the history of the game. If anything goes
     * wrong it can always contact the previous player for synchronization
     * 
     * @return a list of movements.
     * @throws RemoteException
     */
    List<Movement> getHistory() throws RemoteException;
}
