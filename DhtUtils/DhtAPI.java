package DhtUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
This api serves a result (if any) for a function call to the DHT overlay network
the result is saved into a return buffer that has to be initialized before passing through the call
*/

public class DhtAPI {

    private static int payload_len = 512;
    private static int DHT_NET_PORT = 9911;
    private static final int second = 1000;
    private static String DHT_NET_ADDR = "127.0.0.1";
    private static int wait_timeout = 5 * second;

    public String findNode(String key) {
        byte[] res = this.DHT_exec("FINDNODE " + key);
        try {
            return new String(res, "UTF-8").strip();
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED to find node for key "+key;
        }
    }


    public String getValue(String key){
        byte[] res = this.DHT_exec("GET " + key);
        try {
            return new String(res, "UTF-8").strip();
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED to get key "+key;
        }
    }


    public String put(String key, String value){
        byte[] res = this.DHT_exec( "PUT " + key+" "+ value );
        try {
            return new String(res, "UTF-8").strip();
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED to put key "+key;
        }
    }


    public byte[] DHT_exec(String command) {
        byte[] returnBuff = new byte[256];
        DatagramSocket ds;
        try {
            ds = new DatagramSocket();
            InetAddress ip = InetAddress.getByName(DHT_NET_ADDR);
            DatagramPacket dp = new DatagramPacket(command.getBytes(), command.length(), ip, DHT_NET_PORT);
            ds.send(dp);
            ds.setSoTimeout( wait_timeout );
            byte[] buf = new byte[payload_len];
            DatagramPacket dp1 = new DatagramPacket(buf, payload_len);
            ds.receive(dp1);
            returnBuff = dp1.getData().clone();
            ds.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnBuff;


    }




}
