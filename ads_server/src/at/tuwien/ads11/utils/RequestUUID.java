package at.tuwien.ads11.utils;

import java.io.Serializable;

public class RequestUUID implements Serializable {

    private static final long serialVersionUID = -535520513965421740L;

    private String server;
    
    private Long timestamp;

    public RequestUUID(String server, Long timestamp) {
        this.server = server;
        this.timestamp = timestamp;
    }
    
    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        return server;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((server == null) ? 0 : server.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestUUID other = (RequestUUID) obj;
        if (server == null) {
            if (other.server != null)
                return false;
        } else if (!server.equals(other.server))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }
}
