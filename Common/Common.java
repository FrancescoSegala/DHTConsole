
package Common;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Common {

    public static final boolean RUN = true; 

    public static int DHT_MANAGER_SERVER_PORT = -1;
    public static int DHT_ORDERER_SERVER_PORT = -1;    
    public static int DHT_FRONTEND_SERVER_PORT = -1;
    public static int DHT_NET_PORT = 9911;
    public static final boolean DEBUG = true; 
    public static final boolean LOG = true;
    public static final int second = 1000;
    public static final String FE_JOIN_S = "FE_JOIN";
    public static final String ORD_JOIN_S = "ORD_JOIN";
    public static final String TX_FROM_DHT_INFO = "fromDHT";
    public static final String TX_FROM_FES_INFO = "fromFES";
    public static String DHT_NET_ADDR = "127.0.0.1";


    public static void setManagerServerPort(int newport){
        DHT_MANAGER_SERVER_PORT = newport;
    }

    public static void setFrontendServerPort(int newport){
        DHT_FRONTEND_SERVER_PORT = newport;
    }

    public static void setOrdererServerPort(int newport){
        DHT_ORDERER_SERVER_PORT = newport;
    }
    
    public static void setDhtNetPort(int newport){
        DHT_NET_PORT = newport;
    }

    public static int getRandBoundedInt(int max){
        int random = (int)(Math.random() * max + 1);
        return random;
    }


    public static String getHashString( String input ){
        try {
            // Static getInstance method is called with hashing SHA 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
  
            // digest() method called to calculate message digest of an input and return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16);
            
            //return fixed size hash
            while (hashtext.length() < 32) { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 

        } catch (NoSuchAlgorithmException e) { 
            System.out.println("Exception thrown"
                               + " for incorrect algorithm: " + e); 
  
            return null; 
        } 
    }




}