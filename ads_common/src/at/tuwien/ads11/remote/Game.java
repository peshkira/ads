package at.tuwien.ads11.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class Game implements Serializable {

    private static final long serialVersionUID = 9046310918111541521L;

    private String name;
    
    private String host;
    
    private String pass;
    
    private List<ClientMock> players;
    
    public Game(String name, String host, String pass) {
        this.setName(name);
        this.setHost(host);
        this.setPass(pass);
        this.setPlayers(new ArrayList<ClientMock>());
    }

    public void setPlayers(List<ClientMock> players) {
        this.players = players;
    }

    public List<ClientMock> getPlayers() {
        return players;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPass() {
        return pass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pass == null) ? 0 : pass.hashCode());
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
        Game other = (Game) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pass == null) {
            if (other.pass != null)
                return false;
        } else if (!pass.equals(other.pass))
            return false;
        return true;
    }
}
