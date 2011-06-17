package at.tuwien.ads11.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.BasicMessageListener;
import spread.SpreadException;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.ServerState;
import at.tuwien.ads11.utils.RMIServerInfo;
import at.tuwien.ads11.utils.ServerConstants;
import at.tuwien.ads11.utils.ServerMessageFactory;

public class ServerRequestMessageListener implements BasicMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ServerRequestMessageListener.class);
    private ReplicatedServer server;

    public ServerRequestMessageListener(ReplicatedServer server) {
        this.server = server;
    }

    @Override
    public void messageReceived(SpreadMessage msg) {
        LOG.debug("Message of type {} received", msg.getType());
        switch (msg.getType()) {
        case ServerConstants.MSG_GET_SERVER_REFERENCE:
            processGetServerReferenceRequest(msg);
            break;
        case ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE:
            processGetServerReferenceResponse(msg);
            break;
        case ServerConstants.MSG_GET_SERVER_STATE:
            processServerStateRequest(msg);
            break;
        case ServerConstants.MSG_GET_SERVER_STATE_RESPONSE:
            processServerStateReponse(msg);
            break;
        default:
            break;
        }
    }

    private void processGetServerReferenceResponse(SpreadMessage msg) {
        try {
            RMIServerInfo s = (RMIServerInfo) msg.getObject();
            this.server.receiveServerReference(s);

        } catch (SpreadException e) {
            e.printStackTrace();
        }
        
    }

    private void processGetServerReferenceRequest(SpreadMessage msg) {
        this.server.sendProxyReference(msg.getSender());        
    }

    private void processServerStateRequest(SpreadMessage msg) {
        // Don't process own get state requests
    	if (msg.getSender().toString().startsWith(server.getOwnGroup().toString(), 1)) {
            server.getBufferMsgs().set(true);
        } else
            try {
                server.sendMsg(ServerMessageFactory.getInstance().createSafeMessage(
                        ServerConstants.MSG_GET_SERVER_STATE_RESPONSE, server.getState(), msg.getSender()));
            } catch (SpreadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
	
	private void processServerStateReponse(SpreadMessage msg) {
		LOG.debug("State received from {}", msg.getSender());
		try {
			server.setState((ServerState)msg.getObject());
		} catch (SpreadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
