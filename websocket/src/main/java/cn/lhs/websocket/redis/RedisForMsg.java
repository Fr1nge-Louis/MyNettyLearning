package cn.lhs.websocket.redis;

import cn.lhs.websocket.entity.ClientMsg;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisForMsg {
    private final JedisPool jedisPool;

    private final Logger logger = LoggerFactory.getLogger ( this.getClass () );

    private RuntimeSchema<ClientMsg> schema = RuntimeSchema.createFrom ( ClientMsg.class );

    public RedisForMsg(String ip, int port) {
        this.jedisPool = new JedisPool ( ip,port );
    }

    //在数据库中存储单个key-value:存储用户Id所对应的ChannelId
    public void saveCode(String key,String value){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 2 );//选择数据库
            try {
                jedis.set ( key,value );
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
    }


}
