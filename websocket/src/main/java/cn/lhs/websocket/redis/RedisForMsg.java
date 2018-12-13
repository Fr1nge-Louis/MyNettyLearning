package cn.lhs.websocket.redis;

import cn.lhs.websocket.entity.ChannelMsg;
import cn.lhs.websocket.entity.ClientMsg;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class RedisForMsg {
    private final JedisPool jedisPool;

    private final Logger logger = LoggerFactory.getLogger ( this.getClass () );

    private RuntimeSchema<ChannelMsg> schema = RuntimeSchema.createFrom ( ChannelMsg.class );

    public RedisForMsg(String ip, int port) {
        this.jedisPool = new JedisPool ( ip,port );
    }

    public void saveChannelMsg(ChannelMsg channelMsg){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 1 );
            //使用Pipeline写入批量数据更快
            Pipeline pipeline = jedis.pipelined ();
            try {
                byte[] key = channelMsg.getUserId ().getBytes ();
                //用Protostuff序列化LocationInfo
                byte[] value = ProtostuffIOUtil.toByteArray (
                        channelMsg,schema, LinkedBuffer.allocate ( LinkedBuffer.DEFAULT_BUFFER_SIZE ) );
                pipeline.set ( key,value );
            }finally {
                jedis.close ();
                pipeline.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
    }



}
