package ads.gc.fifo;

import java.util.List;

import net.froihofer.teaching.gc.framework.api.LamportTimestamp;
import net.froihofer.teaching.gc.framework.api.Message;

public class ProcessBuffer {

    private List<Message<LamportTimestamp>> buffer;
    
    private long expected;
    
    public ProcessBuffer(List<Message<LamportTimestamp>> buff, long expected) {
        this.setBuffer(buff);
        this.setExpected(expected);
    }

    public void setBuffer(List<Message<LamportTimestamp>> buffer) {
        this.buffer = buffer;
    }

    public List<Message<LamportTimestamp>> getBuffer() {
        return buffer;
    }

    public void setExpected(long expected) {
        this.expected = expected;
    }

    public long getExpected() {
        return expected;
    }
}
