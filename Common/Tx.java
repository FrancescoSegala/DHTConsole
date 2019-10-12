package Common;

public class Tx {

    private String dest;
    private String src;
    private String msg;
    private String info;


    public Tx(String src , String dest , String msg){
        this.dest = dest; 
        this.src = src; 
        this.msg = msg; 
        this.info = src+dest+msg;
    }


    public byte[] packTx(){
        String tx = this.src + ":" + this.dest + ":" + this.msg;
        return tx.getBytes();
    }

    public static Tx string2tx(String msg){
        String[] tuple = msg.split(":");
        return new Tx(tuple[0], tuple[1], tuple[2]);
    }


    /**
     * @return the dest
     */
    public String getDest() {
        return dest;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @return the src
     */
    public String getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(String dest) {
        this.dest = dest;
    }





}