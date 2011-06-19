package net.froihofer.teaching.gc.examples;

import ads.gc.uniformreliable.TestProviderUniformReliable;
import ads.gc.total.TestProviderTotalOrder;
import ads.gc.nonuniformreliable.TestProviderNonUniformReliable;
import ads.gc.fifo.TestProviderFifo;
import ads.gc.causal.TestProviderCausal;
import net.froihofer.teaching.gc.sim.api.ProcessSim;
import net.froihofer.teaching.gc.sim.api.EventType;
import net.froihofer.teaching.gc.sim.api.Event;
import net.froihofer.teaching.gc.sim.api.TestProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.froihofer.teaching.gc.framework.api.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an example implemenation of a {@link TestProvider} checking for
 * reliable delivery, but the implementation is not fully correct. Please
 * refer to the specification of the individual protocols for correct
 * implementation.
 *
 * @author Lorenz Froihofer
 * @version $Id: TestProviderReliable.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class TestProviderReliable implements TestProvider {
  private static Log log = LogFactory.getLog(TestProviderReliable.class);

  public List<Event> getTestData(int numProcs) {
    return new ArrayList(Arrays.asList(new Event(0,EventType.SEND, new Message(0, 0, "Hello World!"))));
  }

  /**
   * Performs a simple, but faulty check of whether the protcol is reliable.
   */
  public boolean checkResult(ProcessSim[] processes) {
    for (ProcessSim p : processes) {
      if (!p.isCrashed() && (p.getDeliveredMessages().size() != 1)) {
        if (p.getDeliveredMessages().size() > 1) {
          log.error("Process "+p.getId()+" delivered the message more than once.");
        }
        else {
          log.error("Process "+p.getId()+" did not deliver the message.");
        }
        return false;
      }
    }
    return true;
  }

}
