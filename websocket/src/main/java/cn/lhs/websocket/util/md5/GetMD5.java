package cn.lhs.websocket.util.md5;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetMD5 {
    public static String encryptString(String password){
        MessageDigest messageDigest = null;
        StringBuffer md5str = new StringBuffer();
        int digital = 0;
        try {
            messageDigest = MessageDigest.getInstance ( "MD5" );
            byte[] bytes = password.trim ().getBytes ();
            byte[] MD5_bytes = messageDigest.digest (bytes);
            //将字节数组转化为16进制字符串
            for (int i = 0; i < MD5_bytes.length; i++) {//把数组中每一字节换成16进制连成md5字符串
                digital = MD5_bytes[i];
                if (digital < 0){//16及以上的数转16进制是两位
                    digital += 256;
                }
                if(digital < 16){//如果是0~16之间的数的话则变成0X
                    md5str.append ( "0" );
                }
                md5str.append ( Integer.toHexString ( digital ) );
            }
            return md5str.toString ().toUpperCase ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
            return null;
        }
    }
}
