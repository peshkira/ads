package ads.gc.uniformreliable;

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
 * @version $Id: TestProviderUniformReliable.java 10:a79260987421 2010/03/30
 *          11:05:00 Lorenz Froihofer $
 */
public class TestProviderUniformReliable implements TestProvider {
    private static Log log = LogFactory.getLog(TestProviderUniformReliable.class);

    public List<Event> getTestData(int numProcs) {
        List<Event> events = new ArrayList<Event>();

        if (numProcs > 2) {
            Event e1 = new Event(0, EventType.SEND, new Message(0, 0, "Hello World!"));
            Event e2 = new Event(1, EventType.SEND, new Message(1, 1, "Blah Blah"));
            Event e3 = new Event(2, EventType.SEND, new Message(2, 2, "42"));
            events.add(e1);
            events.add(e2);
            events.add(e3);
        }

        return events;

    }

    /*
     * The check is specific to this test provider...
     * and assumes that the sended messages have different ids for easier
     * testing purposes.
     */
    public boolean checkResult(ProcessSim[] processes) {

        List<ProcessSim> crashed = new ArrayList<ProcessSim>();
        List<ProcessSim> correct = new ArrayList<ProcessSim>();

        for (ProcessSim proc : processes) {
            if (proc.isCrashed()) {
                crashed.add(proc);
            } else {
                correct.add(proc);
            }
        }

        boolean corr = this.checkCorrectProcesses(correct);
        boolean fault = this.checkFaultyProcesses(correct, crashed);

        return corr && fault;
    }

    /**
     * Check whether all faulty processes have less or equal delivered messages
     * as the correct processes. Assert that there are no delivered messages in
     * a faulty process that are not delivered in the correct processes.
     * 
     * 
     * @param correct
     *            the list with the correct processes
     * @param faulty
     *            the list with the fault processes.
     * @return true if all checks pass, false otherwise.
     */
    private boolean checkFaultyProcesses(List<ProcessSim> correct, List<ProcessSim> faulty) {

        // if there are no correct processes at all
        // we can just return true...
        if (correct.isEmpty()) {
            return true;
        }

        
        for (ProcessSim fault : faulty) {
        	if(!checkForDuplicateMessages(fault.getDeliveredMessages()))
            	return false;
        	
        	for (ProcessSim corr : correct) {
                if (fault.getDeliveredMessages().size() > corr.getDeliveredMessages().size()) {
                    log.error("Process " + fault.getId() + " has delivered more messages than the correct processes");
                    return false;
                }
                
                for (Message m : fault.getDeliveredMessages()) {
                    if (!corr.getDeliveredMessages().contains(m)) {
                        log.error("Correct process " + corr.getId() + "does not contain a message of "
                                + "faulty process " + fault.getId());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check if all correct processes have the same amount of delivered
     * messages. Check that the messages are the same in each process.
     * 
     * @param correct
     *            the list with all correct processes
     * @return true if all processes pass the checks, false otherwise.
     */
    private boolean checkCorrectProcesses(List<ProcessSim> correct) {
        List<Message> dMsgs = new ArrayList<Message>();
        long delivered = -1;

        for (ProcessSim proc : correct) {
            long procDel = proc.getDeliveredMessages().size();
            
            if(!checkForDuplicateMessages(proc.getDeliveredMessages()))
            	return false;
            
            if (delivered == -1) {
                delivered = procDel;
                dMsgs = proc.getDeliveredMessages();
                continue;

            } else if (delivered != procDel) {
                log.error("Process " + proc.getId() + " delivered different count of messages");
                return false;

            } else {
                if (!proc.getDeliveredMessages().containsAll(dMsgs)) {
                    log.error("Process " + proc.getId() + " does not contain all messages");
                    return false;
                }
            }
        }

        return true;
    }
    
    private boolean checkForDuplicateMessages(List<Message> msgs) {
    	Set<Message> msgSet = new HashSet<Message>(msgs);
    	if(msgSet.size() != msgSet.size())
    		return false;
    	return true;
    }

}
