package at.tuwien.ads11;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IServer extends Remote {

    /**
     * Registers to the server as a client.
     * 
     * @param name
     *            the id of the client
     * @param pass
     *            the pass for unregistering
     * @return true if the registration process was successful, false otherwise.
     */
    boolean register(String name, String pass) throws RemoteException;

    /**
     * Unregisters as a client from the server.
     * 
     * @param name
     *            the name of the client.
     * @param pass
     *            the pass that was passed upon registering (otherwise other
     *            could unregister you, if they know your name)
     * @return true if the unregistering process was successful, false
     *         otherwise.
     */
    boolean unregister(String name, String pass) throws RemoteException;

    /**
     * Fetches all created games that have not started yet.
     * 
     * @return a list of all games that have not started yet, or an empty list.
     * @throws RemoteException
     */
    List<Game> fetchGames() throws RemoteException;

    /**
     * Creates a new game with the given id and the given host
     * 
     * @param name
     *            the name (id) of the client
     * @param game
     *            the name (id) of the game
     * @param pass
     *            the pass to be used for cancelation of the game.
     * @return true if the name was successfully created, false otherwise.
     * @throws RemoteException
     */
    boolean createGame(String game, String name, String pass) throws RemoteException;

    /**
     * Cancels the game if it exists and the pass is correct
     * 
     * @param name
     *            the name of the host
     * @param game
     *            the game (id) name
     * @param pass
     *            the pass for cancelation
     * @return true if successful, false otherwise
     * @throws RemoteException
     */
    boolean cancelGame(String game, String name, String pass) throws RemoteException;

    /**
     * Should be called when a client host starts a game.
     * 
     * @param game
     *            the id of the game.
     * @param name
     *            id of the host
     * @param pass
     *            the pass for starting the game (given at game creation) return
     *            Game returns the game object to the client. It will contain
     *            all needed information to start the game of their own.
     * @throws RemoteException
     */
    Game startGame(String game, String name, String pass) throws RemoteException;

    /**
     * Adds the client to a game that has not started yet.
     * 
     * @param name
     *            the name (id) of the client
     * @param game
     *            the id of the game
     * @param pass
     *            the pass for leaving the game.
     * @return true if the operation succeeds, false otherwise.
     * @throws RemoteException
     */
    boolean joinGame(String game, String name, String pass) throws RemoteException;

    /**
     * Leaves the game if it has not started yet.
     * 
     * @param game
     *            the id of the game.
     * @param name
     *            the id of the client
     * @param pass
     *            the pass of the client
     * @return
     * @throws RemoteException
     */
    boolean leaveGame(String game, String name, String pass) throws RemoteException;
}
