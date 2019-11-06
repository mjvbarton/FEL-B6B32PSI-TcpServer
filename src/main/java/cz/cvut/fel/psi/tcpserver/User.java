package cz.cvut.fel.psi.tcpserver;

import java.io.Serializable;

/**
 *
 * @author Matej
 */
public class User implements Serializable{
    private final String username;
    
    public User(String username){
        this.username = username;
    }
    
    public User(Request request){
        if(request.getType() == RequestType.USERNAME){
            this.username = request.getData();
        } else {
            //throw new IllegalStateException();
            this.username = "";
        }
    }
    
    public String getPassword(){
        byte[] bytes = username.getBytes();
        int sum = 0;
        for(byte b : bytes){
            sum += b;
        }
        return Integer.toString(sum);
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == username.hashCode();
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }        
}
