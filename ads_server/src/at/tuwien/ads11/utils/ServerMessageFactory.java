package at.tuwien.ads11.utils;

import java.io.Serializable;

import spread.MessageFactory;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class ServerMessageFactory {

    private MessageFactory factory;
    
    public ServerMessageFactory() {
        SpreadMessage defMsg = new SpreadMessage();
        defMsg.setReliable();
        defMsg.setSelfDiscard(false);
        this.factory = new MessageFactory(defMsg);
        
    }
    
    public SpreadMessage getDefaultMessage() {
        return this.factory.createMessage();
    }
    
    public SpreadMessage createSafeMessage(short msgType, Serializable payload, SpreadGroup... groups) throws SpreadException {
    	SpreadMessage msg = this.factory.createMessage();
    	msg.setSafe();
    	msg.setType(msgType);
    	for(SpreadGroup group : groups)
    		msg.addGroup(group);
    	if(payload != null)
    		msg.setObject(payload);
    	return msg;
    }
    
    //TODO add other methods on demand...
    // change the default on the fly, but don't forget
    // change it back...
}
