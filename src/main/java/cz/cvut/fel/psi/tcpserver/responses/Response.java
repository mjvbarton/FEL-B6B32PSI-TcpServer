/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.RequestType;
import cz.cvut.fel.psi.tcpserver.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * Response from the state machine
 * @author Matej
 */
public abstract class Response {
    protected final List<RequestType> acceptableRequests = new ArrayList();
    
    protected int code;
    protected String message;
    protected Session session;

    protected Response(Session session, int code, String message) {
        this.code = code;
        this.message = message;
        this.session = session;
    }
    
    

    /**
     * Generates the message in valid syntax for client
     * @return message for client
     */
    @Override
    public String toString() {
        return code + " " + message.toUpperCase() + "\r\n";
    }
      
    
    /**
     * Gets next step in valid sequence
     * @return next {@link Response} state or null if the state is final     
     */
    public abstract Response next();
   
}
