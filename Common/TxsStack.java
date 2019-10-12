package Common;

import java.util.Stack;

public class TxsStack {

    /**class to preserve the syncronization for the stack via a sync access method 
     * so TxsStack can be implemented as required and modified just leaving the pop and push inaltered 
     */


    private Stack<Tx> txs; 


    public TxsStack(){
        this.txs = new Stack<>();
    }


    public synchronized int remaining(){
        return this.txs.size();
    }

    public synchronized boolean hasElements(){
        return !this.txs.empty();
    }

    public synchronized Tx getTX(){
        return txs.pop();
    }

    public synchronized void pushTX(Tx tx){
        txs.add(tx);
    }


    
}