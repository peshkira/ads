package at.tuwien.ads11.utils;

import spread.MessageFactory;
import spread.SpreadMessage;

public class ServerMessageFactory  {

    private MessageFactory factory;
    
    public ServerMessageFactory() {
        SpreadMessage defMsg = new SpreadMessage();
        defMsg.setAgreed();
        defMsg.setSelfDiscard(false);
        this.factory = new MessageFactory(defMsg);
        
    }
    
    public SpreadMessage getDefaultMessage() {
        return this.factory.createMessage();
    }
    
    //TODO add other methods on demand...
    // change the default on the fly, but don't forget
    // change it back...
}
