package OrdererInterface;
import Common.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Future;

public class ConnectionManager implements Runnable {

    /**
     * Connection manager thread re-organize the ADT received via the connection
     * messages
     */
    private final int num_tries = 3;
    private ArrayList<Future<ConnectionInterface>> futureEndpointsList;
    private Hashtable<String, ConnectionInterface> endpointsMap;

    public ConnectionManager(ArrayList<Future<ConnectionInterface>> ordList,
            Hashtable<String, ConnectionInterface> ordMap) {
        this.futureEndpointsList = ordList;
        this.endpointsMap = ordMap;
    }

    @Override
    public void run() {

        int added = 0;
        int listSize = futureEndpointsList.size();
        int tries = 0;
        while (tries < num_tries && added < listSize) {

            try {
                for (Future<ConnectionInterface> elem_i : futureEndpointsList) {

                    if (elem_i.isDone()) {

                        if (elem_i.get() == null) {
                            print_debug("element elem_i.get() is null, removed from the List");
                            futureEndpointsList.remove(elem_i);
                            continue;
                        }

                        ConnectionInterface elem = elem_i.get();

                        if (endpointsMap.containsKey(elem.getId())) {
                            String elemId = elem.getId();
                            endpointsMap.replace(elemId, elem);
                            futureEndpointsList.remove(elem_i);
                            print_log("Orderer " + elemId + "rejoined the group");
                        }
                        else {
                            //add elem to the map
                            endpointsMap.put(elem.getId() , elem);
                            print_log("elem "+elem.getId()+" added to the Map");
                            futureEndpointsList.remove(elem_i);
                        }
                        added+=1;
                    } 
                }
                
                tries +=1;                     
                Thread.sleep((long) (Common.second * 0.5));
            } catch (Exception e) {
                //print_debug("exception occurred in manager thread");
            }
        }
        print_debug("tries, listSize, added "+tries+" "+listSize+" "+added+" ");
    }



    private static void print_log(String msg) {
        if (Common.LOG) System.out.println("CONNMAN: "+ msg);
    }

    private static void print_debug(String msg) {
        if (Common.DEBUG) System.out.println("CONNMAN: "+ msg);
    }


    //end
}