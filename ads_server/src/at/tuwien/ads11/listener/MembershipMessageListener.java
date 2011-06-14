package at.tuwien.ads11.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;
import at.tuwien.ads11.utils.ServerConstants;
import at.tuwien.ads11.utils.ServerMessageFactory;

public class MembershipMessageListener implements AdvancedMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(MembershipMessageListener.class);

    private ReplicatedServer server;

    public MembershipMessageListener(ReplicatedServer server) {
        this.server = server;
    }

    @Override
    public void membershipMessageReceived(SpreadMessage msg) {
        MembershipInfo info = msg.getMembershipInfo();
        SpreadGroup left = msg.getMembershipInfo().getLeft();
        SpreadGroup joined = msg.getMembershipInfo().getJoined();

        // this does not work properly
        // I changed the method and flipped the condition...
        //if (!isOwnGroupJoinMessage(info) && info.getJoined() != null)
        //    this.synchronizeState(info.getJoined(), msg);
        if (isSelfJoinMessage(info)) {
        	synchronizeState(info);
        } else if (joined != null) { // on each join
            this.server.askForServerReference(joined);
        }
        
        
        // Why selfdiscard?
        // because I wasn't sure what selfdiscard was for at
        // that time.. I am removing it
        if (left != null) {
            this.leaveMessage(left);
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage msg) {
        // USE ServerRequestMessageListener for server requests
        // USE ClientRequestMessageListener for client requests
    }

    private void synchronizeState(MembershipInfo info) {
        LOG.info("{} has joined the group", info.getJoined().toString());
        SpreadGroup[] members = info.getMembers();
        if(members.length < 2) {
        	server.getUpToDate().set(true);
        	LOG.info("No other servers to get state from.");
        } else {
        	server.setGroupMembers(members);
        	askForState(0);
        }	
    }
    
    // TODO: some meaningful exception handling
    private void askForState(int memberIndex) {
    	server.setLastGroupMemberIndex(memberIndex);
    	if(memberIndex > server.getGroupMembers().size()) {
    		try {
				server.rejoinServerGroup();
				LOG.debug("No group members left to get state from, rejoining server group.");
    		} catch (SpreadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return;
    	}
    	try {
			SpreadMessage request = ServerMessageFactory.getInstance().createSafeMessage(ServerConstants.MSG_GET_SERVER_STATE, null, server.getOwnGroup(), server.getGroupMembers().get(memberIndex));
			server.sendMsg(request);
			LOG.debug("Requesting state from {}", server.getGroupMembers().get(memberIndex));
    	} catch (SpreadException e) {
			// TODO if exception on send -> go to next member
			e.printStackTrace();
		}
    }

    private void leaveMessage(SpreadGroup left) {
        LOG.info("{} has left the group", left.toString());
        if(!server.getUpToDate().get() && left.equals(server.getGroupMembers().get(server.getLastGroupMemberIndex())))
        	askForState(server.getLastGroupMemberIndex() + 1);
    }
   
    private boolean isSelfJoinMessage(MembershipInfo info)
    {
    	if(info.isCausedByJoin() && info.getGroup().equals(server.getServerGroup()) && info.getJoined().toString().startsWith(server.getServerId(), 1))
    		return true;
    	return false;
    }
}
