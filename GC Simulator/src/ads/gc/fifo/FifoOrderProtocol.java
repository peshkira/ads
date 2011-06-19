package ads.gc.fifo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.froihofer.teaching.gc.framework.api.GroupCommunication;
import net.froihofer.teaching.gc.framework.api.LamportTimestamp;
import net.froihofer.teaching.gc.framework.api.Message;
import net.froihofer.teaching.gc.framework.api.MessageGuarantee;
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
 * @version $Id: FifoOrderProtocol.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class FifoOrderProtocol implements TransportListener, GroupCommunication {
  private static Log log = LogFactory.getLog(FifoOrderProtocol.class);
  private Process myProcess;
  private Transport myTransport;
  private int[] processIds;
  
  private LamportTimestamp timestamp;
  private Set<MessageMock> received;
  private Map<Integer, ProcessBuffer> buffer;
  

  public void receiveFrom(Serializable data, int senderProcessId) {
      try {
          Message<LamportTimestamp> msg = (Message<LamportTimestamp>) data;
          log.debug(this.l() + "received message from proc: " + senderProcessId + ": " + msg.getObject().toString());
          
          MessageMock mock = new MessageMock(msg.getId(), msg.getSenderId());
          boolean added = this.received.add(mock);
          
          if (added) {
              log.debug(this.l() + "message is not known, start flooding...");

              this.multicast(msg);
              log.debug(this.l() + "message is multicasted to all others");

              ProcessBuffer buff = this.buffer.get(msg.getSenderId());
              List<Message<LamportTimestamp>> list = buff.getBuffer();
              list.add(msg);
              Collections.sort(list, new MessageComparator());

              while (!list.isEmpty() && list.get(0).getOrderMechanism().getValue() == buff.getExpected()) {
                  this.myProcess.deliver(list.get(0));
                  log.info(this.l() + "delivered " + list.get(0).getObject().toString() + " with timestamp " + list.get(0).getOrderMechanism().getValue());
                  list.remove(0);
                  buff.setExpected(buff.getExpected()+1);
              }
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
    this.timestamp = new LamportTimestamp();
    this.received = Collections.synchronizedSet(new HashSet<MessageMock>());
    this.buffer = Collections.synchronizedMap(new HashMap<Integer, ProcessBuffer>());
    
    LamportTimestamp tmp = new LamportTimestamp();
    tmp.next();
    for (int i : processIds) {
        ProcessBuffer b = new ProcessBuffer(Collections.synchronizedList(new ArrayList<Message<LamportTimestamp>>()), tmp.getValue());
        this.buffer.put(i, b);
    }
  }

  public void multicast(Message msg) {
    try {
        msg.setGuarantee(MessageGuarantee.FIFO);
        
        if (msg.getOrderMechanism() == null) {
            this.timestamp.next();
            msg.setOrderMechanism(this.timestamp);
        }
        
        multicastOnTransport(msg);
    }
    catch (Exception e) {
      log.error("Failed to send message.", e);
    }
  }

  protected void multicastOnTransport(Message msg) throws IOException {
    for (int receiverPid : processIds) {
      myTransport.unicast(msg, receiverPid);
    }
  }
  
  private String l() {
      return "[" + this.getProcessId() + "] ";
  }

}
