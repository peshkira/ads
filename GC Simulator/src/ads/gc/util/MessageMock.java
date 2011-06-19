package ads.gc.util;

public class MessageMock {

    private int messageId;
    
    private int senderId;
    
    public MessageMock(int mId, int sId) {
        this.setMessageId(mId);
        this.setSenderId(sId);
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getSenderId() {
        return senderId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + messageId;
        result = prime * result + senderId;
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
        MessageMock other = (MessageMock) obj;
        if (messageId != other.messageId)
            return false;
        if (senderId != other.senderId)
            return false;
        return true;
    }
    
}
