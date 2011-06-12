package at.tuwien.ads11.utils;

public final class ServerConstants {
    
    public static final String SPREAD_SERVER_GROUP = "serverGroup";
    public static final String SERVER_OBJECT = "replicated.server";

    public static final short MSG_GET_SERVER_REFERENCE = 10;
    public static final short MSG_GET_SERVER_REFERENCE_RESPONSE = 11;
    public static final short MSG_GET_SERVER_STATE = 12;
    public static final short MSG_GET_SERVER_STATE_RESPONSE = 13;
    
    public static final short MSG_PLAYER_REGISTER = 20;
    public static final short MSG_PLAYER_UNREGISTER = 21;
    public static final short MSG_GAME_CREATE = 22;
    public static final short MSG_GAME_CANCEL = 23;
    public static final short MSG_GAME_JOIN = 24;
    public static final short MSG_GAME_LEAVE = 25;
    public static final short MSG_GAME_START = 26;

        
    private ServerConstants() {
        
    }
}
