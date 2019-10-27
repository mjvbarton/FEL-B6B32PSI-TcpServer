package cz.cvut.fel.psi.tcpserver;

import cz.cvut.fel.psi.tcpserver.exceptions.UnauthenticatedException;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matej
 */
public class User implements Serializable{
    private final String username;
    private final Pattern USERNAME_PATTERN = Pattern.compile("^Robot.*");
    
    private boolean authorized = false;
    
    public User(String username){
        this.username = username;
    }
    
    public User(Request request){
        if(request.getType() == RequestType.USERNAME){
            this.username = request.getData();
        } else {
            throw new RuntimeException();
        }
    }
    
    public void authorize(String password) throws UnauthenticatedException{
        Matcher m = USERNAME_PATTERN.matcher(username);
        if(m.matches() == false || getPassword().equals(password) == false){
            throw new UnauthenticatedException("Authentication failed for user " + this);
        }
    }
           
    private String getPassword(){
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
