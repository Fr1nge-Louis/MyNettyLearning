package cn.lhs.websocket.util.redis;

import cn.lhs.websocket.entity.ChannelMsg;
import cn.lhs.websocket.entity.ClientMsg;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class RedisForMsg {
    private final JedisPool jedisPool;

    private String passWord;

    private final Logger logger = LoggerFactory.getLogger ( this.getClass () );

    private RuntimeSchema<ChannelMsg> schemaChannelMsg = RuntimeSchema.createFrom ( ChannelMsg.class );

    private RuntimeSchema<ClientMsg> schemaClientMsg = RuntimeSchema.createFrom(ClientMsg.class);

    public RedisForMsg(String ip, int port,String passWord) {
        this.jedisPool = new JedisPool ( ip,port );
        this.passWord = passWord;
    }

    //储存用户所对应的channel
    public void saveChannelMsg(ChannelMsg channelMsg){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.auth(passWord);
            jedis.select ( 10 );
            try {
                byte[] key = channelMsg.getUserId ().getBytes ();
                //用Protostuff序列化LocationInfo
                byte[] value = ProtostuffIOUtil.toByteArray (channelMsg,schemaChannelMsg,
                        LinkedBuffer.allocate ( LinkedBuffer.DEFAULT_BUFFER_SIZE ) );
                jedis.set(key,value);
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
    }

    //获取用户所对应的channel
    public ChannelId getChannelId(String userId){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.auth(passWord);
            jedis.select ( 10 );
            try {
                byte[] value = jedis.get(userId.getBytes());
                ChannelMsg channelMsg = schemaChannelMsg.newMessage ();
                ProtostuffIOUtil.mergeFrom ( value,channelMsg,schemaChannelMsg );
                return channelMsg.getChannelId();
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
            return null;
        }
    }


    //储存用户未读消息
    public void saveClientMsg(){

    }

    //获取用户未读消息
    public List<ClientMsg> getClientMsg(){
        return null;
    }


}
