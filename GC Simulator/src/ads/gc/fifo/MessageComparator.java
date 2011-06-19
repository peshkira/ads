package ads.gc.fifo;

import java.util.Comparator;

import net.froihofer.teaching.gc.framework.api.LamportTimestamp;
import net.froihofer.teaching.gc.framework.api.Message;

public class MessageComparator implements Comparator<Message<LamportTimestamp>> {

    @Override
    public int compare(Message<LamportTimestamp> m1, Message<LamportTimestamp> m2) {
        return m1.getOrderMechanism().compareTo(m2.getOrderMechanism());
    }


}
