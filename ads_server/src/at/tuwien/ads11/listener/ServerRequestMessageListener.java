package at.tuwien.ads11.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.BasicMessageListener;
import spread.SpreadException;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.utils.RMIServerInfo;
import at.tuwien.ads11.utils.ServerConstants;

public class ServerRequestMessageListener implements BasicMessageListener {

	private static final Logger LOG = LoggerFactory.getLogger(ServerRequestMessageListener.class);
	private ReplicatedServer server;
	
	public ServerRequestMessageListener(ReplicatedServer server) {
		this.server = server;
	}
	
	@Override
	public void messageReceived(SpreadMessage msg) {
		LOG.debug("Message of type {} received", msg.getType());

		switch(msg.getType()) {
		case ServerConstants.MSG_GET_SERVER_REFERENCE:
			this.server.sendProxyReference(msg.getSender());
			
			break;
		case ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE:
			try {
				RMIServerInfo s = (RMIServerInfo) msg.getObject();
	            this.server.receiveServerReference(s);

	        } catch (SpreadException e) {
	            e.printStackTrace();
	        }
	        break;
		case ServerConstants.MSG_GET_SERVER_STATE:
			break;
		case ServerConstants.MSG_GET_SERVER_STATE_RESPONSE:
		    break;
		default:
			break;
		}
	}
}
