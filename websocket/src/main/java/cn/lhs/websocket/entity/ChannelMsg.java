package cn.lhs.websocket.entity;

import io.netty.channel.ChannelId;

public class ChannelMsg {
    private String userId;
    private ChannelId channelId;
    private long time;

    public ChannelMsg() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
