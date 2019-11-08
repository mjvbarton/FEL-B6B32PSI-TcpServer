/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.requests.RequestType;
import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Response is an implementation of session's state machine.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public abstract class Response {

    protected static final Logger LOG = Logger.getLogger(Response.class.getName());
    protected final List<RequestType> acceptableRequests = new ArrayList();
    
    protected int code;
    protected String message;
    protected Session session;

    /**
     * Creates new {@code Response}
     * @param session a reference to parent {@code Session}
     * @param code code of the response
     * @param message text message told by response
     */
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
     * @throws cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException needs to be closed or if the session was closed by client    
     */
    public abstract Response next() throws SessionClosedException;
   
}
