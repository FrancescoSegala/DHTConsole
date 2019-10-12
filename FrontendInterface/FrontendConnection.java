package FrontendInterface;
import Common.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FrontendConnection implements Runnable {

    //ArrayList<Future<ConnectionInterface>> frontendList;
    //Hashtable<String, ConnectionInterface> frontendMap;
    TxsStack messages;

    public FrontendConnection(TxsStack msg) {
        //this.frontendList = new ArrayList<>();
        //this.frontendMap = new Hashtable<>();
        messages = msg;
    }

    /*********************************************************************************************************/
    /*********************************** Main thread routine *************************************************/
 
    @Override
    public void run() {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // recv connection thread start
        FrontendRecvConnectionThread connectionRunnable = new FrontendRecvConnectionThread();
        executor.execute(connectionRunnable);
        
    }

    /*********************************************************************************************************/
    /***************************** Internal-threads classes  *************************************************/

    private class FrontendRecvConnectionThread implements Runnable {

        int port = Common.DHT_FRONTEND_SERVER_PORT;

        @Override
        public void run() {

            ExecutorService executor = Executors.newFixedThreadPool(10);
            try {
                ServerSocket server = new ServerSocket(port);
                
                if (server.isBound()) print_log( "Server started listening on port "+ port);

                while (server.isBound()) {

                    Socket frontend = server.accept();
                    print_log("accepted frontend connection from "+ frontend.getRemoteSocketAddress() );
                    FrontendConnectionGetMessage getMsg = new FrontendConnectionGetMessage(frontend);
                    Future<ConnectionInterface> elem = executor.submit(getMsg);

                    //frontendList.add(elem);
                    //ConnectionManager managerTask = new ConnectionManager(frontendList, frontendMap);
                    //executor.execute(managerTask);
                    
                    RecvTxThread receiver = new RecvTxThread( elem.get() , messages);
                    executor.execute(receiver);


                }
                server.close();

            } catch (Exception e) {
                print_debug("exception occurred in thread");
            }

        }

    }



    private class RecvTxThread implements Runnable {

        private ConnectionInterface frontend;
        private TxsStack messages;

        public RecvTxThread(ConnectionInterface fe, TxsStack msgref) {
            this.frontend = fe;
            this.messages = msgref;
        }

        @Override
        public void run() {
            DataInputStream iss = null ;
            if (this.frontend.getSocket() == null ) {
                return ;
            }   
            boolean connected = this.frontend.getSocket().isConnected();
            //check if the socket is instantiated
            if ( !connected ){
                try {
                    print_debug("lets connect to the frontend ");
                    InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), this.frontend.getPort());
                    this.frontend.getSocket().connect(addr);
                } catch (Exception e) {
                    print_debug("cannot connect frontend socket for receive messages");
                    e.printStackTrace();
                }
            }

            //instantiation of a DataStream for the socket
            try {
                iss = new DataInputStream(this.frontend.getSocket().getInputStream());
            } catch (Exception e) {
                print_debug("cannot instantiate a new DataInputStream ");
                e.printStackTrace();
            }

            //while the socket is connected retrieve messages and push it to the stack
            while (this.frontend.getSocket().isConnected()) {

                try {
    
                    if (iss.available() > 0 ) {

                        byte bMesg[] = new byte[iss.available()];
                        bMesg = iss.readNBytes(iss.available());
                        String msg = new String(bMesg);
                        
                        print_log("received tx: "+ msg);
                        //this parse a message of the form src:dest:msg 
                        Tx transaction = Tx.string2tx(msg);
                        transaction.setInfo(Common.TX_FROM_FES_INFO);
                        messages.pushTX(transaction);
                    }
                
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            print_log("frontend disconnected");


        }

    }




    
    /*********************************************************************************************************/
    /*********************************************************************************************************/

    

    public static void print_debug(String msg){
        if (Common.DEBUG) System.out.println("FECONN: "+msg);
    }
 

    public static void print_log(String msg){
        if (Common.LOG) System.out.println("FECONN: "+msg);
    }


}