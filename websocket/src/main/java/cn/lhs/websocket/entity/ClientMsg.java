package cn.lhs.websocket.entity;

public class ClientMsg {
    private String sender;
    private String receiver;//server：建立连接完成自动发送的验证信息；其他：用户之间发送的信息
    private long time;
    private String message;//server：建立连接完成自动发送的验证信息；其他：用户之间发送的信息

    public ClientMsg() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ClientMsg{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", time=" + time +
                ", message='" + message + '\'' +
                '}';
    }
}
