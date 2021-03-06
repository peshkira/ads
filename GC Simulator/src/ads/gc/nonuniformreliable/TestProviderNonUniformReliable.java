package ads.gc.nonuniformreliable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.froihofer.teaching.gc.framework.api.Message;
import net.froihofer.teaching.gc.sim.api.Event;
import net.froihofer.teaching.gc.sim.api.EventType;
import net.froihofer.teaching.gc.sim.api.ProcessSim;
import net.froihofer.teaching.gc.sim.api.TestProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an example implemenation of a {@link TestProvider} checking for
 * reliable delivery, but the implementation is not fully correct. Please refer
 * to the specification of the individual protocols for correct implementation.
 * 
 * @author Lorenz Froihofer
 * @version $Id: TestProviderNonUniformReliable.java 10:a79260987421 2010/03/30
 *          11:05:00 Lorenz Froihofer $
 */
public class TestProviderNonUniformReliable implements TestProvider {
    private static Log log = LogFactory.getLog(TestProviderNonUniformReliable.class);

    public List<Event> getTestData(int numProcs) {
        List<Event> events = new ArrayList<Event>();
        if (numProcs > 0) {
            Event e = new Event(0, EventType.SEND, new Message(0, 0, "Hello World!"));
            events.add(e);
        }
        return events;
    }

    // check if there is a message is delivered more than once.
    // be careful not to check that there is exactly 1 message
    // there can be none and the protocol is still correct
    // if no one has received it...
    public boolean checkResult(ProcessSim[] processes) {
        long delivered = -1;
        for (ProcessSim p : processes) {
            Set<Message> msgSet = new HashSet<Message>(p.getDeliveredMessages());
            if(msgSet.size() != p.getDeliveredMessages().size())
            	return false;
            
        	if (!p.isCrashed()) {
                int procDel = p.getDeliveredMessages().size();
                if (delivered == -1) {
                    delivered = procDel;
                } else if (delivered != procDel) {
                    log.error("Process " + p.getId() + " delivered different count of messages");
                    return false;
                }
            }
        }
        return true;
    }

}
