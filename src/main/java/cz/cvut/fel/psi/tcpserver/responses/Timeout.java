/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;

/**
 * Represents response {@code '502 TIMEOUT'}
 * <p>
 * As this is an error response it leads to cause of {@code Session.close()} by throwing {@code SessionClosedException}.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class Timeout extends Response{
    
    /**
     * Creates new {@code Timeout} response
     * @param session reference to parent {@code Session}     
     */
    public Timeout(Session session) {
        super(session, 502, "timeout");
    }    
    
    @Override
    public Response next() throws SessionClosedException {
        try {
            session.sendResponse(this);
            throw new SessionClosedException("Session needs to bee closed.");
        } catch (SessionRunException ex) {
            ex.printStackTrace();
            throw new SessionClosedException("Session needs to be closed because of an error.", ex);
        }
    }
    
}
