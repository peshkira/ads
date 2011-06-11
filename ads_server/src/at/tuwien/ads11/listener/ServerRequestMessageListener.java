package at.tuwien.ads11.listener;

import javax.print.attribute.standard.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.ServerConstants;
import spread.BasicMessageListener;
import spread.SpreadException;
import spread.SpreadMessage;

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
		case ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE:
			try {
				IServer s = (IServer) msg.getObject();
	            this.server.receiveServerReference(s);

	        } catch (SpreadException e) {
	            e.printStackTrace();
	        }
		case ServerConstants.MSG_GET_SERVER_STATE:
			;
		case ServerConstants.MSG_GET_SERVER_STATE_RESPONSE:
			;
		default:
			;
		}
	}
}
