package FrontendInterface;
import Common.*;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public class FrontendConnectionGetMessage implements Callable<ConnectionInterface> {
    private Socket sock ; 


    public FrontendConnectionGetMessage ( Socket sock ){
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
        int tries = 0;
        String msg = "";
        String[] tuple; 
        Socket frontendSocket ; 
        ConnectionInterface res = null;
        int frontendPort = 0; 
        while ( tries < 100 && !sock.isClosed() ) {
            if (  iss.available() > 0 ){

                print_debug( "availables "+ iss.available() );
                byte bMsg[] = new byte[iss.available()];
                bMsg = iss.readNBytes(iss.available());
                msg = new String(bMsg);
                print_debug(msg);

                boolean isJoinMsg = msg.contains(Common.FE_JOIN_S);
                if ( isJoinMsg ){
                    print_log("received FRONTEND_JOIN Message from " + sock.getRemoteSocketAddress() );   
                    msg = msg.replace(Common.FE_JOIN_S+":", "");
                    tuple = msg.split(":");
                    msg = tuple[0];
                    try {
                        frontendPort = Integer.parseInt(tuple[1]);    
                        frontendSocket = this.sock;
                    } catch (Exception e) {
                        print_debug("cannot cast frontend connection port, connection failed.");
                        return null;
                    }
                    return new ConnectionInterface(msg, msg, frontendPort, frontendSocket);
                }
                else {
                    //ignore ?
                    return null;
                }
            }
            tries++;
            Thread.sleep(100);
        }
        return res;    
    }


    public static void print_debug(String msg){
        if (Common.DEBUG) System.out.println("FEGETMSG: "+msg);
    }


    public static void print_log(String msg){
        if (Common.LOG) System.out.println("FEGETMSG: "+msg);
    }

}