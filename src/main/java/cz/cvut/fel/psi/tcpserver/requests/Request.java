 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.requests;

import cz.cvut.fel.psi.tcpserver.exceptions.RequestSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Represents clients request.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 * @param <T> Type of request data
 */
public abstract class Request<T> {
    protected T data;    
    protected final RequestType keyword;    
    
    /**
     * Creates request with keyword defined.
     * @param keyword that is contained by the request     
     */
    public Request(RequestType keyword){
        this.keyword = keyword;        
    }
    
    /**
     * Returns data as {@code T} object
     * @return {@code T} data or null if the request does not contain any data
     */
    public T getData(){
        return data;
    }
    
    /**
     * 
     * @return 
     */
    public RequestType getType(){
        return keyword;
    }
    
    /**
     * Loader for {@code T} data.Generates data object from {@link java.io.InputStream} 
     * @param in input stream from session
     * @throws RequestSyntaxException 
     * @throws java.io.IOException when reading from stream fails
     */
    public abstract void loadData(InputStream in) throws RequestSyntaxException, IOException;  
    
    @Override
    public String toString() {
        return "Request{keyword=" + keyword + "; data=" + data + "}";
    }
        
}
