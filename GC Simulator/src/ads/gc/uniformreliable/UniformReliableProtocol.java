package ads.gc.uniformreliable;

import java.io.IOException;
import java.io.Serializable;
import net.froihofer.teaching.gc.framework.api.GroupCommunication;
import net.froihofer.teaching.gc.framework.api.Process;
import net.froihofer.teaching.gc.framework.api.Transport;
import net.froihofer.teaching.gc.framework.api.TransportListener;
import net.froihofer.teaching.gc.framework.api.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is just an example of an unreliable protocol demonstrating how to use 
 * the transport and process API in order to implement a group communication 
 * protocol.
 *
 * @author Lorenz Froihofer
 * @version $Id: UniformReliableProtocol.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class UniformReliableProtocol implements TransportListener, GroupCommunication {
  private static Log log = LogFactory.getLog(UniformReliableProtocol.class);
  private Process myProcess;
  private Transport myTransport;
  private int[] processIds;

  public void receiveFrom(Serializable data, int senderProcessId) {
    try {
      Message msg = (Message)data;
      myProcess.deliver(msg);
    }
    catch (Exception e) {
      log.error("Failed to process received message.", e);
    }
  }

  public int getProcessId() {
    return myProcess.getId();
  }

  public void init(Process process, Transport transport, int[] processIds) {
    myProcess = process;
    myTransport = transport;
    this.processIds = processIds;
  }

  public void multicast(Message msg) {
    try {
      msg.setSenderId(getProcessId());
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

}
