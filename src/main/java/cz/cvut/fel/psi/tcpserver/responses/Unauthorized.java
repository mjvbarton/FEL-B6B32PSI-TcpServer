/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fel.psi.tcpserver.responses;

import cz.cvut.fel.psi.tcpserver.Session;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionClosedException;
import cz.cvut.fel.psi.tcpserver.exceptions.SessionRunException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents reposne {@code '500 LOGIN FAILED'}. 
 * <p>
 * As this is an error response it leads to cause of {@code Session.close()} by throwing {@code SessionClosedException}.
 * @author Matej Barton (bartom47@fel.cvut.cz}
 */
public class Unauthorized extends Response {

    /**
     * Creates new {@code Unauthorized} response
     * @param session reference to parent {@code Session}
     */
    public Unauthorized(Session session) {
        super(session, 500, "login failed");
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
