package at.tuwien.ads11;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.common.Constants;
import at.tuwien.ads11.listener.ClientRequestMessageListener;
import at.tuwien.ads11.listener.MembershipMessageListener;
import at.tuwien.ads11.listener.ServerRequestMessageListener;
import at.tuwien.ads11.proxy.ProxyFactory;
import at.tuwien.ads11.remote.ClientMock;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.RMIServerInfo;
import at.tuwien.ads11.utils.RequestUUID;
import at.tuwien.ads11.utils.ResultPoller;
import at.tuwien.ads11.utils.ServerConstants;
import at.tuwien.ads11.utils.ServerMessageFactory;

public class ReplicatedServer implements IServer {

    private static final long serialVersionUID = -8917839808656077153L;

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedServer.class);

    private ServerState state;

    private RMIServerInfo rmi;

    private int daemonPort;
    private String daemonIP;
    private String serverId;
    private boolean adminsProxy;

    private Registry registry;
    private SpreadConnection spreadCon;
    private SpreadGroup serverGroup;
    private SpreadGroup ownGroup;
    private IServer proxy;
    private Map<RequestUUID, Object> requests;

    private IServer stub;
    private List<SpreadGroup> groupMembers;
    private int lastGroupMemberIndex;

    private List<SpreadMessage> msgBuffer;
    private AtomicBoolean upToDate;
    private AtomicBoolean bufferMsgs;

    public ReplicatedServer(Properties props) {
        this.serverId = props.getProperty("server.id");

        String host = props.getProperty("server.rmi.host");
        int port = Integer.parseInt(props.getProperty("server.rmi.port"));
        this.rmi = new RMIServerInfo(host, port);

        this.adminsProxy = Boolean.parseBoolean(props.getProperty("server.proxy.administer"));

        this.daemonPort = Integer.parseInt(props.getProperty("spread.daemon.port"));
        this.daemonIP = props.getProperty("spraed.daemon.ip");

        this.state = new ServerState();
        this.requests = new HashMap<RequestUUID, Object>();
        upToDate = new AtomicBoolean(false);
        bufferMsgs = new AtomicBoolean(false);
        msgBuffer = Collections.synchronizedList(new LinkedList<SpreadMessage>());
    }

    public static void main(String args[]) {

        if (args == null || args.length != 1) {
            System.out.println("Invalid argument count - provide name of the config file.");
            System.exit(1);
        }

        Properties props = new Properties();
        try {
            props.load(new FileInputStream(args[0]));
        } catch (FileNotFoundException e) {
            LOG.error("Config file: {} not found.", args[0]);
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            LOG.error("Error in processing the config file.");
            System.exit(1);
        }

        ReplicatedServer server = new ReplicatedServer(props);
        Thread console = new Thread(new ServerConsole(server));

        server.start();
        console.start();

    }

    /**
     * Wraps the info into a client mock object and adds it to the set. If the
     * client is already register it doesn't matter as it won't be added again
     * the the set. ClientMock equals will return true...
     * 
     * If however the pass is different, than the client will be considered as a
     * new one.
     */
    @Override
    public synchronized boolean register(ClientMock client) throws RemoteException {
        LOG.debug("incoming registration call");
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());
        try {

            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_PLAYER_REGISTER);
            message.digest(client);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public boolean registerClient(ClientMock mock) {
        LOG.info("registering client [{}]", mock.getName());
        return this.state.addClient(mock);
    }

    /**
     * Works the same way as the register. If the Client is not registered, than
     * the method will just return false.
     */
    @Override
    public synchronized boolean unregister(String name, String pass) throws RemoteException {
        LOG.debug("incoming unregistration call");
        ClientMock client = new ClientMock(name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());
        try {

            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_PLAYER_UNREGISTER);
            message.digest(client);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public Boolean unregister(ClientMock client) {
        return this.state.removeClient(client);
    }

    /**
     * Returns the current list of games that are not started yet.
     */
    @Override
    public synchronized List<Game> fetchGames() throws RemoteException {
        return this.anonymizeGames(); // this is read, so no need for sync
    }

    @Override
    public synchronized boolean createGame(String game, String name, String pass) throws RemoteException {
        LOG.debug("incoming create game call");

        Game g = new Game(game, name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());

        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_GAME_CREATE);
            message.digest(g);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public boolean createGame(Game g) {
        ClientMock c = new ClientMock(g.getHost(), g.getPass());
        if (this.state.getClients().contains(c) && c.getPass().equals(g.getPass())) {
            // Check if client is hosting any other game
        	for(Game tmp : this.state.getGames())
        		if (tmp.getHost().equals(c.getName()))
        			return false;
        	
        	// Check if client is hosting any other running game
        	for(Game tmp : this.state.getPlaying())
        		if (tmp.getHost().equals(c.getName()))
        			return false;
        	
        	for (ClientMock tmp : this.state.getClients()) {
                if (tmp.equals(c)) {
                    g.getPlayers().add(tmp);
                    return this.state.addGame(g);
                }
            }
        }

        // if client not registered
        // or throw remote exception?
        return false;
    }

    @Override
    public synchronized boolean cancelGame(String game, String name, String pass) throws RemoteException {
        LOG.debug("incoming cancel game call");

        Game g = new Game(game, name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());

        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_GAME_CANCEL);
            message.digest(g);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public boolean cancelGame(Game g) {
        ClientMock c = new ClientMock(g.getHost(), g.getPass());
        if (this.state.getClients().contains(c) && this.state.getPlaying().contains(g)) {
            return this.state.removeRunningGame(g);
        }

        return false;
    }

    @Override
    public synchronized Game startGame(String game, String name, String pass) throws RemoteException {
        if(game.length() < 1) {
        	LOG.debug("Rejoining running game");
        	return rejoinRunningGame(name, pass);
        }
        
    	LOG.debug("incoming start game call");
        Game g = new Game(game, name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());

        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_GAME_START);
            message.digest(g);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Game result = (Game) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        //can be null
        if(result.getName().equals(ServerConstants.GAME_NONEXISTING))
        	return null;
        return result;

    }

    private Game rejoinRunningGame(String name, String pass) {
		ClientMock client = new ClientMock(name, pass);
    	for(Game game : this.state.getPlaying())
			if(game.getPlayers().contains(client))
				return game;
		return null;
	}

	public Game startGame(Game g) {
        ClientMock c = new ClientMock(g.getHost(), g.getPass());
        Game start = null;
        if (this.state.getClients().contains(c) && this.state.getGames().contains(g)) {
            for (Game tmp : this.state.getGames()) {
                if (tmp.equals(g) && tmp.getPlayers().size() > 1) {
                    start = tmp;
                    break;
                }
            }

            this.state.removeGame(start);
            this.state.addRunningGame(start);
            return this.anonymizeGame(start);
        } else if (this.state.getPlaying().contains(g)) {
        	return new Game(ServerConstants.GAME_ALREADY_RUNNING, "", "");
        }
        start = new Game(ServerConstants.GAME_NONEXISTING, "", "");
        return start;
    }

    @Override
    public synchronized boolean joinGame(String game, String name, String pass) throws RemoteException {
        LOG.debug("incoming join game call");

        Game g = new Game(game, name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());

        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_GAME_JOIN);
            message.digest(g);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public boolean joinGame(Game g) {
        ClientMock c = new ClientMock(g.getHost(), g.getPass());
        ClientMock tmp = null;
        for (ClientMock m : this.state.getClients()) {
            if (m.equals(c)) {
                tmp = m;
                break;
            }
        }
        
        if (tmp == null) {
            return false; // or throw exception
        }
        
        for (Game game : this.state.getGames()) {
            ClientMock client = game.containsPlayerName(tmp.getName());
            if (game.getName().equals(g.getName()) && client == null && game.getPlayers().size() < 4) {
                if(game.getPlayers().size() == 3 && game.containsPlayerName(game.getHost()) == null)
                	return false;
            	return game.getPlayers().add(tmp);
            }
        }


        return false;
    }

    @Override
    public synchronized boolean leaveGame(String game, String name, String pass) throws RemoteException {
        LOG.debug("incoming leave game call");

        Game g = new Game(game, name, pass);
        RequestUUID uuid = new RequestUUID(this.getServerId(), new Date().getTime());

        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.setType(ServerConstants.MSG_GAME_LEAVE);
            message.digest(g);
            message.digest(uuid);
            message.addGroup(serverGroup);
            this.spreadCon.multicast(message);

        } catch (SpreadException e) {
            throw new RemoteException("An error occured, try with other server");
        }

        ResultPoller poller = new ResultPoller(this, 10);
        Boolean result = (Boolean) poller.poll(uuid);
        this.requests.remove(uuid); // not needed anymore

        if (result != null) {
            return result;
        } else {
            throw new RemoteException("No response");
        }
    }

    public Boolean leaveGame(Game g) {
        ClientMock c = new ClientMock(g.getHost(), g.getPass());

        if (this.state.getClients().contains(c)) {
            Iterator<Game> it = this.state.getGames().iterator();
        	while(it.hasNext())
        	{
        		Game tmp = it.next();
        		if (tmp.getName().equals(g.getName())) {
                	if(tmp.getPlayers().size() == 1) {
                		LOG.debug("Game {} has no players left and is removed.", g.getName());
                		it.remove();
                	}
                    LOG.debug("Player {} removed from game {}", c.getName(), g.getName());
                	return tmp.getPlayers().remove(c);
        		}
        	}
        }

        return false;
    }

    public Map<RequestUUID, Object> getRequests() {
        return this.requests;
    }

    protected void shutdown() {
        try {

            serverGroup.leave();

            if (adminsProxy) {
                UnicastRemoteObject.unexportObject(this.proxy, true);
            }
            UnicastRemoteObject.unexportObject(this, true);
            UnicastRemoteObject.unexportObject(this.registry, true);

        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        } catch (SpreadException e) {
            LOG.error("Error while leaving the server group.");
            e.printStackTrace();
        }

        // consider to kill the process here.
    }

    public void sendProxyReference(SpreadGroup group) {
        try {
            SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
            message.addGroup(group);
            message.setType(ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE);
            message.setObject(this.rmi);
            spreadCon.multicast(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    public void receiveServerReference(RMIServerInfo info) {
        this.rebindProxy(info);
    }

    public void askForServerReference(SpreadGroup joined) {
        if (this.adminsProxy) {
            LOG.debug("Asking for Server References to refresh proxy");

            try {
                SpreadMessage message = ServerMessageFactory.getInstance().getDefaultMessage();
                message.addGroup(joined);
                message.setType(ServerConstants.MSG_GET_SERVER_REFERENCE);
                spreadCon.multicast(message);
            } catch (SpreadException e) {
                e.printStackTrace();
                // what to do here...
            }
        }
    }

    // ========= private ===========

    private void start() {
        getRMIRegistry();
        connectToSpread();
    }

    private void connectToSpread() {
        spreadCon = new SpreadConnection();
        spreadCon.add(new MembershipMessageListener(this));
        spreadCon.add(new ServerRequestMessageListener(this));
        spreadCon.add(new ClientRequestMessageListener(this));
        serverGroup = new SpreadGroup();
        ownGroup = new SpreadGroup();

        try {
            spreadCon.connect(InetAddress.getByName(daemonIP), daemonPort, getServerId(), false, true);
            serverGroup.join(spreadCon, ServerConstants.SPREAD_SERVER_GROUP);
            ownGroup.join(spreadCon, serverId);
        } catch (UnknownHostException e) {
            LOG.error("Can not find daemon: {}", daemonIP);
            System.err.println();
            e.printStackTrace();
            System.exit(1);
        } catch (SpreadException e) {
            LOG.error("Error while connecting to Spread.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void getRMIRegistry() {
        try {
            this.registry = LocateRegistry.createRegistry(this.rmi.getPort());
            this.stub = (IServer) UnicastRemoteObject.exportObject(this, 0);
            this.registry.rebind(ServerConstants.SERVER_OBJECT, this.stub);

        } catch (RemoteException e) {
            e.printStackTrace();
            LOG.error("System will exit now");
            System.exit(1);
        }
    }

    private void rebindProxy(RMIServerInfo... servers) {
        boolean rebind = false;
        for (RMIServerInfo server : servers) {
            if (ProxyFactory.getInstance().addServer(server)) {
                rebind = true;
            }
        }

        try {
            if (rebind) {
                LOG.debug("Rebinding proxy...");

                if (this.proxy != null)
                    UnicastRemoteObject.unexportObject(this.proxy, true);

                this.proxy = ProxyFactory.getInstance().createServerProxy();
                IServer stub = (IServer) UnicastRemoteObject.exportObject(proxy, 0);
                this.registry.rebind(Constants.REMOTE_SERVER_OBJECT_NAME, stub);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private List<Game> anonymizeGames() {
        List<Game> anonymize = new ArrayList<Game>();

        for (Game g : this.state.getGames()) {
            Game tmp = new Game(g.getName(), g.getHost(), "");
            for (ClientMock c : g.getPlayers()) {
                tmp.getPlayers().add(new ClientMock(c.getName(), ""));
            }
            anonymize.add(tmp);
        }

        return anonymize;
    }

    public void rejoinServerGroup() throws SpreadException {
        serverGroup.leave();
        serverGroup.join(spreadCon, ServerConstants.SPREAD_SERVER_GROUP);
    }

    public void sendMsg(SpreadMessage msg) throws SpreadException {
        this.spreadCon.multicast(msg);
    }

    private Game anonymizeGame(Game tmp) {
        Game anonym = new Game(tmp.getName(), tmp.getHost(), "");
        for (ClientMock c : tmp.getPlayers()) {
            ClientMock tmpC = new ClientMock(c.getName(), "");
            tmpC.setHost(c.getHost());
            tmpC.setPort(c.getPort());
            anonym.getPlayers().add(tmpC);
        }

        return anonym;
    }

    public String getServerId() {
        return serverId;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
        if (!msgBuffer.isEmpty()) {
            ClientRequestMessageListener processor = new ClientRequestMessageListener(this);
            for (SpreadMessage msg : msgBuffer)
                processor.processMsg(msg);
            msgBuffer.clear();
        }
        upToDate.set(true);
    }

    public List<SpreadGroup> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(SpreadGroup[] groupMembers) {
        this.groupMembers = new LinkedList<SpreadGroup>();
        for (SpreadGroup group : groupMembers) {
            LOG.info("GROUP: {}, OWNGROUP: {}", group.toString(), ownGroup.toString());
            if (group.toString().startsWith(ownGroup.toString(), 1)) {
                LOG.debug("FILTERED {}", group.toString());
                continue;
            } else
                this.groupMembers.add(group);
        }
    }

    public int getLastGroupMemberIndex() {
        return lastGroupMemberIndex;
    }

    public void setLastGroupMemberIndex(int lastGroupMemberIndex) {
        this.lastGroupMemberIndex = lastGroupMemberIndex;
    }

    @Override
    public String toString() {
        return this.serverId;
    }

    public SpreadGroup getOwnGroup() {
        return this.ownGroup;
    }

    public SpreadGroup getServerGroup() {
        return serverGroup;
    }

    public List<SpreadMessage> getMsgBuffer() {
        return msgBuffer;
    }

    public void setMsgBuffer(List<SpreadMessage> msgBuffer) {
        this.msgBuffer = msgBuffer;
    }

    public AtomicBoolean getUpToDate() {
        return upToDate;
    }

    public AtomicBoolean getBufferMsgs() {
        return bufferMsgs;
    }

}
