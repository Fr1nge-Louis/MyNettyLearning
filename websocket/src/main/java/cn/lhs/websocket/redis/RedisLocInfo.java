package cn.lhs.websocket.redis;

import cn.lhs.websocket.entity.ClientMsg;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


//存储和获取时，需要添加jedis.select ( locationInfo_DB ),不同的数据存储在不同的DB中。
public class RedisLocInfo {

    private final JedisPool jedisPool;

    private final Logger logger = LoggerFactory.getLogger ( this.getClass () );

    private RuntimeSchema<ClientMsg> schema = RuntimeSchema.createFrom ( ClientMsg.class );

    public RedisLocInfo(String ip, int port) {
        this.jedisPool = new JedisPool ( ip,port );
    }

    //获取某一个ClientMsg
    public ClientMsg getCurrentLocInfo(String key){
        ClientMsg clientMsg = null;
        try {
            Jedis jedis = jedisPool.getResource ();
            try {
                //采用protostuff反序列化
                byte[] bytes = jedis.get ( key.getBytes () );
                if (bytes != null){
                    //创建一个属性值为空的LocationInfo对象
                    clientMsg = schema.newMessage ();
                    //反序列化
                    ProtostuffIOUtil.mergeFrom ( bytes,clientMsg,schema );
                }else {
                    logger.error ( "查不到相关数据" );
                }
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
        return clientMsg;
    }

    /*
    //通过List<String> carCodes获得List<LocationInfo> locations
    public List<LocationInfo> getCurrentLocInfos(List<String> carCodes){
        List<Response<byte[]>> responseList = new ArrayList<> ();
        List<LocationInfo> locationInfoList = new ArrayList<> ();
        try {
            Jedis jedis = jedisPool.getResource ();
            Pipeline pipeline = jedis.pipelined ();
            try {
                //获取所有response
                for(String key : carCodes) {
                    responseList.add(pipeline.get ( key.getBytes () ));
                }
                pipeline.sync();
                locationInfoList = responseToLocationInfo ( responseList );
            }finally {
                jedis.close ();
                pipeline.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
        return locationInfoList;
    }


    //获取所有location
    public List<LocationInfo> getAll(){
        List<Response<byte[]>> responseList = new ArrayList<> ();
        List<LocationInfo> locationInfoList = new ArrayList<> ();
        try {
            Jedis jedis = jedisPool.getResource ();
            Pipeline pipeline = jedis.pipelined ();
            jedis.select ( 6 );
            try {
                //获取所有key
                Set<byte[]> keys = jedis.keys ( "*".getBytes () );
                //获取所有response
                for(byte[] key : keys) {
                    responseList.add(pipeline.get ( key ));
                }
                pipeline.sync();
                locationInfoList = responseToLocationInfo ( responseList );
            }finally {
                jedis.close ();
                pipeline.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
        return locationInfoList;
    }

    //存储location--需要加上服务商ID作为参数，key=服务商id+carCode
    public void saveCurrentLocInfo(List<LocationInfo> locationInfos){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 1 );
            //使用Pipeline写入批量数据更快
            Pipeline pipeline = jedis.pipelined ();
            try {
                for (int i = 0; i < locationInfos.size () ; i++) {
                    byte[] key = locationInfos.get ( i ).getCarCode ().getBytes ();
                    //用Protostuff序列化LocationInfo
                    byte[] value = ProtostuffIOUtil.toByteArray ( locationInfos.get ( i ),schema,
                            LinkedBuffer.allocate ( LinkedBuffer.DEFAULT_BUFFER_SIZE ) );
                    pipeline.set ( key,value );
                }
            }finally {
                jedis.close ();
                pipeline.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
    }

    //删除一个location。1:删除成功；0:删除失败。
    public long delLocInfo(String key){
        long result = 0;
        try {
            Jedis jedis = jedisPool.getResource ();
            try {
                result = jedis.del ( key.getBytes () );
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }

        return result;
    }

    //清空所有数据
    public String flushDB(int index){
        String result = "";
        try {
            Jedis jedis = jedisPool.getResource ();
            try {
                jedis.select ( index );
                result = jedis.flushDB ();
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }

        return result;
    }

    //校验鉴权码
    public String isRightCode(String code){
        String result = "";
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 2 );
            try {
                result = jedis.get ( code);
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
        return result;
    }

    //在2数据库中存储单个key-value
    public void saveCode(String key,String value){
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 2 );
            try {
                jedis.set ( key,value );
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
    }

    //校验服务商有效性--0:不合法；1:合法
    public int isLegalServer(String key){
        int result = 0;
        Date now = new Date ();
        long nowTime = now.getTime ();
        try {
            Jedis jedis = jedisPool.getResource ();
            jedis.select ( 2 );
            try {
                String value = jedis.get ( key );
                if(nowTime<=Long.getLong ( value )){
                    result = 1;
                }
            }finally {
                jedis.close ();
            }
        }catch (Exception e){
            logger.error(e.getMessage (),e);
        }
        return result;
    }

*/

    //将所有response转换为ClientMsg
    private List<ClientMsg> responseToLocationInfo(List<Response<byte[]>> responseList){

        List<ClientMsg> clientMsgs = new ArrayList<> ();
        for (int i = 0; i < responseList.size (); i++) {
            byte[] bytes = responseList.get ( i ).get ();
            ClientMsg clientMsg = schema.newMessage ();
            ProtostuffIOUtil.mergeFrom ( bytes,clientMsg,schema );
            clientMsgs.add ( clientMsg );
        }
        return clientMsgs;
    }
}