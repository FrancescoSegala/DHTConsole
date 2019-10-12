package OrdererInterface;
import Common.*;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class OrdConnection implements Runnable {

    ArrayList<Future<ConnectionInterface>> futureOrdererList;
    Hashtable<String, ConnectionInterface> ordererMap;
    TxsStack messages;

    public OrdConnection(TxsStack msg, Hashtable<String, ConnectionInterface> orderers ) {
        this.futureOrdererList = new ArrayList<>();
        this.ordererMap = orderers;
        messages = msg;
    }
/*****************************************************************************************************/
    /******************************* Main thread run method ******************************************/
    @Override
    public void run() {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        // recv connection thread start 
        OrdererRecvConnectionThread recvRunn = new OrdererRecvConnectionThread();
        executor.execute(recvRunn);

        // send tx thread start
        OrdererSendTxThread sendRunn = new OrdererSendTxThread(messages);
        executor.execute(sendRunn);

    }


    /*****************************************************************************************************/
    /******************************* internal sub-threads  classes ***************************************/
    /*******************************                               ***************************************/
    /*****************************************************************************************************/

    private class OrdererRecvConnectionThread implements Runnable {

        int port = Common.DHT_ORDERER_SERVER_PORT;

        @Override
        public void run() {

            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                ServerSocket server = new ServerSocket(port);
                
                while (server.isBound()) {
                    print_log( "Server listening on port " + port );
                    Socket orderer = server.accept();
                    print_log("Started Connection Routine for "+orderer.getRemoteSocketAddress().toString());
                    OrdConnectionGetMessage getMsg = new OrdConnectionGetMessage(orderer);
                    Future<ConnectionInterface> elem = executor.submit(getMsg);

                    futureOrdererList.add(elem);
                    ConnectionManager managerTask = new ConnectionManager(futureOrdererList, ordererMap);
                    executor.execute(managerTask);
                    //if the Future<OI> is not ready will be processed next manager execution and added 
                    // so if for some reason a thread is not ready in the timeout time he should then retry 

                }
                server.close();

            } catch (Exception e) {
                print_debug("exception occurred in thread");
            }

        }

    }



    private class OrdererSendTxThread implements Runnable {

        private TxsStack txs;

        public OrdererSendTxThread(TxsStack txsref) {
            this.txs = txsref;
        }

        @Override
        public void run() {

            while (true) {

                //check whether there are unprocessed txs 
                if (this.txs.hasElements() && ordererMap.size() > 0) {

                    Tx elem = this.txs.getTX();
                    
                    
                    String dest = elem.getDest();
                    ConnectionInterface destInterface = ordererMap.get(dest); 
                    //retrieve the ADT of the dest orderer 
  
                    if (destInterface != null && destInterface.getId().equals(dest)) {
                        print_debug("dest interface == dest ");
                        try {

                            boolean closed = destInterface.getSocket().isClosed();
                            if ( closed ){
                                print_debug("dest interface socket is closed ");
                                Socket s = new Socket(InetAddress.getLocalHost(), destInterface.getPort());
                                destInterface.setSock( s );
                            }

                            boolean connected = destInterface.getSocket().isConnected();
                            if (!connected) {
                                //connect to orderer only if the connection is not set 
                                print_debug("let's connect to the orderer!");
                                InetSocketAddress addr = new InetSocketAddress(InetAddress.getLocalHost(), destInterface.getPort());
                                destInterface.getSocket().connect( addr );
                            }

                            print_debug("open and connected ");
                            if (  !destInterface.getSocket().isOutputShutdown()) {
                                DataOutputStream oss = new DataOutputStream(destInterface.getSocket().getOutputStream());
                                oss.writeBytes(elem.getMsg());   
                                print_debug("sent to orderer "+destInterface.getId()+" message "+elem.getMsg() ); 
                            }
                            else {
                                print_debug( "stored transaction ");
                            }

                            
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                        
                
                    }

                    else {
                        //send to DhtNode.stack (different stack) and will be forwarded by the right thread 
                        // DhtStack.push(elem)
                        //print some infos biatch 
                    }
                    
                    print_debug("processed: " + elem.getSrc() + " to " + elem.getDest() + " contains: " + elem.getMsg());
                } 

            }


        }
    }

    /*********************************************************************************************************************/

    public static void print_debug(String msg){
        if (Common.DEBUG) System.out.println("ORDCONN:" + msg );
    }

    public static void print_log(String msg){
        if (Common.LOG) System.out.println( "ORDCONN:" + msg );
    }

}