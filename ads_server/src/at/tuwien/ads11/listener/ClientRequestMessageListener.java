package at.tuwien.ads11.listener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.utils.RequestUUID;
import at.tuwien.ads11.utils.ServerConstants;
import spread.BasicMessageListener;
import spread.SpreadException;
import spread.SpreadMessage;

public class ClientRequestMessageListener implements BasicMessageListener {

	private static final Logger LOG = LoggerFactory.getLogger(ClientRequestMessageListener.class);
	
	private ReplicatedServer server;
	
	public ClientRequestMessageListener(ReplicatedServer server)
	{
		this.server = server;
	}
	
	@Override
	public void messageReceived(SpreadMessage msg) {
		LOG.debug("Message of type {} received", msg.getType());
		if(server.getBufferMsgs().get())
			server.getMsgBuffer().add(msg);
		processMsg(msg);
	}
	
	public void processMsg(SpreadMessage msg) {
		switch(msg.getType()) {
		case ServerConstants.MSG_PLAYER_REGISTER:
			try {
                Vector digest = msg.getDigest();
                ClientMock client = (ClientMock) digest.get(0);
                RequestUUID uuid = (RequestUUID) digest.get(1);
                Boolean registered = server.register(client);
                
                if (server.getServerId().equals(uuid.getServer())) {
                    server.getRequests().put(uuid, registered);
                }
                
            } catch (SpreadException e) {
                e.printStackTrace();
            }
            
            break;
		case ServerConstants.MSG_PLAYER_UNREGISTER:
			break;
		case ServerConstants.MSG_GAME_CREATE:
			break;
		case ServerConstants.MSG_GAME_JOIN:
			break;
		case ServerConstants.MSG_GAME_LEAVE:
			break;
		case ServerConstants.MSG_GAME_START:
			break;
		case ServerConstants.MSG_GAME_CANCEL:
			break;
		default:
			break;
		}
	}

}
