package at.tuwien.ads11.listener;

import spread.AdvancedMessageListener;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.ServerConstants;

public class MembershipMessageListener implements AdvancedMessageListener {

    private ReplicatedServer server;

    public MembershipMessageListener(ReplicatedServer server) {
        this.server = server;
    }

    @Override
    public void membershipMessageReceived(SpreadMessage msg) {
        SpreadGroup joined = msg.getMembershipInfo().getJoined();
        SpreadGroup left = msg.getMembershipInfo().getLeft();

        if (joined != null && !msg.isSelfDiscard()) {
            this.joinMessage(joined, msg);
        }

        if (left != null && !msg.isSelfDiscard()) {
            this.leaveMessage(left);
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage msg) {
        // do nothing for now
        // consider to use different listeners for different types of messages
        
        System.out.println("Message received: " + msg.getType());
        
        if (msg.getType() == ServerConstants.MSG_GET_SERVER_REFERENCE) {
            this.server.sendProxyReference(msg.getSender());
            
        } else if (msg.getType() == ServerConstants.MSG_GET_SERVER_REFERENCE_RESPONSE) {
            try {
                IServer s = (IServer) msg.getObject();
                this.server.receiveServerReference(s);

            } catch (SpreadException e) {
                e.printStackTrace();
            }
        }
    }

    private void joinMessage(SpreadGroup joined, SpreadMessage msg) {
        System.out.println(joined.toString() + " has joined the group");
        // TODO synchornize the new guy...
        
        this.server.askForServerReference();
    }

    private void leaveMessage(SpreadGroup left) {
        System.out.println(left.toString() + " has left the group");
    }
}
