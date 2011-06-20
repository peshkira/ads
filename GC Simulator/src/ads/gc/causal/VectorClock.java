package ads.gc.causal;

import net.froihofer.teaching.gc.framework.api.LamportTimestamp;

public class VectorClock {

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
}
