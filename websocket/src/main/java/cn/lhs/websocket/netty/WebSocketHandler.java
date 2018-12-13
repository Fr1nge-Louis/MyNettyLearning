package cn.lhs.websocket.netty;

import cn.lhs.websocket.entity.ClientMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import net.sf.json.JSONObject;

import java.util.Date;

public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/websocket";


    //客户端与服务端创建连接的时候调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.add(ctx.channel());
        System.out.println ("客户端与服务端连接开启...");
    }

    //客户端与服务端断开连接的时候调用
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.remove(ctx.channel());
        System.out.println ("客户端与服务端连接关闭...");
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
            handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame.retain());
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
                System.out.println("byte:"+b);
            }
            return;
        }

        //返回应答消息:获取客户端向服务端发送的消息并群发出去
        String reqMsg = ((TextWebSocketFrame) frame).text();
        System.out.println ("服务端收到客户端的消息====>>>" + reqMsg);
        ClientMsg clientMsg = (ClientMsg) JSONObject.toBean ( JSONObject.fromObject ( reqMsg ), ClientMsg.class );

        //如果是验证信息
        if(clientMsg.getReceiver ().equals ( "server" )){
            if(clientMsg.getMessage ().equals ( "open" )){//如果是连接请求，则将相应ChannelId与用户Id存储起来
                System.out.println ("===连接===");
            }else if(clientMsg.getMessage ().equals ( "close" )){//如果是连接请求，则将相应ChannelId与用户Id删除
                System.out.println ("===断开===");
            }
        }else{
        //如果是用户之间的信息，则把信息发送给相应用户
            //1.如果发送的对象处于离线


            //2.如果发送对象处于在线


            TextWebSocketFrame tws = new TextWebSocketFrame(new Date ().toString() + " >=>=> " + clientMsg.getMessage ());


            //群发，服务端向每个连接上来的客户端群发消息
            NettyConfig.group.writeAndFlush(tws);
        }





    }

    //处理客户端向服务端发起http握手请求的业务
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
        if (!req.decoderResult ().isSuccess() || ! ("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse ( ctx.channel() );
        }else{
            handshaker.handshake(ctx.channel(), req);
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
