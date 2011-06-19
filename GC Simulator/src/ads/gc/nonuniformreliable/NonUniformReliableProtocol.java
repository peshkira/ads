package ads.gc.nonuniformreliable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.froihofer.teaching.gc.framework.api.GroupCommunication;
import net.froihofer.teaching.gc.framework.api.Message;
import net.froihofer.teaching.gc.framework.api.Process;
import net.froihofer.teaching.gc.framework.api.Transport;
import net.froihofer.teaching.gc.framework.api.TransportListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ads.gc.util.MessageMock;

/**
 * This is just an example of an unreliable protocol demonstrating how to use
 * the transport and process API in order to implement a group communication
 * protocol.
 * 
 * @author Lorenz Froihofer
 * @version $Id: NonUniformReliableProtocol.java 10:a79260987421 2010/03/30
 *          11:05:00 Lorenz Froihofer $
 */
public class NonUniformReliableProtocol implements TransportListener, GroupCommunication {
    private static Log log = LogFactory.getLog(NonUniformReliableProtocol.class);
    private Process myProcess;
    private Transport myTransport;
    private int[] processIds;

    private Set<MessageMock> received;

    public void receiveFrom(Serializable data, int senderProcessId) {
        log.debug(this.l() + "received message from proc: " + senderProcessId);
        try {
            Message msg = (Message) data;
            MessageMock mock = new MessageMock(msg.getId(), msg.getSenderId());
            boolean added = this.received.add(mock);
            if (added) {
                log.debug(this.l() + "message is not known, deliver and start flooding...");
                // first deliver and than flood
                // it doesn't matter if this process crashes right
                // after the delivery, because
                // the protocol checks only the correct processes
                // afterwards.
                this.myProcess.deliver(msg);
                this.multicast(msg);
                log.debug(this.l() + "message is multicasted to all others");
            } else {
                log.debug(this.l() + "already knows this message");
            }

        } catch (Exception e) {
            log.error(this.l() + "Failed to process received message.", e);
        }
    }

    public int getProcessId() {
        return myProcess.getId();
    }

    public void init(Process process, Transport transport, int[] processIds) {
        myProcess = process;
        myTransport = transport;
        this.processIds = processIds;
        this.received = Collections.synchronizedSet(new HashSet<MessageMock>());
    }

    public void multicast(Message msg) {
        try {
            multicastOnTransport(msg);
        } catch (Exception e) {
            log.error(this.l() + "Failed to send message.", e);
        }
    }

    protected void multicastOnTransport(Message msg) throws IOException {
        for (int receiverPid : processIds) {
            myTransport.unicast(msg, receiverPid);
            log.info(this.l() + "unicasted to " + receiverPid);
        }
    }

    private String l() {
        return "[" + this.getProcessId() + "] ";
    }

}
