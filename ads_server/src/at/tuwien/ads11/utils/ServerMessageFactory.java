package at.tuwien.ads11.utils;

import java.io.Serializable;

import spread.MessageFactory;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class ServerMessageFactory  extends MessageFactory {

    private static ServerMessageFactory factory;
    
    public static synchronized ServerMessageFactory getInstance() {
    	if(factory == null) {
    		SpreadMessage defMsg = new SpreadMessage();
            defMsg.setAgreed();
            defMsg.setSelfDiscard(false);
    		factory = new ServerMessageFactory(defMsg);
    	}
    	return factory;
    }
    
    private ServerMessageFactory(SpreadMessage msg) {
        super(msg);
    }
    
    public SpreadMessage getDefaultMessage() {
        return ServerMessageFactory.factory.createMessage();
    }
    
    public SpreadMessage createSafeMessage(short msgType, Serializable payload, SpreadGroup... groups) throws SpreadException {
    	SpreadMessage msg = ServerMessageFactory.factory.createMessage();
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
