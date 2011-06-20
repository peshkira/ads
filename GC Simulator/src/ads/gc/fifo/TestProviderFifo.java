package ads.gc.fifo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * @version $Id: TestProviderFifo.java 10:a79260987421 2010/03/30 11:05:00
 *          Lorenz Froihofer $
 */
public class TestProviderFifo implements TestProvider {
    private static Log log = LogFactory.getLog(TestProviderFifo.class);
    
    Map<Integer, List<Integer>> deliveryMap = null;

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

    private boolean checkFaultyProcesses(List<ProcessSim> correct, List<ProcessSim> faulty) {
        // if there are no correct processes at all
        // we can just return true...
        if (correct.isEmpty()) {
            return true;
        }

        for (ProcessSim fault : faulty) {
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

            //TODO
            // check if order is correct if any messages are delivered...
            if(!incorrectCheckForFIFOorder(fault))
            	return false;
           
        }

        return true;
    }

    private boolean checkCorrectProcesses(List<ProcessSim> correct) {
        List<Message> dMsgs = new ArrayList<Message>();
        long delivered = -1;

        for (ProcessSim proc : correct) {
            long procDel = proc.getDeliveredMessages().size();

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

                //TODO
                // test that all received messages are fifo within
                // the sending process
                // not cool but functional for this provider
                if (deliveryMap == null)
                	if(!initDeliveryMap(proc))
                		return false;
                else
                	if(!correctCheckForFIFOorder(proc))
                		return false;

            }
        }

        return true;
    }
    
    /**
     * Inits deliveryMap and checks if some of the delivered messages was delivered twice.
     * @param proc
     * @return true if everything OK, false if a msg delivered twice
     */
    private boolean initDeliveryMap(ProcessSim proc) {
    	deliveryMap = new HashMap<Integer, List<Integer>>();
    	for(Message m : proc.getDeliveredMessages()) {
    		List<Integer> msgsForSender = deliveryMap.get(m.getSenderId());
    		if(msgsForSender == null) {
    			msgsForSender = new LinkedList<Integer>();
    			msgsForSender.add(m.getId());
    			deliveryMap.put(m.getSenderId(), msgsForSender);
    		} else {
    			if(msgsForSender.contains(m.getId()))
    				return false;
    			else
    				msgsForSender.add(m.getId());
    		}
    	}
    	return true;
    }
    
    private boolean correctCheckForFIFOorder(ProcessSim proc) {
    	Map<Integer, LinkedList<Integer>> tmpMap = cloneMap(deliveryMap);
    	if(!checkFIFOorder(proc, tmpMap))
    		return false;
    	
    	if(tmpMap.size() != 0)
    		return false;
    	
    	return true;
    }
    
    private boolean incorrectCheckForFIFOorder(ProcessSim proc) {
    	Map<Integer, LinkedList<Integer>> tmpMap = cloneMap(deliveryMap);
    	return checkFIFOorder(proc, tmpMap);
    }
    
    private boolean checkFIFOorder(ProcessSim proc, Map<Integer, LinkedList<Integer>> map) {
    	for(Message m : proc.getDeliveredMessages()) {
    		LinkedList<Integer> tmpList = map.get(m.getSenderId());
    		if(tmpList == null)
    			return false;
    		
    		if(tmpList.getFirst().compareTo(m.getId()) == 0) {
    			tmpList.removeFirst();
    			if(tmpList.size() == 0)
    				map.remove(tmpList);
    		} else
    			return false;
    	}
    	return true;
    }
    
    private Map<Integer, LinkedList<Integer>> cloneMap(Map<Integer, List<Integer>> source) {
    	Map<Integer, LinkedList<Integer>> result = new HashMap<Integer, LinkedList<Integer>>();
    	for(Integer key : source.keySet()) {
    		LinkedList<Integer> resultList = new LinkedList<Integer>();
    		for(Integer i : source.get(key)) {
    			resultList.add(i);
    		}
    		result.put(key, resultList);
    	}
    	
    	return result;
    }

}
