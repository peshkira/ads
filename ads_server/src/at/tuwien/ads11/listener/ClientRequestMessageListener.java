package at.tuwien.ads11.listener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.BasicMessageListener;
import spread.SpreadException;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.remote.Game;
import at.tuwien.ads11.utils.RequestUUID;
import at.tuwien.ads11.utils.ServerConstants;

public class ClientRequestMessageListener implements BasicMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRequestMessageListener.class);

    private ReplicatedServer server;

    public ClientRequestMessageListener(ReplicatedServer server) {
        this.server = server;
    }

    @Override
    public void messageReceived(SpreadMessage msg) {
        LOG.debug("Message of type {} received", msg.getType());

        switch (msg.getType()) {
        case ServerConstants.MSG_PLAYER_REGISTER:
            this.handleRegCall(msg, true);
            break;
        case ServerConstants.MSG_PLAYER_UNREGISTER:
            this.handleRegCall(msg, false);
            break;
        case ServerConstants.MSG_GAME_CREATE:
            this.handleGameCall(msg, true);
            break;
        case ServerConstants.MSG_GAME_JOIN:
            this.handleGameJoinCall(msg, true);
            break;
        case ServerConstants.MSG_GAME_LEAVE:
            this.handleGameJoinCall(msg, false);
            break;
        case ServerConstants.MSG_GAME_START:
            this.handleStartGame(msg);
            break;
        case ServerConstants.MSG_GAME_CANCEL:
            this.handleGameCall(msg, false);
            break;
        default:
            break;
        }

    }

    private void handleGameJoinCall(SpreadMessage msg, boolean join) {
        try {
            Vector digest = msg.getDigest();
            Game g = (Game) digest.get(0);
            RequestUUID uuid = (RequestUUID) digest.get(1);
            
            Boolean response = null;
            if (join) {
                response = server.joinGame(g);
            } else {
                response = server.leaveGame(g);
            }
            
            if (server.getServerId().equals(uuid.getServer())) {
                server.getRequests().put(uuid, response);
            }
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private void handleStartGame(SpreadMessage msg) {
        try {
            Vector digest = msg.getDigest();
            Game g = (Game) digest.get(0);
            RequestUUID uuid = (RequestUUID) digest.get(1);
            Game response = server.startGame(g);
            
            if (server.getServerId().equals(uuid.getServer())) {
                server.getRequests().put(uuid, response);
            }
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private void handleGameCall(SpreadMessage msg, boolean create) {
        try {
            Vector digest = msg.getDigest();
            Game g = (Game) digest.get(0);
            RequestUUID uuid = (RequestUUID) digest.get(1);
            Boolean response = null;
            
            if (create) {
                response = server.createGame(g);
            } else {
                response = server.cancelGame(g);
            }
            
            if (server.getServerId().equals(uuid.getServer())) {
                server.getRequests().put(uuid, response);
            }
            
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private void handleRegCall(SpreadMessage msg, boolean reg) {
        try {
            Vector digest = msg.getDigest();
            ClientMock client = (ClientMock) digest.get(0);
            RequestUUID uuid = (RequestUUID) digest.get(1);
            Boolean response = null;
            if (reg) {
                response = server.register(client);
            } else {
                response = server.unregister(client);
            }

            if (server.getServerId().equals(uuid.getServer())) {
                server.getRequests().put(uuid, response);
            }
        } catch (SpreadException e) {
            e.printStackTrace();
            // nothing to here
            // server won't get response and will throw remote exception
            // proxy will try another one....
        }

    }

}
