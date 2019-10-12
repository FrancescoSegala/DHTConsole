package Common;
import java.net.Socket;

public class ConnectionInterface {


    private String id;
    private String hostName;
    private int port;
    private Socket socket;

    public ConnectionInterface(String id, String host, int port, Socket sock) {
        this.setId(id);
        this.setHostName(host);
        this.setPort(port);
        this.socket = sock;
    }

  


    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }


    public Socket getSocket(){
        return this.socket;
    }

    public void setSock(Socket sock){
        this.socket = sock ;
    }

//end class
}