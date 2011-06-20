package ads.gc.causal;

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
 * @version $Id: TestProviderCausal.java 10:a79260987421 2010/03/30 11:05:00 Lorenz Froihofer $
 */
public class TestProviderCausal implements TestProvider {
  private static Log log = LogFactory.getLog(TestProviderCausal.class);

  public List<Event> getTestData(int numProcs) {
      List<Event> events = new ArrayList<Event>();

      if (numProcs > 2) {
          Event e1 = new Event(0, EventType.SEND, new Message(0, 0, "Some Message"));
          Event e2 = new Event(0, EventType.SEND, new Message(1, 0, "Hello"));
          events.add(e1);
          events.add(e2);

          Event e4 = new Event(1, EventType.SEND, new Message(0, 1, "Other Message"));
          Event e5 = new Event(1, EventType.SEND, new Message(1, 1, "World"));
          events.add(e4);
          events.add(e5);
      }

      return events;
  }

  public boolean checkResult(ProcessSim[] processes) {
      log.error("Not Implemented!");
    return false;
  }

}
