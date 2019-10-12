package OrdererInterface;
import Common.*;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.Callable;


/**
 * This class represent a thread in the recv connection main thread, it return a OrdererInterface object 
 * with the connected orderer basic informations. 
 */

public class OrdConnectionGetMessage implements Callable<ConnectionInterface> {

    private Socket sock ; 


    public OrdConnectionGetMessage ( Socket sock ){
        this.sock = sock; 
    }


    @Override
    public ConnectionInterface call() throws Exception {
        DataInputStream iss = null;
        try {
            iss = new DataInputStream(sock.getInputStream());    
        } catch (Exception e) {
            e.printStackTrace();
        }
        print_debug("Data input Stream for connection instantiated");
        int tries = 0;
        String msg = "";
        int received;
        String remoteAddr ="";
        String[] tuple; 
        Socket ordSocket ; 
        ConnectionInterface res = null;
        int ordPort = 0; 
        while ( tries < 100 && !sock.isClosed() ) {
            if (  iss.available() > 0 ){
                

                byte ByteMSg[] = new byte[iss.available()]; 
                //read a byte
                received = iss.read(ByteMSg);

                msg = new String(ByteMSg);
                print_debug("received " + received + " bytes, message: " + msg);

                boolean isJoinMsg = msg.contains(Common.ORD_JOIN_S);
                if ( isJoinMsg ){
                    remoteAddr = sock.getRemoteSocketAddress().toString();
                    print_log("received ORDJoin Message from " + remoteAddr);
                    print_debug("socket closed ");
                    sock.close();    
                    msg = msg.replace(Common.ORD_JOIN_S+":", "");
                    tuple = msg.split(":");
                    print_debug("tuple:");
                    for (String  tuple_i : tuple) {
                        print_debug(tuple_i);
                    }
                    msg = tuple[0];
                    try {
                        ordPort = Integer.parseInt(tuple[1]); 
                        print_debug("port "+ ordPort + " id "+msg);   
                        ordSocket = new Socket();
                    } catch (Exception e) {
                        print_log("cannot cast orderer connection port, connection failed.");
                        return null;
                    }
                    return new ConnectionInterface(msg, remoteAddr, ordPort, ordSocket);
                }
                else {
                    return null;
                }
            }
            tries++;
            Thread.sleep(100);
        }
        return res;    
    }


    public static void print_debug(String msg){
        if (Common.DEBUG) System.out.println("ORDGETMSG: "+msg);
    }

    public static void print_log(String msg){
        if (Common.LOG) System.out.println("ORDGETMSG: "+msg);
    }

}