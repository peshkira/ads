package at.tuwien.ads11.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;
import at.tuwien.ads11.ReplicatedServer;

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

        // this does not work properly
        // I changed the methdo and flipped the condition...
        if (!isOwnGroupJoinMessage(info) && info.getJoined() != null)
            this.joinMessage(info.getJoined(), msg);

        // Why selfdiscard?
        // because I wasn't sure what selfdiscard was for at
        // that time.. I am removing it
        if (left != null) {
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

        this.server.askForServerReference(joined);
    }

    private void leaveMessage(SpreadGroup left) {
        LOG.info("{} has left the group", left.toString());
    }

    private boolean isOwnGroupJoinMessage(MembershipInfo info) {
        // why == false
        // and this method always returns false...
        // if (info.isCausedByJoin() && info.isRegularMembership() == false) {
        // return true;
        // }
        // return false;
        if (info.isCausedByJoin() && info.getGroup().equals(server.getOwnGroup())) {
            return true;
        }

        return false;
    }
}
