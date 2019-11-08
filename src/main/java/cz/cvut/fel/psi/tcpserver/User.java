package cz.cvut.fel.psi.tcpserver;

import java.io.Serializable;

/**
 *
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class User implements Serializable{
    private final String username;
    private final String password;
    
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    
//    public User(Request request){
//        if(request.getType() == RequestType.USERNAME){
//            this.username = request.getData();
//        } else {
//            //throw new IllegalStateException();
//            this.username = "";
//        }
//        this.password = "";
//    }
    
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
