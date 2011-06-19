package ads.gc.uniformreliable;

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
 * @version $Id: TestProviderUniformReliable.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class TestProviderUniformReliable implements TestProvider {
  private static Log log = LogFactory.getLog(TestProviderUniformReliable.class);

  public List<Event> getTestData(int numProcs) {
    return new ArrayList(Arrays.asList(new Event(0,EventType.SEND, new Message(0, 0, "Hello World!"))));
  }

  public boolean checkResult(ProcessSim[] processes) {
      //TODO
      return false;
  }

}
