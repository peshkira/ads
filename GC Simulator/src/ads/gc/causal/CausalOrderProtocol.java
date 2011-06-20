package ads.gc.causal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.froihofer.teaching.gc.framework.api.GroupCommunication;
import net.froihofer.teaching.gc.framework.api.LamportTimestamp;
import net.froihofer.teaching.gc.framework.api.MessageGuarantee;
import net.froihofer.teaching.gc.framework.api.Process;
import net.froihofer.teaching.gc.framework.api.Transport;
import net.froihofer.teaching.gc.framework.api.TransportListener;
import net.froihofer.teaching.gc.framework.api.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ads.gc.util.MessageMock;

/**
 * This is just an example of an unreliable protocol demonstrating how to use 
 * the transport and process API in order to implement a group communication 
 * protocol.
 *
 * @author Lorenz Froihofer
 * @version $Id: CausalOrderProtocol.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class CausalOrderProtocol implements TransportListener, GroupCommunication {
  private static Log log = LogFactory.getLog(CausalOrderProtocol.class);
  private Process myProcess;
  private Transport myTransport;
  private int[] processIds;
  
  private VectorClock clock;

  private Set<MessageMock> received;

  public void receiveFrom(Serializable data, int senderProcessId) {
      log.debug(this.l() + "received message from proc: " + senderProcessId);
      try {
          Message msg = (Message) data;
          MessageMock mock = new MessageMock(msg.getId(), msg.getSenderId());
          boolean added = this.received.add(mock);
          if (added) {
              log.debug(this.l() + "message is not known, start flooding...");
              // first multicast to others and
              // only if done than chek for delivery
              this.multicast(msg);
              log.debug(this.l() + "message is multicasted to all others.");

              int sender = msg.getSenderId();
              LamportTimestamp otherClock = this.clock.getTimestamps()[sender];
              LamportTimestamp myClock = this.clock.getTimestamps()[sender];
              
              this.myProcess.deliver(msg);
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
      this.clock = new VectorClock(processIds);
  }

  public void multicast(Message msg) {
      try {
          msg.setGuarantee(MessageGuarantee.CAUSAL);
          
          //I am the sender, not the flooder
          if (msg.getOrderMechanism() == null) {
              this.clock.getTimestamps()[this.getProcessId()].next();
              msg.setOrderMechanism(this.clock);
          }
          
          multicastOnTransport(msg);
      } catch (Exception e) {
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
