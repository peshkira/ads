package at.tuwien.ads11.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.utils.ServerConstants;
import spread.BasicMessageListener;
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
		
		switch(msg.getType()) {
		case ServerConstants.MSG_PLAYER_REGISTER:
			;
		case ServerConstants.MSG_PLAYER_UNREGISTER:
			;
		case ServerConstants.MSG_GAME_CREATE:
			;
		case ServerConstants.MSG_GAME_JOIN:
			;
		case ServerConstants.MSG_GAME_LEAVE:
			;
		case ServerConstants.MSG_GAME_START:
			;
		case ServerConstants.MSG_GAME_CANCEL:
			;
		default:
			;
		}
		
		
	}

}
