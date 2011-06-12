package at.tuwien.ads11;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.remote.Game;

public class ServerState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6795130759499699008L;
	
	private List<Game> games;
    private List<Game> runningGames;
    private Set<ClientMock> clients;
    
    public ServerState() {
    	this.games = Collections.synchronizedList(new LinkedList<Game>());
    	this.runningGames = Collections.synchronizedList(new LinkedList<Game>());
        this.clients = Collections.synchronizedSet(new HashSet<ClientMock>());
    }

	public List<Game> getGames() {
		return games;
	}

	public List<Game> getPlaying() {
		return runningGames;
	}

	public Set<ClientMock> getClients() {
		return clients;
	}
    
    public boolean addClient(ClientMock client) {
    	return this.clients.add(client);
    }
    
    public boolean removeClient(ClientMock client) {
    	return this.clients.remove(client);
    }
    
    public boolean addGame(Game game) {
    	return this.games.add(game);
    }
    
    public boolean removeGame(Game game) {
    	return this.games.remove(game);
    }
    
    public boolean addRunningGame(Game game) {
    	return this.runningGames.add(game);
    }
    
    public boolean removeRunningGame(Game game) {
    	return this.runningGames.remove(game);
    }
}
