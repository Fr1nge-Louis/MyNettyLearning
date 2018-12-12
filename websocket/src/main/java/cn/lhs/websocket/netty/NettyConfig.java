package cn.lhs.websocket.netty;


import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class NettyConfig {
    /**
     * 存储每一个客户端接入进来时的channel对象
     * GlobalEventExecutor--服务端将信息广播到每个客户端
     *
     */
    //public static ChannelGroup group = new DefaultChannelGroup ( GlobalEventExecutor.INSTANCE);

    //ImmediateEventExecutor--
    public static ChannelGroup group = new DefaultChannelGroup ( ImmediateEventExecutor.INSTANCE);


}
