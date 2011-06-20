package ads.gc.causal;

import java.io.Serializable;

import net.froihofer.teaching.gc.framework.api.LamportTimestamp;

public class VectorClock implements Serializable {

    private static final long serialVersionUID = 1849066984060802919L;

    private int[] processIds;
    
    private LamportTimestamp[] timestamps;
    
    public VectorClock(int[] processIds) {
        this.setProcessIds(processIds);
        this.setTimestamps(new LamportTimestamp[processIds.length]);
        for (int i = 0; i < processIds.length; i++) {
            this.getTimestamps()[i] = new LamportTimestamp();
        }
    }

    public void setProcessIds(int[] processIds) {
        this.processIds = processIds;
    }

    public int[] getProcessIds() {
        return processIds;
    }

    public void setTimestamps(LamportTimestamp[] timestamps) {
        this.timestamps = timestamps;
    }

    public LamportTimestamp[] getTimestamps() {
        return timestamps;
    }
    
    public boolean canBeDelivered(VectorClock other) {
        if (this.timestamps.length != other.timestamps.length) {
            throw new RuntimeException("VectorClocks have different sizes");
        }
        
        int diff = 0;
        
        for (int i = 0; i < timestamps.length; i++) {
            if (this.timestamps[i].getValue() != other.getTimestamps()[i].getValue()) {
                diff++;
            }
        }
        
        if (diff > 1) {
            return false; //not yet ready...
        }
        
        return true;
    }
}
