package cn.lhs.websocket.netty;

import cn.lhs.websocket.entity.ChannelMsg;
import cn.lhs.websocket.entity.ClientMsg;
import cn.lhs.websocket.util.redis.RedisForMsg;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private String WEB_SOCKET_URL = "ws://localhost:8888/websocket";
    private RedisForMsg redis = new RedisForMsg("localhost",6379,"123456");

    private WebSocketServerHandshaker handShaker;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final ChannelGroup channels = new DefaultChannelGroup( GlobalEventExecutor.INSTANCE);


    //客户端与服务端创建连接的时候调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.add(ctx.channel());
        logger.info ("客户端与服务端连接开启...");
    }

    //客户端与服务端断开连接的时候调用
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.remove(ctx.channel());
        logger.info ("客户端与服务端连接关闭...");
    }

    //服务端接收客户端发送过来的数据结束之后调用
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    //工程出现异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    //服务端处理客户端websocket请求的核心方法
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        //处理客户端向服务端发起http握手请求的业务
        if (msg instanceof FullHttpRequest) {
            handHttpRequest(ctx,  (FullHttpRequest)msg);
        }else if (msg instanceof WebSocketFrame) { //处理websocket连接业务
            handWebsocketFrame(ctx, (WebSocketFrame)msg);
        }
    }

    //处理客户端与服务端之间的websocket业务
    private void handWebsocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame){
        //判断是否是关闭websocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
            return;
        }
        //判断是否是ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        //判断是否是二进制消息
        if( frame instanceof BinaryWebSocketFrame ){
            ByteBuf buf = frame.content();
            for (int i = 0; i < buf.capacity(); i++){
                byte b = buf.getByte(i);
                logger.info("byte:"+b);
            }
            return;
        }

        //返回应答消息:获取客户端向服务端发送的消息并发出去
        String reqMsg = ((TextWebSocketFrame) frame).text();
        logger.info ("服务端收到客户端的消息====>>>" + reqMsg);
        ClientMsg clientMsg = (ClientMsg) JSONObject.toBean ( JSONObject.fromObject ( reqMsg ), ClientMsg.class );


        //如果是验证信息
        if(clientMsg.getReceiver ().equals ( "server" )){
            if(clientMsg.getMessage ().equals ( "open" )){//如果是连接请求，则将相应ChannelId与用户Id存储起来
                logger.info (clientMsg.getSender()+"连接成功，channelId="+ctx.channel().id());
                ChannelMsg channelMsg = new ChannelMsg();
                channelMsg.setUserId(clientMsg.getSender());
                channelMsg.setChannelId(ctx.channel().id());
                channelMsg.setTime(new Date().getTime());
                redis.saveChannelMsg(channelMsg);
            }else if(clientMsg.getMessage ().equals ( "close" )){//如果是连接请求，则将相应ChannelId与用户Id删除
                logger.info (clientMsg.getSender()+"断开======");
                ChannelMsg channelMsg = new ChannelMsg();
                channelMsg.setUserId(clientMsg.getSender());
                channelMsg.setChannelId(null);
                channelMsg.setTime(new Date().getTime());
                redis.saveChannelMsg(channelMsg);
            }
        }else{
        //如果是用户之间的信息，则把信息发送给相应用户

            //获取接收者的channel
            ChannelId channelId = redis.getChannelId(clientMsg.getReceiver());
            logger.info("接收者channel="+channelId+",发送者channel="+ctx.channel().id());

            //1.如果发送的对象处于离线
            if(channelId == null || NettyConfig.group.find(channelId) == null){
                TextWebSocketFrame tws = new TextWebSocketFrame("接收者处于离线状态");
                //发送给指定的人
                ctx.channel().writeAndFlush(tws);
            }
            //2.如果发送对象处于在线
            else {
                TextWebSocketFrame tws = new TextWebSocketFrame(clientMsg.getSender()+"在"+new Date ().toString() + "说 >=>=> " + clientMsg.getMessage ());
                //发送给指定的人
                channels.add(ctx.channel());
                channels.add(NettyConfig.group.find(channelId));
                channels.writeAndFlush(tws);
                channels.clear();
            }
        }





    }

    //处理客户端向服务端发起http握手请求的业务
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
        if (!req.decoderResult ().isSuccess() || ! ("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL, null, false);
        handShaker = wsFactory.newHandshaker(req);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse ( ctx.channel() );
        }else{
            handShaker.handshake(ctx.channel(), req);
        }
    }
    //服务端向客户端响应消息
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res){
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (res.status().code() != 200) {
            f.addListener( ChannelFutureListener.CLOSE);
        }
    }
}
