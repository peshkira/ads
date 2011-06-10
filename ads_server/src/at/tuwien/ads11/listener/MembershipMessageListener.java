package at.tuwien.ads11.listener;

import spread.AdvancedMessageListener;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;

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
        //do nothing for now
        //consider to use different listeners for different types of messages
        try {
            ReplicatedServer s = (ReplicatedServer) msg.getObject();
            System.out.println(s.getServerId());
         
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    private void joinMessage(SpreadGroup joined, SpreadMessage msg) {
        System.out.println(joined.toString() + " has joined the group");
        //TODO synchornize the new guy...
        //update proxy somehow...
    }
    
    private void leaveMessage(SpreadGroup left) {
        System.out.println(left.toString() + " has left the group");
    }
}
