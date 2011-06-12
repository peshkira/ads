package at.tuwien.ads11.listener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.common.ClientMock;
import at.tuwien.ads11.remote.IServer;
import at.tuwien.ads11.utils.RequestUUID;
import at.tuwien.ads11.utils.ServerConstants;

public class MembershipMessageListener implements AdvancedMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipMessageListener.class);
    
    private ReplicatedServer server;

    public MembershipMessageListener(ReplicatedServer server) {
        this.server = server;
    }

    @Override
    public void membershipMessageReceived(SpreadMessage msg) {
        //SpreadGroup joined = msg.getMembershipInfo().getJoined();
        
    	MembershipInfo info = msg.getMembershipInfo();
    	SpreadGroup left = msg.getMembershipInfo().getLeft();
        
        if(isOwnJoinMessage(info))
        	this.joinMessage(info.getJoined(), msg);
        //if (joined != null && !msg.isSelfDiscard()) {
        //    this.joinMessage(joined, msg);
        //}

        // Why selfdiscard?
        if (left != null && !msg.isSelfDiscard()) {
            this.leaveMessage(info.getLeft());
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage msg) {
    	// USE ServerRequestMessageListener for server requests
    	// USE ClientRequestMessageListener for client requests
    }

    private void joinMessage(SpreadGroup joined, SpreadMessage msg) {
        LOG.info("{} has joined the group", joined.toString());
        // TODO synchornize the new guy...
        
        this.server.askForServerReference();
    }

    private void leaveMessage(SpreadGroup left) {
        LOG.info("{} has left the group", left.toString());
    }
    
    private boolean isOwnJoinMessage(MembershipInfo info) {
    	if(info.isCausedByJoin() && info.isRegularMembership() == false)
    		return true;
    	return false;
    }
}
