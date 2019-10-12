import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;


public class Prova {

  public static void loadDHTConfigMap(Map<String,String> map ){
       File folder = new File(".");
       String filename ;
       for (final File fileEntry : folder.listFiles()) {

           filename = fileEntry.getName();
           if (filename.endsWith(".txt")){
               try {
                   String content = Files.readString(fileEntry.toPath(), StandardCharsets.US_ASCII);
                   String[] tuple = content.split(":");
                   map.put(tuple[0], tuple[1]);
                } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
   }

   public static void main(String[] args) {

     Map<String,String> map = new TreeMap<>();

     loadDHTConfigMap(map);

     System.out.println("" + map.keySet() );
     System.out.println("" + map.values() );


   }


}
